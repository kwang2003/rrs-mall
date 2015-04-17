package com.aixforce.open.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.open.dto.RichCoupons;
import com.aixforce.open.util.NSSessionUID;
import com.aixforce.open.util.RequestUtils;
import com.aixforce.open.util.Signatures;
import com.aixforce.session.AFSession;
import com.aixforce.session.AFSessionManager;
import com.aixforce.shop.service.ChannelShopsService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rrs.coupons.model.*;
import com.rrs.coupons.service.CouponsRrsService;
import com.rrs.coupons.service.LqCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.isEmpty;
import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 营销
 *
 * Created by jack.yang on 14-9-5.
 */
@Slf4j
@Controller
@RequestMapping("/api/open/promo")
public class PromoController {

    @Autowired
    private ChannelShopsService channelShopsService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    LqCouponService lqCouponService;

    @Autowired
    CouponsRrsService couponsRrsService;

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    /**
     * 抢优惠券
     * @param uid 用户ID
     * @param couponid 优惠券ID
     * @param sessionId 会话ID
     * @param channel 频道ID
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 抢券结果
     */
    @RequestMapping(value = "/coupons/forestall", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Map> forestallCoupons(@RequestParam(value="uid", required = true) Long uid,
                                                   @RequestParam(value = "couponid", required = true) Long couponid,
                                                   @RequestParam(value = "session", required = true) String sessionId,
                                                   @RequestParam(value = "channel", required = true) String channel,
                                                   @RequestParam(value = "sign", required = true) String sign,
                                                   HttpServletRequest request) {
        log.info("/coupons/forestall accepted channel :{}, ip:{}, uid:{}, couponid:{}",
                channel, RequestUtils.getIpAddr(request), uid, couponid);

        Response<Map> result = new Response<Map>();

        try{

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
            checkState(idGet.isSuccess(), idGet.getError());

            Map map = Maps.newConcurrentMap();

            BaseUser baseUser = new BaseUser();
            baseUser.setId(uid);
            LqMessage lqMessage = lqCouponService.LqCoupon(baseUser, couponid.intValue());

            map.put("lqMsg", lqMessage);

            if (Objects.equal(lqMessage.getStatus(), "4")) {
                Response<RrsCou> rrsCouResponse = couponsRrsService.queryCouponsById(couponid);
                checkState(rrsCouResponse.isSuccess(),rrsCouResponse.getError());
                map.put("couponsInfo", rrsCouResponse.getResult());
            }


            result.setResult(map);

        } catch (IllegalArgumentException e) {
            log.error("fail to forestall coupons with channel :{}, ip:{}, uid:{}, couponid:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), uid, couponid, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to forestall coupons with channel :{}, ip:{}, uid:{}, couponid:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), uid, couponid, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to forestall coupons with channel :{}, ip:{}, uid:{}, couponid:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), uid, couponid, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.forestall.coupons"));
        }
        return result;
    }


