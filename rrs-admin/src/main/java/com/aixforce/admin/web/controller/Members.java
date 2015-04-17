package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.dto.BuyerDto;
import com.aixforce.user.model.User;
import com.aixforce.user.model.UserProfile;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.service.AddressService;
import com.aixforce.user.service.UserProfileService;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * User: yangzefeng
 * Date: 13-11-20
 * Time: 上午9:50
 */

@Controller
@Slf4j
@RequestMapping("/api/admin/members")
@SuppressWarnings("unused")
public class Members {
    private final static Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemService itemService;


    @RequestMapping(value = "/findById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<BuyerDto> findById(@RequestParam(value = "id") Long id) {
        Response<User> result = accountService.findUserById(id);
        BuyerDto buyerDto = new BuyerDto();
        if (result.isSuccess()) {
            try {
                User user = result.getResult();
                UserProfile userProfile = userProfileService.findUserProfileByUserId(user.getId()).getResult();
                BeanMapper.copy(userProfile, buyerDto);
                String provinceName = addressService.findById(userProfile.getProvinceId()).getResult().getName();
                String cityName = addressService.findById(userProfile.getCityId()).getResult().getName();
                buyerDto.setProvince(provinceName);
                buyerDto.setCity(cityName);
                BeanMapper.copy(user, buyerDto);
                return new Paging<BuyerDto>(1L, Lists.newArrayList(buyerDto));
            } catch (Exception e) {
                log.error("fail to find userProfile by userId");
                throw new JsonResponseException(500, "fail to find userProfile by userId");
            }
        } else {
            log.error("failed to find user by id");
            throw new JsonResponseException(500, result.getError());
        }
    }

    /**
     * 店铺冻结或者解冻，给后台运营用
     *
     * @param ids    店铺ids
     * @param status 店铺待更新的状态
     * @return 操作是否成功
     */
    @RequestMapping(value = "/updateSellers", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateMultiStatus(@RequestParam(value = "ids") String ids,
                                    @RequestParam(value = "status") Integer status) {
        Response<Boolean> result;
        result = shopService.updateStatusByIds(ids, status);
        if (!result.isSuccess()) {
            log.error("failed to update shop status by ids,error code:{}",result.getError());
            throw new JsonResponseException(500, result.getError());
        }
        List<String> parts = splitter.splitToList(ids);
        List<Long> userIds = Lists.newArrayListWithCapacity(parts.size());
        for (String part : parts) {
            Response<Shop> shop = shopService.findById(Long.parseLong(part));
            if(!shop.isSuccess()){
                log.error("failed to find shop(id={}),error code:{}",part,shop.getError());
                throw new JsonResponseException(500,result.getError());
            }
            userIds.add(shop.getResult().getUserId());
        }

        if (Objects.equal(status, Shop.Status.OK.value())) {
            result = accountService.updateStatusByIds(userIds, User.STATUS.NORMAL.toNumber());
            if (!result.isSuccess()) {
                log.error("failed to update seller status by ids,error code:{}",result.getError());
                throw new JsonResponseException(500, result.getError());
            }
            result = itemService.updateStatusBySellerIds(userIds, Item.Status.OFF_SHELF.toNumber());
            if (!result.isSuccess()) {
                log.error("failed to update item status by sellerIds,error code:{}",result.getError());
                throw new JsonResponseException(500, result.getError());
            }
        }
        if (Objects.equal(status, Shop.Status.FROZEN.value())) {
            result = accountService.updateStatusByIds(userIds, User.STATUS.FROZEN.toNumber());
            if (!result.isSuccess()) {
                log.error("failed to update seller status by ids,error code:{}",result.getError());
                throw new JsonResponseException(500, result.getError());
            }
            result = itemService.updateStatusBySellerIds(userIds, Item.Status.FROZEN.toNumber());
            if (!result.isSuccess()) {
                log.error("failed to update item status by sellerIds,error code:{}",result.getError());
                throw new JsonResponseException(500, result.getError());
            }
        }

        //重新计算店铺宝贝数,解冻后店铺内所有商品都为下架状态，所以直接把店铺宝贝数设为0
        if (Objects.equal(status, Shop.Status.FROZEN.value())) {
            for(String sShopId : parts) {
                Long shopId = Long.parseLong(sShopId);
                itemService.setItemCountByShopId(shopId, 0l);
            }
        }

        log.info("user id={} update shops ids={} status to {}",
                UserUtil.getCurrentUser().getId(), ids, Shop.Status.from(status));

        return "ok";
    }

    @RequestMapping(value = "/updateMembers", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateMembersStatus(@RequestParam("ids") String ids,
                                      @RequestParam("status") Integer status) {
        Response<Boolean> result;
        List<String> parts = splitter.splitToList(ids);
        List<Long> idLongs = Lists.newArrayListWithCapacity(parts.size());
        for (String part : parts) {
            idLongs.add(Long.parseLong(part));
        }
        result = accountService.updateStatusByIds(idLongs, status);
        if (!result.isSuccess()) {
            log.error("update status by userIds fail,error code:{}",result.getError());
            throw new JsonResponseException(result.getError());
        }

        log.info("user id={} update member ids={} to status {}", UserUtil.getCurrentUser().getId(), ids, User.STATUS.fromNumber(status));

        return "ok";
    }
}
