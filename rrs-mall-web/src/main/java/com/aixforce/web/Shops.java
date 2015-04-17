package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import com.aixforce.rrs.grid.service.ShopAuthorizeInfoService;
import com.aixforce.shop.dto.ShopInfoDto;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopExtraService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.site.model.Site;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.enums.Business;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.rrs.arrivegift.model.ShopGiftConfig;
import com.rrs.arrivegift.service.ShopGiftConfigService;
import com.rrs.brand.model.SmsConfigDto;
import com.rrs.brand.service.SmsConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.aixforce.common.utils.Arguments.*;

/**
 * User: yangzefeng Date: 13-11-6 Time: 下午1:56
 */
@Controller
@Slf4j
@RequestMapping("/api")
public class Shops {

	@Autowired
	private ShopService shopService;

	@Autowired
	private AccountService<User> accountService;

	@Autowired
	private MessageSources messageSources;

	@Autowired
	private SiteService siteService;

    @Autowired
    private ShopAuthorizeInfoService shopAuthorizeInfoService;
    
    @Autowired
	private SmsConfigService smsConfigService;

    private final static Set<Long> businessIds = Sets.newHashSet();

	@Autowired
	private ShopGiftConfigService shopGiftConfigService;

	@Autowired
	private ShopExtraService shopExtraService;

//	private final static Set<Long> businessIds = ImmutableSet.of(1L, 2L, 3L,
//			4L, 5L);
    @PostConstruct
    private void initBusinessId() {
        for(Business b : Business.values()) {
            businessIds.add(b.value());
        }
    }