    /**
     * 获取用户的优惠券
     * @param uid 用户ID
     * @param skus 以json格式保存的Map<Long,Integer> key为skuId，value为sku数量
     * @param sessionId 会话ID
     * @param channel 频道ID
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 用户的优惠券列表
     */
    @RequestMapping(value = "/coupons/user/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<RichCoupons>> getCouponsByUser(@PathVariable Long uid,
                                                   @RequestParam(value = "skus", required = false) String skus,
                                                   @RequestParam(value = "session", required = true) String sessionId,
                                                   @RequestParam(value = "channel", required = true) String channel,
                                                   @RequestParam(value = "sign", required = true) String sign,
                                                   HttpServletRequest request) {
        log.info("/coupons/user accepted channel :{}, ip:{}, uid:{}, skus:{}",
                channel, RequestUtils.getIpAddr(request), uid, skus);

        Response<List<RichCoupons>> result = new Response<List<RichCoupons>>();

        try{

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
            checkState(idGet.isSuccess(), idGet.getError());


            List<RichCoupons> richCouponsList = new ArrayList<RichCoupons>();

            BaseUser baseUser = new BaseUser();
            baseUser.setId(uid);
            if (isEmpty(skus)) {
                Response<List<RrsShowCouponView>> listResponse = couponsRrsService.getCouponByUser(baseUser);
                checkState(listResponse.isSuccess(), listResponse.getError());
                for (RrsShowCouponView rrsShowCouponView : listResponse.getResult()) {
                    RichCoupons richCoupons = BeanMapper.map(rrsShowCouponView, RichCoupons.class);
                    richCoupons.setCouponId(rrsShowCouponView.getId());
                    richCouponsList.add(richCoupons);
                }
            } else {
                Response<List<RrsCouUserView>> listResponse = couponsRrsService.preCouponsBySku(skus, baseUser);
                checkState(listResponse.isSuccess(), listResponse.getError());
                for (RrsCouUserView rrsCouUserView : listResponse.getResult()) {
                    RichCoupons richCoupons = BeanMapper.map(rrsCouUserView, RichCoupons.class);
                    richCouponsList.add(richCoupons);
                }
            }

            result.setResult(richCouponsList);
        } catch (IllegalArgumentException e) {
            log.error("fail to get coupons by user with channel :{}, ip:{}, uid:{}, skus:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), uid, skus, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get coupons by user with channel :{}, ip:{}, uid:{}, skus:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), uid, skus, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get coupons by user with channel :{}, ip:{}, uid:{}, skus:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), uid, skus, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.coupons.by.user"));
        }
        return result;
    }


    /**
     *  获取所有有效优惠券
     * @param channel 频道ID
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 所有有效优惠券
     */
    @RequestMapping(value = "/coupons/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<LqCouponView>> getAllCoupons(@RequestParam(value = "channel", required = true) String channel,
                                                        @RequestParam(value = "sign", required = true) String sign,
                                                        HttpServletRequest request) {
        log.info("/coupons/all accepted channel :{}, ip:{}",
                channel, RequestUtils.getIpAddr(request));

        Response<List<LqCouponView>> result = new Response<List<LqCouponView>>();

        try{

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            result = lqCouponService.findCouponAll();
            checkState(result.isSuccess(), result.getError());

        } catch (IllegalArgumentException e) {
            log.error("fail to get all coupons with channel :{}, ip:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get all coupons with channel :{}, ip:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get all coupons with channel :{}, ip:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.all.coupons"));
        }
        return result;
    }


    /**
     *  根据优惠券ID获取详细信息
     * @param cid 优惠券ID
     * @param channel 频道ID
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 优惠券详细信息
     */
    @RequestMapping(value = "/coupons/{cid}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<RrsCou> getAllCoupons(@PathVariable Long cid,
                                                        @RequestParam(value = "channel", required = true) String channel,
                                                        @RequestParam(value = "sign", required = true) String sign,
                                                        HttpServletRequest request) {
        log.info("/coupons/{}/info accepted channel :{}, ip:{}",
                cid, channel, RequestUtils.getIpAddr(request));

        Response<RrsCou> result = new Response<RrsCou>();

        try{

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            result = couponsRrsService.queryCouponsById(cid);
            checkState(result.isSuccess(), result.getError());

        } catch (IllegalArgumentException e) {
            log.error("fail to get coupons info with channel :{}, ip:{}, cid:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), cid, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get coupons info with channel :{}, ip:{}, cid:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), cid, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get coupons info with channel :{}, ip:{}, cid:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), cid, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.coupons.info"));
        }
        return result;
    }


    /**
     * 店铺商品抢购订单列表
     * @param shopId 店铺ID
     * @param begin 开始时间（格式为yyyymmddHHmmss），若为空则表示无起始日期
     * @param end 截止时间，若为空则表示无截止时间
     * @param pageNo 分页页码，从1开始，不传默认为1
     * @param size 分页大小，如果不传默认50条
     * @param channel 频道ID
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 订单列表
     */
    @RequestMapping(value = "/dtd/shop/{shopId}/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Paging<Map>> dtdOrdersByShopId(@PathVariable Long shopId,
                                              @RequestParam(value = "begin", required = false) String begin,
                                              @RequestParam(value = "end", required = false) String end,
                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                              @RequestParam(value = "size", required = false) Integer size,
                                              @RequestParam(value = "channel", required = true) String channel,
                                              @RequestParam(value = "sign", required = true) String sign,
                                              HttpServletRequest request) {
        log.info("/dtd/shop/{shopId}/orders accepted channel :{}, ip:{}, begin:{}, end:{}, pageNo:{}, size:{}",
                shopId, channel, RequestUtils.getIpAddr(request), begin, end, pageNo, size);

        Response<Paging<Map>> result = new Response<Paging<Map>>();

        try{

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Map map = Maps.newConcurrentMap();
            map.put("id",29);
            map.put("uid",133333331);
            map.put("tradeUser","五来");
            map.put("mobile","13333333333");
            map.put("tradeAddress","山东省青岛市崂山区");
            map.put("itemId",2345);
            map.put("amount",1);
            map.put("createAt","2014-09-15 23:00:00");


            List list = Lists.newArrayList();
            list.add(map);

            Paging<Map> paging = new Paging<Map>(0L, list);

            result.setResult(paging);

        } catch (IllegalArgumentException e) {
            log.error("fail to get dtd shop orders with channel :{}, ip:{}, shopId:{}, begin:{}, end:{}, pageNo:{}, size:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), shopId, begin, end, pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get dtd shop orders with channel :{}, ip:{}, shopId:{}, begin:{}, end:{}, pageNo:{}, size:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), shopId, begin, end, pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get dtd shop orders with channel :{}, ip:{}, shopId:{}, begin:{}, end:{}, pageNo:{}, size:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), shopId, begin, end, pageNo, size, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.dtd.shop.orders"));
        }
        return result;
    }

}
