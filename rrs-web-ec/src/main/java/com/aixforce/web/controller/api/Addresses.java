/*
 * Copyright (c) 2013 杭州端点网络科技有限公司
 */

package com.aixforce.web.controller.api;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.trade.dto.UserFreightInfo;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.trade.service.FreightCountService;
import com.aixforce.trade.service.UserTradeInfoService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.Address;
import com.aixforce.user.service.AddressService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;


/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-14
 */
@Controller
@RequestMapping("/api")
public class Addresses {

    private static final Logger log = LoggerFactory.getLogger(Addresses.class);

    private final AddressService addressService;

    private final UserTradeInfoService userTradeInfoService;

    private final MessageSources messageSources;

    private final GridService gridService;

    private final FreightCountService freightCountService;

    @Autowired
    public Addresses(AddressService addressService, UserTradeInfoService userTradeInfoService, MessageSources messageSources,
                     GridService gridService, FreightCountService freightCountService) {
        this.addressService = addressService;
        this.userTradeInfoService = userTradeInfoService;
        this.messageSources = messageSources;
        this.gridService = gridService;
        this.freightCountService = freightCountService;
    }

    @RequestMapping(value = "/address/provinces", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Address> provinces() {
        Response<List<Address>> result = addressService.provinces();
        if(!result.isSuccess()) {
            log.error("find provinces failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/address/province/{provinceId}/cities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Address> cities(@PathVariable("provinceId") Integer provinceId) {
        Response<List<Address>> result = addressService.citiesOf(provinceId);
        if(!result.isSuccess()) {
            log.error("find cities failed, provinceId={}, cause:{}", provinceId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/address/city/{cityId}/districts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Address> district(@PathVariable("cityId") Integer cityId) {
        Response<List<Address>> result = addressService.districtOf(cityId);
        if(!result.isSuccess()) {
            log.error("find cities failed, cityId={}, cause:{}", cityId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 新增异步计算运费的逻辑
     * @param itemsInfo 商品信息（包含itemId，quantity,数据格式itemsInfo=["100:2","100:3","100:4"]）
     * @param request 用于获取区域cookie信息
     * @return  List
     * 返回一个
     */
    @RequestMapping(value = "/user/tradeInfos", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserFreightInfo getTradeInfos(@RequestBody String[] itemsInfo, HttpServletRequest request) {
        Long userId = UserUtil.getUserId();

        Response<List<UserTradeInfo>> result = userTradeInfoService.findTradeInfosByUserId(userId);
        if (result.isSuccess()) {
            //获取用户对应与该区域的物流地址
           Cookie[] cookies = request.getCookies();
           Map<String, String> cookieKVs = Maps.newHashMap();
           for(Cookie cookie : cookies) {
                cookieKVs.put(cookie.getName(), cookie.getValue());
            }

            //运费计算
            UserFreightInfo userFreightInfo = new UserFreightInfo();
            userFreightInfo.setTradeInfoList(result.getResult());

            //当前只计算到省份
            Response<Integer> provinceRes = gridService.findProvinceFromCookie(cookieKVs);
            if(!provinceRes.isSuccess()){
                log.error("failed to find provinceId from cookie, error code={}", provinceRes.getError());
                throw new JsonResponseException(500, messageSources.get(provinceRes.getError()));
            }

            //如果存在默认地址
            if(notNull(provinceRes.getResult())){
                Map<String, Integer> freightFee = Maps.newHashMap();
                Long id;
                Integer count;
                List<String> params;
                //计算每个itemId:count的运费信息
                for (String itemInfo : itemsInfo) {
                    //解析商品编号&购买数量的关系
                    params = Splitter.on(':').omitEmptyStrings().trimResults().splitToList(itemInfo);
                    id = Long.parseLong(params.get(0));
                    count = Integer.parseInt(params.get(1));
                    freightFee.put(itemInfo , freightCountService.countDefaultFee(provinceRes.getResult(), id, count).getResult());
                }
                userFreightInfo.setFreightFees(freightFee);
            }

            return userFreightInfo;
        } else {
            log.error("failed to query trade information for user:{},error code :{}", userId, result.getError());
            throw new JsonResponseException(500, result.getError());
        }
    }

    @RequestMapping(value = "/user/tradeInfo/{tradeInfoId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable("tradeInfoId") Long userTradeInfoId) {
        Long userId = UserUtil.getUserId();
        Response<UserTradeInfo> result = userTradeInfoService.findById(userTradeInfoId);
        if (!result.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get("tradeinfo.not.exist"));
        }
        if (Objects.equal(userId, result.getResult().getUserId())) {
            Response<Boolean> ur = userTradeInfoService.delete(userTradeInfoId);
            if (ur.isSuccess()) {
                return "ok";
            } else {
                log.error("failed to delete userTradeInfo id={},cause:{}", userTradeInfoId, ur.getError());
                throw new JsonResponseException(500, messageSources.get(ur.getError()));
            }
        } else {
            throw new JsonResponseException(500, messageSources.get("tradeinfo.not.owner"));
        }
    }

    @RequestMapping(value = "/user/tradeInfo/{tradeInfoId}/invalidate", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void invalidate(@PathVariable("tradeInfoId") Long tradeInfoId) {
        Long userId = UserUtil.getUserId();
        Response<UserTradeInfo> result = userTradeInfoService.findById(tradeInfoId);
        if (!result.isSuccess()) {
            log.error("fail to find tradeInfo by id={}, error code:{}",tradeInfoId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        if (Objects.equal(userId, result.getResult().getUserId())) {
            Response<Boolean> ur = userTradeInfoService.invalidate(tradeInfoId);
            if(!ur.isSuccess()) {
                log.error("failed to invalidate userTradeInfo id={},cause:{}", tradeInfoId, ur.getError());
                throw new JsonResponseException(500, messageSources.get(ur.getError()));
            }
            return;
        }
        throw new JsonResponseException(500, messageSources.get("tradeinfo.not.owner"));
    }

    @RequestMapping(value = "/user/tradeInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserTradeInfo create(UserTradeInfo userTradeInfo) {
        Long userId = UserUtil.getUserId();
        userTradeInfo.setUserId(userId);
        Response<Long> result = userTradeInfoService.create(userTradeInfo);
        if (result.isSuccess()) {
            userTradeInfo.setId(result.getResult());
            return userTradeInfo;
        } else {
            log.error("failed to create {},cause:{}", userTradeInfo, result.getError());
            throw new JsonResponseException(500, result.getError());
        }
    }

    @RequestMapping(value = "/user/tradeInfo/{tradeInfoId}/default", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String makeAsDefault(@PathVariable("tradeInfoId") Long tradeInfoId) {
        Long userId = UserUtil.getUserId();
        Response<Boolean> result = userTradeInfoService.makeDefault(userId, tradeInfoId);
        if (result.isSuccess()) {
            return "ok";
        } else {
            log.error("failed to make tradeInfo(id={}),cause:{}", tradeInfoId, result.getError());
            throw new JsonResponseException(500, result.getError());
        }
    }

    @RequestMapping(value = "/user/tradeInfo/{tradeInfoId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long update(@PathVariable("tradeInfoId") Long id,
                       UserTradeInfo userTradeInfo) {
        Long userId = UserUtil.getUserId();
        userTradeInfo.setId(id);
        Response<Long> result = userTradeInfoService.update(userTradeInfo, userId);
        if (!result.isSuccess()) {
            log.error("failed to update userTradeInfo, id={},cause:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/user/tradeInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserTradeInfo findById(@RequestParam("tradeInfoId") Long id) {
        Response<UserTradeInfo> result = userTradeInfoService.findById(id);
        if (!result.isSuccess()) {
            log.error("failed to find userTradeInfo by id, id={},cause:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        BaseUser user = UserUtil.getCurrentUser();
        //只有卖家或者买家本人才能查看收获地址信息
        if (Objects.equal(user.getId(), result.getResult().getUserId()) || BaseUser.TYPE.SELLER.toNumber() == user.getType()) {
            return result.getResult();
        }
        throw new JsonResponseException(500, messageSources.get("authorized.fail"));
    }


    @RequestMapping(value = "/user/{userId}/tradeInfo/default", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserTradeInfo findDefault(@PathVariable("userId") Long userId) {
        Response<UserTradeInfo> result = userTradeInfoService.findDefault(userId);
        if (!result.isSuccess()) {
            log.error("failed to find userTradeInfo default by user id, id={},cause:{}", userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        BaseUser user = UserUtil.getCurrentUser();
        //只有卖家或者买家本人才能查看收获地址信息
        if (Objects.equal(user.getId(), result.getResult().getUserId()) || BaseUser.TYPE.SELLER.toNumber() == user.getType()) {
            return result.getResult();
        }
        throw new JsonResponseException(500, messageSources.get("authorized.fail"));
    }



    //供抢购下单使用
    /*
    1 选择是默认地址且属于当前区域
    2 选择属于当前区域中的一个地址
    3选择默认地址
    4以上都不满足就随便选择一个
    5如果一个地址也不存在 则显示为空
     */
    @RequestMapping(value = "/user/buying/tradeInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserTradeInfo findtradeInfoForBuying(HttpServletRequest request) {

        BaseUser user = UserUtil.getCurrentUser();

        Response<UserTradeInfo> result = userTradeInfoService.findDefault(user.getId()); //获取默认地址
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionIdR = gridService.findRegionFromCookie(cookieKVs);
        if (!regionIdR.isSuccess()) {
            log.error("fail to get region from cookie, error code:{}", regionIdR.getError());
            throw new JsonResponseException(500, messageSources.get(regionIdR.getError()));
        }
        if(notNull(result.getResult())){
            //如果默认地址所在区域和当前区域相等
            if(Objects.equal(result.getResult().getDistrictCode(),regionIdR.getResult())){
                return result.getResult();
            }
        }

        Response<List<UserTradeInfo>> listResponse = userTradeInfoService.findTradeInfoByUserAndDistrict(user.getId(),regionIdR.getResult());
        if (!listResponse.isSuccess()) {
            log.error("fail to get userTradeInfo by userId and districtId, error code:{}", listResponse.getError());
            throw new JsonResponseException(500, messageSources.get(listResponse.getError()));
        }
        if(listResponse.getResult()!=null&&listResponse.getResult().size()>0){
            return listResponse.getResult().get(0);//返回第一个当前区域的地址
        }

        if(notNull(result.getResult())){
                return result.getResult(); //返回默认地址
        }

        Response<List<UserTradeInfo>> tradeInfoListResonse = userTradeInfoService.findTradeInfosByUserId(user.getId());
        if (!tradeInfoListResonse.isSuccess()) {
            log.error("fail to get userTradeInfo by userId, error code:{}", tradeInfoListResonse.getError());
            throw new JsonResponseException(500, messageSources.get(tradeInfoListResonse.getError()));
        }
        if(tradeInfoListResonse.getResult()!=null&&tradeInfoListResonse.getResult().size()>0){
            return tradeInfoListResonse.getResult().get(0); //返回用户的一个地址
        }

        return null;
    }

}