	/**
	 * 用户申请开店
	 *
	 * @return Shop
	 */
	@RequestMapping(value = "/buyer/apply", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Shop apply(@RequestBody ShopInfoDto shopInfoDto) {
		Shop shop = shopInfoDto.getShop();

		BaseUser baseUser = UserUtil.getCurrentUser();
		if (Objects.equal(baseUser.getType(), BaseUser.TYPE.ADMIN.toNumber())) {
			log.error("admin don't need to apply for a shop anymore");
			throw new JsonResponseException(500,
					messageSources.get("already.admin"));
		}
		if (!businessIds.contains(shop.getBusinessId())) {
			log.error("unknown businessId {}", shop.getBusinessId());
			throw new JsonResponseException(500,
					messageSources.get("unknown.business.id"));
		}
		// 应海尔的要求，把店铺服务电话改成必填项，后台验证
		if (Strings.isNullOrEmpty(shop.getPhone())) {
			log.error("shop{} phone can not be null", shop);
			throw new JsonResponseException(500,
					messageSources.get("shop.phone.not.found"));
		}

		// 税务登记号为必填项
		if (Strings.isNullOrEmpty(shop.getTaxRegisterNo())) {
			log.error("shop{} tax no can not be empty", shop);
			throw new JsonResponseException(500,
					messageSources.get("shop.tax.no.not.found"));
		}

		// 税务登记号为必填项 登记号小于15或大于18或大于15小于18都为不合理状态
		if (shop.getTaxRegisterNo().length() != 15
				&& shop.getTaxRegisterNo().length() != 18
                && shop.getTaxRegisterNo().length() != 20) {
			log.error("shop{} taxNo should be 15 or 18 bit length", shop);
			throw new JsonResponseException(500,
					messageSources.get("shop.tax.no.len.not.correct"));
		}

		if (shop.getName().length() > 80) {
			throw new JsonResponseException(500,
					messageSources.get("shop.name.too.long"));
		}
		// 验证店铺名称是否存在
		Response<Shop> existName = shopService.findByName(shop.getName());
		if (existName.isSuccess() && existName.getResult() != null) {
			if (!Objects.equal(existName.getResult().getUserId(),
					baseUser.getId())) {
				log.warn("shop with name:{} Duplicate", shop.getName());
				throw new JsonResponseException(500,
						messageSources.get("shop.name.duplicate"));
			}
		}
		Response<Shop> exist = shopService.findByUserId(baseUser.getId());
		Response<Shop> result = new Response<Shop>();
		if (exist.isSuccess()) {
			// 如果已存在店铺，并且店铺的状态不是"审核不通过"，说明该用户已有店铺，不允许再次申请
			if (!Objects.equal(exist.getResult().getStatus(),
					Shop.Status.FAIL.value())) {
				log.warn("one user can only have one shop, userId={}",
						baseUser.getId());
				throw new JsonResponseException(
						messageSources.get("user.shop.exist"));
			} else if (Objects.equal(exist.getResult().getStatus(),
					Shop.Status.FAIL.value())) {
				shop.setId(exist.getResult().getId());
				shop.setUserId(baseUser.getId());
				shop.setUserName(baseUser.getName());
				shop.setStatus(Shop.Status.INIT.value());
				shop.setIsCod(Boolean.TRUE);

				// 审核不通过情况下再次申请：可以更改证件信息
				shopInfoDto.getShopPaperwork().setShopId(shop.getId());

				Response<Boolean> shopR = shopService.update(shop,
						shopInfoDto.getShopPaperwork());
				if (!shopR.isSuccess()) {
					log.error(
							"fail to update shop when apply shop again,shop:",
							shop);
					throw new JsonResponseException(500,
							messageSources.get(shopR.getError()));
				}
				result = shopService.findById(exist.getResult().getId());
			}
		} else {
			shop.setUserId(baseUser.getId());
			shop.setUserName(baseUser.getName());
			shop.setStatus(Shop.Status.INIT.value());
			shop.setIsCod(Boolean.TRUE);

			// 添加对于证件的添加逻辑
			result = shopService.create(shop, shopInfoDto.getShopPaperwork());
			if (!result.isSuccess()) {
				log.error("fail to apply shop ( {}), cause:{}", shop,
						result.getError());
				throw new JsonResponseException(500, messageSources.get(result
						.getError()));
			}
		}
		User user = new User();
		user.setId(baseUser.getId());
		user.setTags("1");
		Response<Boolean> ur = accountService.updateUser(user);
		if (!ur.isSuccess()) {
			log.error("failed to set user(id={}) tags to 1, error code:{}",
					user.getId(), ur.getError());
		}
		return result.getResult();
	}

	@RequestMapping(value = "/seller/shop/edit", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String editShop(Shop shop,
            @RequestParam(value = "isSms") String isSms,
			@RequestParam(value = "enable") String enable,
			@RequestParam(value = "weekday") String weekday,
			@RequestParam(value = "amstart") String amstart,
			@RequestParam(value = "amend") String amend,
			@RequestParam(value = "pmstart") String pmstart,
			@RequestParam(value = "pmend") String pmend,
            @RequestParam(value = "isStorePay",required = false) String isStorePay) {
		Response<Boolean> result;
		Long userId = UserUtil.getUserId();
		Response<Shop> exist = shopService.findByUserId(userId);
		if (!exist.isSuccess()) {
			log.error("failed to find shop by userId={}, error code ", userId);
			throw new JsonResponseException(500, messageSources.get(exist
					.getError()));
		}
		// 验证店铺名称是否存在,只会找状态不为Fail的店铺
		Response<Shop> existName = shopService.findByName(shop.getName());
		if (existName.isSuccess() && existName.getResult() != null
				&& !Objects.equal(existName.getResult().getUserId(), userId)) {
			log.warn("shop with name:{} Duplicate", shop.getName());
			throw new JsonResponseException(500,
					messageSources.get("shop.name.duplicate"));
		}
		shop.setId(exist.getResult().getId());
		shop.setBusinessId(null); // 暂不允许修改businessId

        //货到付款使用复选框后。不为null时支持货到付款
        if(isNull(shop.getIsCod())){
            shop.setIsCod(Boolean.FALSE);
        }else{
            shop.setIsCod(Boolean.TRUE);
        }
        result = shopService.update(shop);



        //是否接受短信通知 add by zf 2014-10-10
        SmsConfigDto smsConfigCndDto = new SmsConfigDto();
		smsConfigCndDto.setUserId(String.valueOf(userId));
		smsConfigCndDto.setEnable(isSms);
		//1: 个人 2:卖家
		smsConfigCndDto.setUserType("2");

        if (!result.isSuccess()) {
            log.error("failed to update shop by seller sellerId={},cause:{}",
                    userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result
                    .getError()));
        }

		try {
			 smsConfigService.updateSmsConfig(smsConfigCndDto);
		} catch (Exception e) {
            // Ignore possible exception on purpose.
            log.warn("failed to update shop sms configuration.", e);
//			 log.error("failed to update shop smsConfig by seller sellerId={},cause:{}", userId, result.getError());
//	         throw new JsonResponseException(500, messageSources.get(result.getError()));
		}


		// 是否支持到店有礼		
		Response<ShopGiftConfig> shopGiftService = shopGiftConfigService
				.findShopGift(exist.getResult().getId());
		weekday =weekday.replace(",","");
			
		ShopGiftConfig shopGiftConfig = new ShopGiftConfig();
		shopGiftConfig.setUserid(userId);
		shopGiftConfig.setShopid(exist.getResult().getId());
		shopGiftConfig.setEnable(Integer.valueOf(enable).intValue()); 
		shopGiftConfig.setWeekday(weekday);
		shopGiftConfig.setAmstart(amstart);
		shopGiftConfig.setAmend(amend);
		shopGiftConfig.setPmstart(pmstart);		
		shopGiftConfig.setPmend(pmend);
        try {
            if (null == shopGiftService.getResult()
                    || null == shopGiftService.getResult().getShopid()) {
                shopGiftConfigService.insertShopGift(shopGiftConfig);
            } else {
                shopGiftConfigService.updateShopGift(shopGiftConfig);
            }
        } catch (Exception e) {
            // Ignore possible exception on purpose.
            log.warn("failed to update shop gift configuration.", e);
        }

        //是否支持到店支付
        ShopExtra shopExtra = new ShopExtra();
        shopExtra.setShopId(shop.getId());
        if(isNull(isStorePay) || isEmpty(isStorePay)){//为null或空，不支持到店支付
            shopExtra.setIsStorePay(Boolean.FALSE);
        }else {
            shopExtra.setIsStorePay(Boolean.TRUE);
        }

        try{
            Response<ShopExtra> extraExist = shopExtraService.findByShopId(shop.getId());
            // 若已经存在则更新，否则插入
            if (notNull(extraExist)) {
                shopExtraService.updateIsStorePayByShopId(shopExtra);
            }else{
                shopExtra.setRate(0.0000);
                shopExtra.setDepositNeed(0L);
                shopExtra.setTechFeeNeed(0L);
                shopExtra.setCreatedAt(new Date());
                shopExtra.setUpdatedAt(new Date());
                shopExtraService.create(shopExtra);
            }
        }catch (Exception ex){
            log.error("failed to update shopExtra for isStorePay by shopid is {} .error code :{}",shop.getId(),ex);
        }


		return "ok";
	}

	/**
	 * 验证店铺名称唯一
	 *
	 * @param name
	 *            店铺名称
	 * @param operation
	 *            1为创建时验证，2为修改时验证
	 * @return 是否存在
	 */
	@RequestMapping(value = "/user/verifyShop", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Boolean verifyName(
			@RequestParam("name") String name,
			@RequestParam(value = "operation", defaultValue = "1") Integer operation) {
		Response<Shop> exist = shopService.findByName(name);
		Long userId = UserUtil.getUserId();
		if (!Objects.equal(operation, 1) && !Objects.equal(operation, 2)) {
			throw new JsonResponseException("unknown operation");
		}
		if (Objects.equal(operation, 1)) {
			if (exist.isSuccess()
					&& !Objects.equal(exist.getResult().getUserId(), userId)) {
				log.warn("shop name exist, name={}", name);
				return false;
			}
		} else {
			if (exist.isSuccess()
					&& !Objects.equal(exist.getResult().getUserId(), userId)) {
				log.warn("shop name exist, name={}", name);
				return false;
			}
		}
		return true;
	}

	@RequestMapping(value = "/seller/shopAuthorizeInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<ShopAuthorizeInfo> findByShopId() {
		Long userId = UserUtil.getUserId();
		Response<Shop> shopR = shopService.findByUserId(userId);
		if (!shopR.isSuccess()) {
			log.error("failed to find shop by userId={}, error code:{}",
					userId, shopR.getError());
			throw new JsonResponseException(500, "您的店铺未找到");
		}
		Long shopId = shopR.getResult().getId();
		Response<List<ShopAuthorizeInfo>> result = shopAuthorizeInfoService
				.findByShopId(shopId);
		if (!result.isSuccess()) {
			log.error(
					"failed to find authorizeInfo by shopId={}, error code:{}",
					shopId, result.getError());
			throw new JsonResponseException(500, messageSources.get(result
					.getError()));
		}
		return result.getResult();
	}

	@RequestMapping(value = "/seller/domain", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String findShopDomain() {
		Long userId = UserUtil.getUserId();
		Response<Site> siteR = siteService.findShopByUserId(userId);
		if (!siteR.isSuccess()) {
			log.error("fail to find shop site by userId={},error code:{}",
					userId, siteR.getError());
			throw new JsonResponseException(500, messageSources.get(siteR
					.getError()));
		}
		Site site = siteR.getResult();
		return site.getSubdomain();
	}

	@RequestMapping(value = "/shop/items/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Long findShopItemCount(@RequestParam("sellerId") Long sellerId) {
		if (sellerId == null) {
			throw new JsonResponseException(500,
					messageSources.get("illegal.param"));
		}
		Response<Shop> shopR = shopService.findByUserId(sellerId);
		if (!shopR.isSuccess()) {
			log.error("fail to find shop by sellerId={}, error code:{}",
					sellerId, shopR.getError());
			throw new JsonResponseException(500, messageSources.get(shopR
					.getError()));
		}
		Long shopId = shopR.getResult().getId();
		Response<Long> countR = shopService.getItemCountByShopId(shopId);
		if (!countR.isSuccess()) {
			log.error("fail to get item count by shopId={},error code:{}",
					shopId, countR.getError());
			throw new JsonResponseException(500, messageSources.get(countR
					.getError()));
		}
		Long count = countR.getResult();
		return count > 0 ? count : 0;
	}

	@ResponseBody
	@RequestMapping(value = "/seller/findShopByCurrentUser", method = RequestMethod.GET)
	public  Response<Shop> findShopByCurrentUser() {
		Long userId = UserUtil.getUserId();
		Response<Shop> shopR = shopService.findByUserId(userId);
		return shopR;
	}
}
