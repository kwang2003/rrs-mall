package com.aixforce.admin.web.controller;

import com.aixforce.admin.event.AdminEventBus;
import com.aixforce.admin.event.OuterCodeSetEvent;
import com.aixforce.category.model.BackCategory;
import com.aixforce.category.service.BackCategoryService;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Brand;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.rrs.grid.service.ShopAuthorizeInfoService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.model.Site;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: yangzefeng
 * Date: 13-11-6
 * Time: 下午2:26
 */
@Controller
@RequestMapping("/api/admin/shops")
public class Shops {
    private final static Logger log = LoggerFactory.getLogger(Shops.class);
    private final static Splitter splitter = Splitter.on(",");
    private static final JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    @Autowired
    private ShopService shopService;
    @Autowired
    private MessageSources messageSources;
    @Autowired
    private AccountService<User> accountService;
    @Autowired
    private SiteService siteService;
    @Autowired
    private ShopAuthorizeInfoService shopAuthorizeInfoService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private AdminEventBus eventBus;
    @Autowired
    private GridService gridService;
    @Autowired
    private BackCategoryService backCategoryService;

    @RequestMapping(value = "/full-dump", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String fullDump() {
        shopService.fullDump();
        return "full-dump finished";
    }

    /**
     * 店铺审核通过或者不通过，给后台运营用
     *
     * @param shopIds 店铺ids
     * @param status  店铺待更新状态
     * @return 操作是否成功
     */
    @RequestMapping(value = "/updateMulti", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateStatusByIds(@RequestParam("ids") String shopIds,
                                    @RequestParam("status") Integer status) {
        Response<Boolean> result;

        result = shopService.approved(shopIds, status);
        if (!result.isSuccess()) {
            log.error("failed to update shop status by shopIds, error code:{}", result.getError());
            throw new JsonResponseException(500, result.getError());
        }
        Iterable<String> parts = splitter.trimResults().omitEmptyStrings().split(shopIds);
        //审核不通过，typeTransfer改为No
        if (Objects.equal(status, Shop.Status.FAIL.value())) {
            for (String shopId : parts) {
                Response<Shop> shop = shopService.findById(Long.parseLong(shopId));
                if(!shop.isSuccess()){
                    log.error("failed to find shop(id={}),error code:{}",shopId,shop.getError());
                    throw new JsonResponseException(500,messageSources.get(shop.getError()));
                }
                Long userId = shop.getResult().getUserId();
                User user = new User();
                user.setId(userId);
                user.setTags("-1");
                Response<Boolean> ur = accountService.updateUser(user);
                if (!ur.isSuccess()) {
                    log.error("failed to update user info to {},error code :{}", user, result.getError());
                    throw new JsonResponseException(500, "升级卖家失败");
                }
            }
        }
        //买家变为卖家
        if (Objects.equal(status, Shop.Status.OK.value())) {
            //默认情况下找模版，第一页，每页大小为10
            Response<Paging<Site>> templates = siteService.pagination(null, 1, 10);
            if (!templates.isSuccess()) {
                log.error("no template exist, cause:{}", templates.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            //找到第一套已发布的模板
            Long templateId = null;
            for (Site site : templates.getResult().getData()) {
                if (site.getReleaseInstanceId() != null) {
                    templateId = site.getId();
                    break;
                }
            }
            if (templateId == null) {
                throw new JsonResponseException(500, "未找到任何已发布的模板，无法正确创建店铺");
            }
            for (String shopId : parts) {
                Response<Shop> shop = shopService.findById(Long.parseLong(shopId));
                Long userId = shop.getResult().getUserId();
                User user = new User();
                user.setId(userId);
                user.setType(User.TYPE.SELLER.toNumber());
                Response<Boolean> ur = accountService.updateUser(user);
                if (!ur.isSuccess()) {
                    log.error("failed to update user info to {},error code :{}", user, result.getError());
                    throw new JsonResponseException(500, "升级卖家失败");
                }
                //一些初始化，审核通过自动生成shopSite，每个店铺有且只有一个
                Site site = new Site();
                site.setName(shop.getResult().getName());
                site.setSubdomain("shop" + userId);
                Response<Long> shopSite = siteService.createShopSite(userId, site, templateId);
                if (!shopSite.isSuccess()) {
                    log.error("fail to create shop site,cause:{}", shopSite.getError());
                    throw new JsonResponseException(500, messageSources.get(shopSite.getError()));
                }
            }
        }
        log.info("user id={} update shops status, shopIds={}, status={}", UserUtil.getCurrentUser().getId(),
                shopIds, Shop.Status.from(status));
        return "ok";
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean delete(@PathVariable("id") Long id) {
        Response<Shop> shopR = shopService.findById(id);

        if(!shopR.isSuccess()) {
            log.error("failed to find shop id={}, error code={}", id, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        if(!Objects.equal(shopR.getResult().getStatus(), Shop.Status.FAIL.value())) {
            log.error("shop id={} can not delete, only shop with status fail can be delete", shopR.getResult().getId());
            throw new JsonResponseException(500, messageSources.get("shop.can.not.delete"));
        }
        Response<Boolean> result = shopService.delete(id);
        if(!result.isSuccess()) {
            log.error("failed to delete shop id={},error code={}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        log.info("shop id={} be deleted by user id={}", id, UserUtil.getCurrentUser().getId());
        return result.getResult();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateSubDomain(@RequestParam(value = "subDomain") String subDomain,
                                @PathVariable("id") Long shopId) {
        Response<Shop> shopR = shopService.findById(shopId);
        Server500Exception.failToThrow(shopR, "query shop {} failed", shopId);
        Shop shop = shopR.getResult();
        //验证二级域名唯一性
        Response<Site> existR = siteService.findBySubdomain(subDomain);
        if(existR.isSuccess() && existR.getResult() != null) {
            if (!Objects.equal(shop.getUserId(), existR.getResult().getUserId())) {
                log.error("sub domain exist");
                throw new JsonResponseException(500, messageSources.get("second.domain.exist"));
            } else {
                return;
            }
        }
        Response<Site> siteR = siteService.findShopByUserId(shop.getUserId());
        Server500Exception.failToThrow(shopR, "query site {} failed", shop.getUserId());
        Site site = siteR.getResult();
        site.setSubdomain(subDomain);
        Response<Boolean> updateR = siteService.update(site);
        if(!updateR.isSuccess()) {
            log.error("failed to update second domain={}, shopId={}, error code:{}",
                    subDomain, shopId, updateR.getError());
            throw new JsonResponseException(500, messageSources.get(updateR.getError()));
        }
        log.info("user id={} update shop id={} sub domain={}", UserUtil.getCurrentUser().getId(), shopId, subDomain);
    }

    @RequestMapping(value = "/{id}/authorizeInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createAuthorizeInfo(@PathVariable("id") Long shopId,
                                      @RequestBody ShopAuthorizeInfo shopAuthorizeInfo) {
        shopAuthorizeInfo.setShopId(shopId);
        Response<Shop>  shopR = shopService.findById(shopId);
        if(!shopR.isSuccess()){
            log.error("failed to find shop (id={}), error code:{}", shopId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Shop shop = shopR.getResult();

        //在二级类目名称前面加上一级类目名称，用->分割
        shopAuthorizeInfo = addParentCategoryName(shopAuthorizeInfo);
        Response<Long> result = shopAuthorizeInfoService.create(shopAuthorizeInfo, shop.getUserId(), shop.getUserName());
        if(!result.isSuccess()) {
            log.error("failed to create shopAuthorizeInfo{}, error code:{}",shopAuthorizeInfo, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        Long shopAuthorizeInfoId = result.getResult();
        final ShopAuthorizeInfo exist = shopAuthorizeInfoService.findById(shopAuthorizeInfoId).getResult();

        Response<Shop> shopGet = shopService.findById(shopId);
        if (!shopGet.isSuccess()) {
            log.error("failed to find shop by shopId:{}, error code:{}", shopId, shopGet.getError());
            throw new JsonResponseException(500, messageSources.get(shopGet.getError()));
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                batchUpdateItemRegion(exist.getShopId());
            }
        });

        log.info("user id={} create shop authorizeInfo shopId={}, authorizeInfo{}",
                UserUtil.getCurrentUser().getId(), shopId, shopAuthorizeInfo);

        return shopAuthorizeInfoId;
    }

    @RequestMapping(value = "/{id}/authorizeInfo", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateAuthorizeInfo(@PathVariable("id") Long shopAuthorizeInfoId,
                                      @RequestBody ShopAuthorizeInfo shopAuthorizeInfo) {
        shopAuthorizeInfo.setId(shopAuthorizeInfoId);
        Response<ShopAuthorizeInfo> saiR = shopAuthorizeInfoService.findById(shopAuthorizeInfoId);
        if(!saiR.isSuccess()) {
            log.error("failed to query shopAuthorizeInfo by id={}, error code:{}", shopAuthorizeInfoId, saiR.getError());
            throw new JsonResponseException(500, messageSources.get(saiR.getError()));
        }

        final ShopAuthorizeInfo existSai = saiR.getResult();
        final Long shopId = existSai.getShopId();
        Response<Shop>  shopR = shopService.findById(shopId);
        if(!shopR.isSuccess()){
            log.error("failed to find shop (id={}), error code:{}", shopId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Shop shop = shopR.getResult();

        //在二级类目名称前面加上一级类目名称，用->分割
        shopAuthorizeInfo = addParentCategoryName(shopAuthorizeInfo);

        Response<Boolean> result = shopAuthorizeInfoService.update(existSai,shopAuthorizeInfo, shop.getUserId(), shop.getUserName());
        if(!result.isSuccess()) {
            log.error("failed to update shopAuthorizeInfo{}, error code:{}", shopAuthorizeInfoId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        final ShopAuthorizeInfo exist = shopAuthorizeInfoService.findById(shopAuthorizeInfoId).getResult();
        //need to update item
        executor.submit(new Runnable() {
            @Override
            public void run() {
                batchUpdateItemRegion(exist.getShopId());
            }
        });

        log.info("user id={} update shop authorizeInfo shopId={}, authorizeInfo{}",
                UserUtil.getCurrentUser().getId(), shopId, shopAuthorizeInfo);

        return "ok";
    }

    //在二级类目名称前面加上一级类目名称，用->分割
    private ShopAuthorizeInfo addParentCategoryName(ShopAuthorizeInfo shopAuthorizeInfo) {

        try {
            AuthorizeInfo authorizeInfo =
                    jsonMapper.fromJson(shopAuthorizeInfo.getJsonAuthorize(), AuthorizeInfo.class);
            List<BackCategory> backCategories = authorizeInfo.getCategories();
            List<BackCategory> newBackCategories = Lists.newArrayListWithCapacity(backCategories.size());
            for (BackCategory bc : backCategories) {
                Response<BackCategory> fullInfoBackCategoryR = backCategoryService.findById(bc.getId());
                if(!fullInfoBackCategoryR.isSuccess()) {
                    log.error("fail to find back category by id={}, error code:{}",
                            bc.getId(), fullInfoBackCategoryR.getError());
                }
                BackCategory fibc = fullInfoBackCategoryR.getResult();
                Response<BackCategory> backCategoryR = backCategoryService.findById(fibc.getParentId());
                if (!backCategoryR.isSuccess() || backCategoryR.getResult() == null) {
                    log.error("fail to find back category by id={}, error code:{}",
                            bc.getParentId(), backCategoryR.getError());
                }
                BackCategory parent = backCategoryR.getResult();
                bc.setName(parent.getName() + "->" + bc.getName());
                newBackCategories.add(bc);
            }
            authorizeInfo.setCategories(newBackCategories);
            String newAuthorizeJson = jsonMapper.toJson(authorizeInfo);
            shopAuthorizeInfo.setJsonAuthorize(newAuthorizeJson);
            return shopAuthorizeInfo;
        }catch (Exception e) {
            log.error("fail to add parent category name, shopAuthorizeInfo={}, cause:{}",
                    shopAuthorizeInfo, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("createOrUpdate.shopAuthorizeInfo.fail"));
        }

    }

    @RequestMapping(value = "/authorizeInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ShopAuthorizeInfo> find(@RequestParam("id") Long shopId) {
        Response<List<ShopAuthorizeInfo>> result = shopAuthorizeInfoService.findByShopIdNoCache(shopId);
        if(!result.isSuccess()) {
            log.error("failed to query shopAuthorizeInfo by shopId={}, error code:{}", shopId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/{id}/authorizeInfo", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteShopAuthorizeInfo(@PathVariable("id") Long shopAuthorizeInfoId) {
        final Response<ShopAuthorizeInfo> rExist = shopAuthorizeInfoService.findById(shopAuthorizeInfoId);

        if(!rExist.isSuccess()){
            log.warn("failed to find shopAuthorizeInfo(id={}), error code:{}", shopAuthorizeInfoId, rExist.getError());
            throw new JsonResponseException(500, messageSources.get(rExist.getError()));
        }

        final ShopAuthorizeInfo existSai = rExist.getResult();
        final Long shopId = existSai.getShopId();
        Response<Shop>  shopR = shopService.findById(shopId);
        if(!shopR.isSuccess()){
            log.error("failed to find shop (id={}), error code:{}", shopId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }

        Response<Boolean> result = shopAuthorizeInfoService.delete(existSai, shopR.getResult().getUserId());
        if(!result.isSuccess()) {
            log.error("failed to delete shopAuthorizeInfo id={}, error code:{}", shopAuthorizeInfoId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        //update item
        executor.submit(new Runnable() {
            @Override
            public void run() {
                batchUpdateItemRegion(existSai.getShopId());
            }
        });

        log.info("user id={} delete shop authorizeInfo shopId={}, authorizeInfoId={}",
                UserUtil.getCurrentUser().getId(), shopId, shopAuthorizeInfoId);

        return "ok";
    }

    @RequestMapping(value = "/{id}/extra", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void setExtraInfo(@PathVariable("id") Long shopId,
                             @RequestParam(required = false) String outerCode,
                             @RequestParam(required = false) String ntalkerId) {

        try {
            checkArgument(notEmpty(outerCode) || notEmpty(ntalkerId), "param.can.not.empty");

            ShopExtra extra = new ShopExtra();
            extra.setShopId(shopId);
            extra.setOuterCode(outerCode);
            extra.setNtalkerId(ntalkerId);
            Response<Long> setR = shopService.setExtra(extra);
            checkState(setR.isSuccess(), setR.getError());

            // 默认给创建一个保证金账户
            eventBus.post(new OuterCodeSetEvent(shopId, outerCode));

            log.info("user id={} set shop id={} extra info outerCode={}, ntalkerId={}",
                    UserUtil.getCurrentUser().getId(), shopId, outerCode, ntalkerId);

        } catch (IllegalStateException e) {
            log.error("fail to set extra info for shop(id:{}), error:{}", shopId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to set extra info for shop(id:{}), cause:{}", shopId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("shop.extra.update.fail"));
        }
    }


    @RequestMapping(value = "/{id}/rate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateShopRate(@PathVariable("id") Long shopId,
                             @RequestParam("updating") Double rateUpdating) {

        try {

            ShopExtra updating = new ShopExtra();
            updating.setShopId(shopId);
            updating.setRateUpdating(rateUpdating);
            Response<Long> extraUpdateResult = shopService.setExtra(updating);
            checkState(extraUpdateResult.isSuccess(), extraUpdateResult.getError());

            log.info("user id={} update shop id={} rate={}", UserUtil.getCurrentUser().getId(), shopId, rateUpdating);

            return "ok";

        } catch (IllegalStateException e) {
            log.error("fail to update rate of shop failed with shopId:{},rateUpdating:{}, error:{}", shopId, rateUpdating, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to update rate of shop failed with shopId:{},rateUpdating:{} ", shopId, rateUpdating, e);
            throw new JsonResponseException(500, messageSources.get("shop.extra.set.rate.fail"));
        }
    }

    @RequestMapping(value = "/{id}/fee/need", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateShopDepositOrTechFeeNeed(@PathVariable("id") Long shopId,
                                               @RequestParam("depositNeed") Long depositNeed,
                                               @RequestParam("techFeeNeed") Long techFeeNeed) {

        try {

            ShopExtra updating = new ShopExtra();
            updating.setShopId(shopId);
            updating.setDepositNeed(depositNeed);
            updating.setTechFeeNeed(techFeeNeed);
            Response<Long> extraUpdateResult = shopService.setExtra(updating);
            checkState(extraUpdateResult.isSuccess(), extraUpdateResult.getError());

            log.info("user id={} update shop id={} deposit {} or techFee {}",
                    UserUtil.getCurrentUser().getId(), shopId, depositNeed, techFeeNeed);

            return "ok";

        } catch (IllegalStateException e) {
            log.error("fail to update rate of shop failed with shopId:{},depositNeed:{}, techFeeNeed:{}, error:{}",
                    shopId, depositNeed, techFeeNeed, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to update rate of shop failed with shopId:{},depositNeed:{}, techFeeNeed:{}, error:{}",
                    shopId, depositNeed, techFeeNeed, e.getMessage());
            throw new JsonResponseException(500, messageSources.get("shop.extra.update.fail"));
        }


    }




    @ToString
    public static class AuthorizeInfo implements Serializable {

        private static final long serialVersionUID = -5666685458223801535L;

        @Getter
        @Setter
        private List<Brand> brands;

        @Getter
        @Setter
        private List<BackCategory> categories;

        @Getter
        @Setter
        private List<Map<String, List<Long>>> regions;
    }

    private static final Integer PAGE_SIZE = 200;

    private void batchUpdateItemRegion(Long shopId) {
        List<ShopAuthorizeInfo> shopAuthorizeInfos = shopAuthorizeInfoService.findByShopId(shopId).getResult();
        Response<Long> maxIdR = itemService.maxIdByShopId(shopId);
        if(!maxIdR.isSuccess() || maxIdR.getResult() == null) {
            log.error("fail to find shop max item id by shopId={}, error code:{}",shopId, maxIdR.getError());
            return;
        }

        int returnSize = PAGE_SIZE;
        Long lastId = maxIdR.getResult() + 1;

        List<Item> updateds = Lists.newArrayList();

        while (returnSize == PAGE_SIZE) {
            Response<List<Item>> itemsR = itemService.findPagingItemByShopId(lastId, shopId, PAGE_SIZE);
            if(!itemsR.isSuccess()) {
                log.error("fail to find paging item by shopId={},lastId={},limit={},error code={}, skip it",
                        shopId, lastId, PAGE_SIZE, itemsR.getError());
                continue;
            }
            List<Item> items = itemsR.getResult();

            for(Item i : items) {
                Response<List<Long>> brandIdAndRegionIdsR = gridService.authorizeByInfos(i, shopAuthorizeInfos);
                if(!brandIdAndRegionIdsR.isSuccess()) {
                    log.error("fail to get regions by item id={}, shopAuthorizeInfos={}, error code:{},skip it",
                            i, shopAuthorizeInfos, brandIdAndRegionIdsR.getError());
                    continue;
                }
                List<Long> ids = brandIdAndRegionIdsR.getResult();
                List<Long> regionIds = ids.subList(1, ids.size());
                String regionStr = Joiner.on(",").skipNulls().join(regionIds);

                Item updated = new Item();
                updated.setId(i.getId());
                updated.setRegion(regionStr);
                updateds.add(updated);
            }

            returnSize = items.size();
            lastId = items.get(returnSize - 1).getId();
        }

        itemService.batchUpdateItemRegions(updateds);
    }
}
