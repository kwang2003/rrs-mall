package com.aixforce.restful.controller.neusoft;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.item.dto.ItemsWithTagFacets;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemSearchService;
import com.aixforce.item.service.ItemService;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.Signatures;
import com.aixforce.shop.dto.RichShop;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ItemTagService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.model.Address;
import com.aixforce.user.model.UserExtra;
import com.aixforce.user.service.AddressService;
import com.aixforce.user.service.UserExtraService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 4/17/14
 * Time: 14:28
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/shop")
public class NSShops {

    @Autowired
    UserExtraService userExtraService;

    @Autowired
    ShopService shopService;

    @Autowired
    ItemTagService itemTagService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    AddressService addressService;

    @Autowired
    ItemSearchService itemSearchService;

    @Autowired
    ItemService itemService;


    @Value("#{app.restkey}")
    private String key;

    /**
     * 获取商店基本信息
     *
     * @param id        店铺id, 必填
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     * @return   店铺基本信息
     */
    @RequestMapping(value = "/{id}/info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<RichShop> baseInfo(@PathVariable Long id,
                                            @RequestParam("channel") String channel,
                                            @RequestParam("sign") String sign,
                                            HttpServletRequest request) {
        HaierResponse<RichShop> result = new HaierResponse<RichShop>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Shop> shopGetResult = shopService.findById(id);
            checkState(shopGetResult.isSuccess(), shopGetResult.getError());

            Shop shop = shopGetResult.getResult();
            RichShop richShop = BeanMapper.map(shop, RichShop.class);

            if (notNull(shop.getRegion())) {
                Response<List<Address>> regionGetResult = addressService.ancestorOfAddresses(shop.getRegion());
                checkState(regionGetResult.isSuccess(), regionGetResult.getError());
                setShopAddress(richShop, regionGetResult.getResult());
            }

            Response<UserExtra> extraGetResult = userExtraService.findByUserId(shop.getUserId());
            checkState(extraGetResult.isSuccess(), extraGetResult.getError());
            UserExtra userExtra = extraGetResult.getResult();

            richShop.setSoldQuantity(Objects.firstNonNull(userExtra.getTradeQuantity(), 0));
            richShop.setSale(Objects.firstNonNull(userExtra.getTradeSum(), 0L));


            Response<Long> countResult = itemService.countOnShelfByShopId(id);
            checkState(countResult.isSuccess(), countResult.getError());
            richShop.setItemCount(countResult.getResult().intValue());

            result.setResult(richShop, key);
        } catch (IllegalStateException e) {
            log.error("fail to get shop with id:{}, error:{}", id, e.getMessage());
            result.setError(messageSources.get("shop.query.fail"));
        } catch (Exception e) {
            log.error("fail to get shop with id:{}", id, e);
            result.setError(messageSources.get("shop.query.fail"));
        }

        return result;
    }

    private void setShopAddress(RichShop richShop, List<Address> result) {
        for (Address address: result) {
            String addressName = address.getName();
            switch (address.getLevel()) {
                case 1:
                    richShop.setProvinceName(addressName);
                    break;
                case 2:
                    richShop.setCityName(addressName);
                    break;
                case 3:
                    richShop.setRegionName(addressName);
                    break;
                default:break;
            }
        }
    }

    /**
     * 获取店铺首页商品
     *
     * @param id        店铺id, 必填
     * @param order     返回排序方式, 必填
     * @param size      每页数量, 必填
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     */
    @RequestMapping(value = "/{id}/recommend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<Item>> recommend(@PathVariable Long id,
                                               @RequestParam(value = "order", defaultValue = "hot") String order,
                                               @RequestParam(value = "size", defaultValue = "12") Integer size,
                                               @RequestParam(value = "channel") String channel,
                                               @RequestParam(value = "sign") String sign,
                                               HttpServletRequest request) {

        HaierResponse<List<Item>> result = new HaierResponse<List<Item>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Shop> shopGet = shopService.findById(id);
            checkState(shopGet.isSuccess(), shopGet.getError());

            Shop shop = shopGet.getResult();

            Response<List<Item>> itemsGet = itemSearchService.recommendItemInShop(shop.getUserId(), "auto", null,
                    size, order, null);

            checkState(itemsGet.isSuccess(), itemsGet.getError());
            result.setResult(itemsGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to get recommend items with id:{}, order:{}, size:{}, error:{}", id, order, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get recommend items with id:{}, order:{}, size:{}", id, order, size, e);
            result.setError(messageSources.get("item.query.fail"));
        }

        return result;
    }


    /**
     * 店铺内所有商品信息
     *
     * @param id        店铺id, 必填
     * @param order     返回排序方式, 必填
     * @param q         查询关键字, 必填
     * @param size      每页数量, 必填
     * @param pageNo    页数, 必填
     */
    @RequestMapping(value = "/{id}/items", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<ItemsWithTagFacets> items(@PathVariable Long id,
                                           @RequestParam("order") String order,
                                           @RequestParam("q") String q,
                                           @RequestParam(value = "size", defaultValue = "12") Integer size,
                                           @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam("channel") String channel,
                                           @RequestParam("sign") String sign,
                                           HttpServletRequest request) {

        HaierResponse<ItemsWithTagFacets> result = new HaierResponse<ItemsWithTagFacets>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Map<String, String> params = Maps.newHashMap();

            Response<Shop> shopGet = shopService.findById(id);
            checkState(shopGet.isSuccess(), shopGet.getError());
            Shop shop = shopGet.getResult();

            params.put("sellerId", String.valueOf(shop.getUserId()));
            params.put("q", q);
            params.put("sort", order);

            Response<ItemsWithTagFacets> itemsGet = itemSearchService.searchOnShelfItemsInShop(pageNo, size, params);
            checkState(itemsGet.isSuccess(), itemsGet.getError());
            result.setResult(itemsGet.getResult());

        } catch (IllegalStateException e) {
            log.error("fail to search items with id:{}, order:{}, q:{}, size:{}, pageNo:{}, error:{}",
                    id, order, q, size, pageNo, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to search items with id:{}, order:{}, q:{}, size:{}, pageNo:{}",
                    id, order, q, size, pageNo, e);
            result.setError(messageSources.get("item.query.fail"));
        }
        return result;
    }

    /**
     * 店铺所有商品分类
     *
     * @param id            店铺id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     */
    @RequestMapping(value = "/{id}/categories", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<String> shopCategories(@PathVariable Long id,
                                                @RequestParam("channel") String channel,
                                                @RequestParam("sign") String sign,
                                                HttpServletRequest request) {
        HaierResponse<String> result = new HaierResponse<String>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");



            Response<String> cateGet = itemTagService.findTree(id);
            result.setResult(cateGet.getResult());

        } catch (Exception e) {
            log.error("fail to query categories with id:{}", id);
            result.setError(messageSources.get("shop.categories.query.fail"));
        }
        return result;
    }
}
