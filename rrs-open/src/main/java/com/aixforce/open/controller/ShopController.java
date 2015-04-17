package com.aixforce.open.controller;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.item.service.ItemService;
import com.aixforce.open.dto.HaierResponse;
import com.aixforce.open.util.Channel;
import com.aixforce.open.util.RequestUtils;
import com.aixforce.open.util.Signatures;
import com.aixforce.shop.dto.RichShop;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ChannelShopsService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.model.Address;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.model.UserExtra;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.service.AddressService;
import com.aixforce.user.service.UserExtraService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * 店铺
 *
 * Created by yangjingang on 14-8-4.
 */
@Slf4j
@Controller
@RequestMapping("/api/open/shop")
public class ShopController {

    @Autowired
    private ChannelShopsService channelShopsService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private AddressService addressService;

    @Autowired
    UserExtraService userExtraService;

    @Autowired
    ItemService itemService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    AccountService accountService;

    // 2:卖家
    private static final int USER_TYPE_SELLER = 2;

    /**
     *  根据店铺Id或者外部编码获得店铺信息
     * @param channel 渠道id
     * @param shopId 店铺id（可选）
     * @param outerCode 外部编码（可选）
     * @param loginId   登录凭证(登录名|邮箱|手机), （可选，第三方必填）
     * @param password  密码, （可选，第三方必填）
     * @param type      登录类型 1:邮箱 2:手机 3:登录名, （可选，第三方必填）
     * @param sign 签名
     * @param request 请求对象
     * @return 店铺信息
     */
    @RequestMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<RichShop> baseInfo(@RequestParam(value="channel", required = true) String channel,
                                                  @RequestParam(value="shopId", required = false) String shopId,
                                                  @RequestParam(value="outerCode", required = false) String outerCode,
                                                  @RequestParam(value="loginId", required = false) String loginId,
                                                  @RequestParam(value="password", required = false) String password,
                                                  @RequestParam(value = "type", defaultValue = "1", required = false) Integer type,
                                                  @RequestParam(value="sign", required = true) String sign,
                                                  HttpServletRequest request){

        log.info("/info accepted request with channel:{}, ip:{}, shopId:{}, outerCode:{}", channel, RequestUtils.getIpAddr(request), shopId, outerCode);

        HaierResponse<RichShop> result = new HaierResponse<RichShop>();
        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            String method = Thread.currentThread().getStackTrace()[1].getClassName()+'.'+Thread.currentThread().getStackTrace()[1].getMethodName();

            Response<Boolean> authResult = channelShopsService.isAuthRole(method, channel);
            checkState(authResult.isSuccess(), authResult.getError());

            // 检查是否需要授权验证
            if (authResult.getResult()) {

                LoginType loginType = LoginType.from(type);
                checkArgument(notNull(loginType), "incorrect.login.type");

                Response<User> userResult = accountService.userLogin(loginId, loginType, password);
                checkState(userResult.isSuccess(), userResult.getError());

                // 必须为店铺账号
                User user = userResult.getResult();
                checkArgument(user.getType() == USER_TYPE_SELLER, "no.auth.to.access");

                // 根据USER ID获取店铺信息
                Response<Shop> shopResponse = shopService.findByUserId(user.getId());
                checkState(shopResponse.isSuccess(), shopResponse.getError());

                shopId = String.valueOf(shopResponse.getResult().getId());
            }

            // 根据店铺id查询
            if (notEmpty(shopId)) {

                Response<Shop> shopGetResult = shopService.findById(Long.parseLong(shopId));
                checkState(shopGetResult.isSuccess(), shopGetResult.getError());

                Shop shop = shopGetResult.getResult();
                RichShop richShop = BeanMapper.map(shop, RichShop.class);

                Response<List<Address>> regionGetResult = addressService.ancestorOfAddresses(shop.getRegion());
                checkState(regionGetResult.isSuccess(), regionGetResult.getError());
                setShopAddress(richShop, regionGetResult.getResult());

                Response<UserExtra> extraGetResult = userExtraService.findByUserId(shop.getUserId());
                checkState(extraGetResult.isSuccess(), extraGetResult.getError());
                UserExtra userExtra = extraGetResult.getResult();

                richShop.setSoldQuantity(Objects.firstNonNull(userExtra.getTradeQuantity(), 0));
                richShop.setSale(Objects.firstNonNull(userExtra.getTradeSum(), 0L));

                Response<Long> countResult = itemService.countOnShelfByShopId(Long.parseLong(shopId));
                checkState(countResult.isSuccess(), countResult.getError());
                richShop.setItemCount(countResult.getResult().intValue());

                result.setResult(richShop);
                // 根据外部编码查询店铺信息
            } else if (notEmpty(outerCode)){

                Response<List<Shop>> listResponse = shopService.findByOuterCode(outerCode);
                checkState(listResponse.isSuccess(), listResponse.getError());

                Response<List<Long>> bussinessIdsResult = channelShopsService.findBusinessIds(channel);
                checkState(bussinessIdsResult.isSuccess(), "fail.to.get.business.info");

                List<Shop> shopList = listResponse.getResult();
                RichShop richShop = null;
                for (int i=0;i<shopList.size();i++) {
                    Shop shop = shopList.get(i);

                    List<Long> bussinessIds =bussinessIdsResult.getResult();
                    // 筛选店铺类目
                    if (-1 == bussinessIds.indexOf(shop.getBusinessId())) {
                        continue;
                    }
                    richShop = BeanMapper.map(shop, RichShop.class);

                    Response<List<Address>> regionGetResult = addressService.ancestorOfAddresses(shop.getRegion());
                    // checkState(regionGetResult.isSuccess(), regionGetResult.getError());
                    // 没有地区信息的话，不显示
                    if (regionGetResult.isSuccess()) {
                        setShopAddress(richShop, regionGetResult.getResult());
                    }

                    Response<UserExtra> extraGetResult = userExtraService.findByUserId(shop.getUserId());
                    checkState(extraGetResult.isSuccess(), extraGetResult.getError());
                    UserExtra userExtra = extraGetResult.getResult();

                    richShop.setSoldQuantity(Objects.firstNonNull(userExtra.getTradeQuantity(), 0));
                    richShop.setSale(Objects.firstNonNull(userExtra.getTradeSum(), 0L));

                    Response<Long> countResult = itemService.countOnShelfByShopId(richShop.getId());
                    checkState(countResult.isSuccess(), countResult.getError());
                    richShop.setItemCount(countResult.getResult().intValue());

                    break;
                }
                checkNotNull(richShop, "no data found!");

                result.setResult(richShop);

            } else {
                throw new IllegalStateException("params cant be empty!");
            }

        } catch (IllegalArgumentException e) {
            log.error("failed to get shop info with channel:{}, ip:{}, shopId:{}, outerCode:{}, error:{}", channel, RequestUtils.getIpAddr(request), shopId, outerCode, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to get shop info with channel:{}, ip:{}, shopId:{}, outerCode:{}, error:{}", channel, RequestUtils.getIpAddr(request), shopId, outerCode, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to get shop info with channel:{}, ip:{}, shopId:{}, outerCode:{}, error:{}", channel, RequestUtils.getIpAddr(request), shopId, outerCode, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.shop.info"));
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
}
