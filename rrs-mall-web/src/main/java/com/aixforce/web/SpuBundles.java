package com.aixforce.web;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.SpuBundle;
import com.aixforce.item.service.SpuBundleService;
import com.aixforce.rrs.grid.dto.SellerBrandsDto;
import com.aixforce.rrs.grid.model.UnitBrand;
import com.aixforce.rrs.grid.model.UnitSeller;
import com.aixforce.rrs.grid.service.BrandsSellersService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 套餐模版控制器
 * CREATED BY: IntelliJ IDEA
 * AUTHOR: haolin
 * ON: 14-4-23
 */
@Controller
@Slf4j
@RequestMapping("/api")
public class SpuBundles {
    @Autowired
    private MessageSources messageSources;

    @Autowired
    private SpuBundleService spuBundleService;

    @Autowired
    private BrandsSellersService brandsSellersService;

    /**
     * 创建
     */
    @RequestMapping(value = "/brander/spubundles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String create(SpuBundle sb){
        sb.setUserId(UserUtil.getUserId());
        Response<Long> result = spuBundleService.create(sb);
        if(!result.isSuccess()){
            log.error("failed to create SpuBundle({})", sb);
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 更新
     */
    @RequestMapping(value = "/brander/spubundles/{sbId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String update(@PathVariable Long sbId, SpuBundle sb){
        Long userId = UserUtil.getUserId();
        preValidate(sbId, userId);
        Response<Boolean> result = spuBundleService.update(sb);
        if (!result.isSuccess()){
            log.error("failed to update spubundle(id={}) from {},error code :{}",
                    sbId, sb, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 上下架
     */
    @RequestMapping(value = "/brander/spubundle/{sbId}/onoff", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String onOff(@PathVariable Long sbId, @RequestParam Integer status){
        Long userId = UserUtil.getUserId();
        preValidate(sbId, userId);
        Response<Boolean> result = spuBundleService.onOff(sbId, SpuBundle.Status.from(status));
        if(!result.isSuccess()){
            log.error("failed to on or off spubundle(id={})", sbId);
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 更新使用此时
     */
    @RequestMapping(value = "/brander/spubundle/{sbId}/incrused", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String incrUsedCount(@PathVariable Long sbId){
        Long userId = UserUtil.getUserId();
        preValidate(sbId, userId);
        Response<Boolean> result = spuBundleService.incrUsedCount(sbId);
        if(!result.isSuccess()){
            log.error("failed to increment spubundle.usedCount(id={})", sbId);
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/brander/spubundles/{sbId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable Long sbId){
        Long userId = UserUtil.getUserId();
        preValidate(sbId, userId);
        Response<Boolean> result = spuBundleService.delete(sbId);
        if(!result.isSuccess()){
            log.error("failed to delete spubundle(id={})", sbId);
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 预验证
     * @param sbId 套餐模版id
     * @param userId 套餐模版创建者id
     */
    private void preValidate(Long sbId, Long userId) {
        BaseUser user = UserUtil.getCurrentUser();

        if (user == null){
            throw new JsonResponseException(500, messageSources.get("user.not.login"));
        }
        // 是否是品牌商
        if (!Objects.equal(user.getType(), BaseUser.TYPE.WHOLESALER)){
            throw new JsonResponseException(500, messageSources.get("user.isnt.brander"));
        }

        Response<SpuBundle> result = spuBundleService.findById(sbId);

        if (!result.isSuccess()){
            log.error("failed to find spubundle id = {},error code:{}",
                    sbId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        SpuBundle older = result.getResult();

        if (older == null) {
            throw new JsonResponseException(500, messageSources.get("spubundle.not.exist"));
        }

        if (!Objects.equal(userId, older.getUserId())){
            throw new JsonResponseException(500, messageSources.get("spubundle.not.owner"));
        }
    }

    @RequestMapping(value = "/brander/spubundles/templates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<SpuBundle> spuBundleOfBrander(@RequestParam("pageNo") Integer pageNo,
                                              @RequestParam("size") Integer size) {
        BaseUser currentUser = UserUtil.getCurrentUser();

        if (currentUser == null) {
            return new Paging<SpuBundle>(0l, Collections.<SpuBundle>emptyList());
        }


        Response<SellerBrandsDto> brandsR = brandsSellersService.findBrandsBySeller(currentUser.getId());
        if (!brandsR.isSuccess()) {
            log.error("find brands by seller fail, seller(id={}), error code:{}", currentUser.getId(), brandsR.getError());
            throw new JsonResponseException(500, messageSources.get(brandsR.getError()));
        }
        SellerBrandsDto sellerBrandsDto = brandsR.getResult();
        if(sellerBrandsDto.getBrands().isEmpty()) {
            // 先当成功返回
            return new Paging<SpuBundle>(0l, Collections.<SpuBundle>emptyList());
        }
        List<UnitBrand> brands = sellerBrandsDto.getBrands();

        Response<List<UnitSeller>> usersR = brandsSellersService.findSellersByBrands(brands);
        if (!usersR.isSuccess()) {
            log.error("failed to find users by  brands:{}, error code:{}", brands, usersR.getError());
            throw new JsonResponseException(500, messageSources.get(brandsR.getError()));
        }

        List<Long> userIds = Lists.newArrayListWithCapacity(usersR.getResult().size());
        for (UnitSeller s: usersR.getResult()) {
            userIds.add(s.getSellerId());
        }

        Response<Paging<SpuBundle>> spuBundleR = spuBundleService.findByUserIds(userIds, pageNo, size);
        if (spuBundleR.isSuccess()) {
            return spuBundleR.getResult();
        }
        log.error("failed to get spu bundles by userIds={}, error code:{}",userIds, spuBundleR.getError());
        throw new JsonResponseException(500, messageSources.get(spuBundleR.getError()));
    }
}
