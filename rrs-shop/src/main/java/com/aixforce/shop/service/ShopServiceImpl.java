package com.aixforce.shop.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.search.ESClient;
import com.aixforce.shop.dao.ShopDao;
import com.aixforce.shop.dao.ShopExtraDao;
import com.aixforce.shop.dao.ShopPaperworkDao;
import com.aixforce.shop.dao.redis.ShopRedisDao;
import com.aixforce.shop.dto.RichShop;
import com.aixforce.shop.dto.ShopDto;
import com.aixforce.shop.dto.ShopInfoDto;
import com.aixforce.shop.dto.ShopSidebar;
import com.aixforce.shop.event.ApprovePassEvent;
import com.aixforce.shop.event.ShopEventBus;
import com.aixforce.shop.manager.ShopManager;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.model.ShopPaperwork;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.Address;
import com.aixforce.user.model.UserProfile;
import com.aixforce.user.service.AddressService;
import com.aixforce.user.service.UserProfileService;
import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rrs.arrivegift.model.ShopGiftConfig;
import com.rrs.arrivegift.model.Weekday;
import com.rrs.arrivegift.service.ShopGiftConfigService;
import com.rrs.brand.model.BrandRlView;
import com.rrs.brand.model.SmsConfigDto;
import com.rrs.brand.service.BrandClubService;
import com.rrs.brand.service.BrandRlService;
import com.rrs.brand.service.SmsConfigService;

import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Objects.equal;
import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author: <a href="mailto:jlchen.cn@gmail.com">jlchen</a> Date: 2013-10-28
 */
@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

	@Autowired
	private ESClient esClient;

	@Autowired
	private ShopDao shopDao;

	@Autowired
	private ShopExtraDao shopExtraDao;

	@Autowired
	private ShopPaperworkDao shopPaperworkDao;

	@Autowired
	private AddressService addressService;

	@Autowired
	private ShopRedisDao shopRedisDao;

	@Autowired
	private ShopManager shopManager;

	@Autowired
	private ShopEventBus shopEventBus;

	@Autowired
	private BrandRlService brandRlService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ShopGiftConfigService shopGiftConfigService;

    @Value("#{app.eHaierSellerId}")
    private String eHaierSellerId;

	@Autowired
	private SmsConfigService smsConfigService;

	@Autowired
	private BrandClubService brandClubService;


	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat
			.forPattern("yyyy-MM-dd HH:mm:ss");
	private static final int PAGE_SIZE = 200;
	private static final Splitter commaSplitter = Splitter.on(",")
			.trimResults().omitEmptyStrings();
	private static final Splitter underscoreSplitter = Splitter.on("_")
			.trimResults().omitEmptyStrings();

	private final static String SHOP_INDEX_NAME = "shops";
	private final static String SHOP_INDEX_TYPE = "shop";

	/**
	 * 查询指定状态的店铺
	 *
	 * @param statuses
	 *            状态集合
	 * @param pageNo
	 *            分页
	 * @param size
	 *            每页数据量
	 * @return 用户分页信息
	 */
	@Override
	public Response<Paging<Shop>> findBy(List<Integer> statuses,
			Integer pageNo, Integer size) {
		Response<Paging<Shop>> result = new Response<Paging<Shop>>();

		try {
			PageInfo pageInfo = new PageInfo(pageNo, size);
			Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
			params.put("statuses", statuses);
			params.put("offset", pageInfo.offset);
			params.put("limit", pageInfo.limit);

			Paging<Shop> paging = shopDao.findBy(params);
			result.setResult(paging);

		} catch (Exception e) {
			log.error(
					"fail to query shop with statuses:{}, pageNo:{}, size:{}",
					statuses, pageNo, size, e);
			result.setError("shop.query.fail");
		}
		return result;
	}

	/**
	 * 获取品牌认领列表
	 * 
	 * @param params
	 * @param pageNo
	 * @param size
	 * @return
	 */
	@Override
	public Response<Paging<ShopDto>> findShopsByBrand(
			@ParamInfo("params") Map<String, String> params,
			@ParamInfo("pageNo") Integer pageNo, @ParamInfo("size") Integer size) {
		PageInfo pageInfo = new PageInfo(pageNo, size);
		Response<Paging<ShopDto>> result = new Response<Paging<ShopDto>>();
		try {
			int brandClubKey = Integer.valueOf(params.get("brandClubKey"));
			Response<List<BrandRlView>> viewList = brandRlService
					.findRl(brandClubKey);
			List<Integer> ids = new ArrayList<Integer>();
			for (BrandRlView view : viewList.getResult()) {
				ids.add(view.getShopId());
			}
			if (ids.size() == 0) {
				ids.add(-1);
			}
			Paging<Shop> shops = shopDao.brandShops(pageInfo.getOffset(),
					pageInfo.getLimit(), ids);
			List<ShopDto> shopDtos = transToShopDto(shops.getData());
			result.setResult(new Paging<ShopDto>(shops.getTotal(), shopDtos));
			return result;
		} catch (NullPointerException e) {
			log.error("address.query.fail", e);
			result.setError("address.query.fail");
			return result;
		} catch (Exception e) {
			log.error("find shop fail", e);
			result.setError("shop.query.fail");
			return result;
		}
	}

	/**
	 * 根据店铺id查找店铺
	 *
	 * @param shopId
	 *            店铺id
	 * @return 查找结果
	 */
	@Override
	public Response<Shop> findById(@Nonnull Long shopId) {
		Response<Shop> result = new Response<Shop>();
		try {
			Shop shop = shopDao.findById(shopId);
			if (shop == null) {
				log.error("no shop(id={}) found", shopId);
				result.setError("shop.not.found");
				return result;
			}
			result.setResult(shop);
			return result;
		} catch (Exception e) {
			log.error("failed to find shop where id = {},cause:{}", shopId,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.query.fail");
			return result;
		}
	}

	/**
	 * 根据卖家id查找店铺
	 *
	 * @param userId
	 *            卖家id
	 * @return 查找结果
	 */
	@Override
	public Response<Shop> findByUserId(@Nonnull Long userId) {
		Response<Shop> result = new Response<Shop>();
		try {
			Shop shop = shopDao.findByUserId(userId);
			if (shop == null) {
				result.setError("shop.not.found");
				return result;
			}
			result.setResult(shop);
			return result;
		} catch (Exception e) {
			log.error("failed to find shop where userId = {},cause:{}", userId,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.query.fail");
			return result;
		}
	}

	// 包括证件信息（用于前台显示店铺详细信息）
	@Override
	public Response<ShopInfoDto> findByUser(BaseUser user) {
		Response<ShopInfoDto> result = new Response<ShopInfoDto>();

		try {
			ShopInfoDto shopInfoDto = new ShopInfoDto();
			// 获取店铺信息
			Shop shop = shopDao.findByUserId(user.getId());
			checkState(notNull(shop), "shop.not.found");
			// 是否是ehaier商家
			shopInfoDto.setIsEhaier(Objects.equal(eHaierSellerId, shop
					.getUserId().toString()));

			// 获取店铺证件信息
			shopInfoDto.setShop(shop);
			shopInfoDto.setShopPaperwork(shopPaperworkDao.findByShopId(shop
					.getId()));

            // 获取店铺扩展信息
            ShopExtra extra = shopExtraDao.findByShopId(shop.getId());
            checkState(notNull(extra), "shop.extra.not.found");
            shopInfoDto.setExtra(extra);
            
         // 获取店点有礼信息		
            Response<ShopGiftConfig> shopGiftCog = shopGiftConfigService.findShopGift(shop.getId());
            checkState(notNull(shopGiftCog), "shop.shopGiftCog.not.found");
            ShopGiftConfig shopGift =null;
            if(null !=shopGiftCog.getResult()){
            	Weekday weekdayBean = new Weekday();			
            	String weekDay=shopGiftCog.getResult().getWeekday();
                System.err.println("==weekDay==:"+weekDay);
                
                char[] dayarr=weekDay.toCharArray();
                for(char day:dayarr){	
        			switch (day) {
        			case '1':
        				weekdayBean.setMon(String.valueOf(day));
        				break;
        			case '2':
        				weekdayBean.setTue(String.valueOf(day));
        				break;
        			case '3':
        				weekdayBean.setWed(String.valueOf(day));
        				break;
        			case '4':
        				weekdayBean.setThu(String.valueOf(day)); 
        				break;
        			case '5':
        				weekdayBean.setFri(String.valueOf(day));
        				break;
        			case '6':
        				weekdayBean.setSat(String.valueOf(day));
        				break;
        			case '7':
        				weekdayBean.setSun(String.valueOf(day));
        				break;
        			default:
        				break;
        			}
        		}			
            	shopGift =shopGiftCog.getResult();
            	shopGift.setWeekdayBean(weekdayBean);
            }else{		
            	shopGift = new ShopGiftConfig();
            	shopGift.setShopid(shop.getId());
            	shopGift.setUserid(user.getId());
            	shopGift.setEnable(0);	
            }					
            shopInfoDto.setShopGiftConfig(shopGift);

			// 查询卖家是否接收短信通知 add by zf 2014-10-10
			Response<SmsConfigDto> smsConfigDto = smsConfigService
					.selectSmsConfig(user);
			if (null != smsConfigDto.getResult()) {
				shopInfoDto.setIsSms(smsConfigDto.getResult().getEnable());
			} else {
				shopInfoDto.setIsSms("1");
			}
			// add end

            //显示到店支付：如果是广场店，则显示到店支付。0 不显示 1显示
            Response<Long> shopid = brandClubService.findStorePay(user.getId());
            if(shopid.isSuccess() && !isNull(shopid.getResult()) && shopid.getResult() > 0){
                shopInfoDto.setDisplayStorePay("1");
            }else{
                shopInfoDto.setDisplayStorePay("0");
            }

			result.setResult(shopInfoDto);
		} catch (Exception e) {
			log.error("failed to find shop where userId = {},cause:{}",
					user.getId(), Throwables.getStackTraceAsString(e));
			result.setError("shop.query.fail");
		}

		return result;
	}

	@Override
	public Response<Shop> findByName(@Nonnull String name) {
		Response<Shop> result = new Response<Shop>();
		try {
			Shop shop = shopDao.findByName(name);
			if (shop == null) {
				result.setError("shop.not.found");
				return result;
			}
			result.setResult(shop);
			return result;
		} catch (Exception e) {
			log.error("failed to find shop where name = {},cause:{}", name,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.query.fail");
			return result;
		}
	}

	/**
	 * 创建店铺
	 *
	 * @param shop
	 *            待创建的店铺
	 * @return 设置了id的shop
	 */
	@Override
	public Response<Shop> create(@Nonnull Shop shop,
			@Nonnull ShopPaperwork shopPaperwork) {
		Response<Shop> result = new Response<Shop>();
		try {
			shopManager.createShopAndExtra(shop);
			shopPaperwork.setShopId(shop.getId());
			shopPaperworkDao.create(shopPaperwork);
			result.setResult(shop);
			return result;
		} catch (Exception e) {
			log.error("failed to create {},cause:{}", shop,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.create.fail");
			return result;
		}
	}

	/**
	 * 更新除状态和用户之外的店铺信息 此时不允许修改税务登记号
	 *
	 * @param shop
	 *            店铺
	 * @return 是否更新成功
	 */
	@Override
	public Response<Boolean> update(@Nonnull Shop shop) {
		Response<Boolean> result = new Response<Boolean>();
		try {

			shop.setTaxRegisterNo(null);
			boolean success = shopDao.update(shop);
			result.setResult(success);

			return result;
		} catch (Exception e) {
			log.error("failed to update {}, cause:{}", shop,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.update.fail");
			return result;
		}
	}

	/*
	 * 申请失败后的再次提交申请
	 */
	@Override
	public Response<Boolean> update(@Nonnull Shop shop,
			@Nonnull ShopPaperwork shopPaperwork) {
		Response<Boolean> result = new Response<Boolean>();

		try {
			// 更改店铺基本信息
			if (shopDao.update(shop)) {
				// 更改门店证件数据
				result.setResult(shopPaperworkDao.updateByShopId(shopPaperwork));
			} else {
				result.setResult(false);
			}
		} catch (Exception e) {
			log.error("failed to update shop:{}, shopPaperWork={}, cause:{}",
					shop, shopPaperwork, Throwables.getStackTraceAsString(e));
			result.setError("shop.update.fail");
		}

		return result;
	}

	/**
	 * 物理删除店铺对象,运营使用
	 *
	 * @param id
	 *            店铺id
	 * @return 是否删除成功
	 */
	@Override
	public Response<Boolean> delete(Long id) {
		Response<Boolean> result = new Response<Boolean>();
		try {
			boolean success = shopDao.delete(id);
			result.setResult(success);
			return result;
		} catch (Exception e) {
			log.error("failed to delete shop id={}, cause:{}", id,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.delete.fail");
			return result;
		}
	}

	/**
	 * 更新店铺状态,这个接口是给运营用的
	 *
	 * @param shopId
	 *            店铺id
	 * @param status
	 *            目标状态
	 * @return 是否更新成功
	 */
	@Override
	public Response<Boolean> updateStatus(@Nonnull Long shopId,
			@Nonnull Shop.Status status) {
		Response<Boolean> result = new Response<Boolean>();
		try {
			boolean success = shopDao.updateStatus(shopId, status);
			result.setResult(success);
			return result;
		} catch (Exception e) {
			log.error("failed to update shop(id={}) status to {}, cause:{}",
					shopId, status, Throwables.getStackTraceAsString(e));
			result.setError("shop.update.fail");
			return result;
		}
	}

	/**
	 * 全量dump搜索引擎
	 */
	@Override
	public Response<Boolean> fullDump() {

		Response<Boolean> result = new Response<Boolean>();
		log.info("[FULL_DUMP_SHOP] shop dump start ");
		Stopwatch stopwatch = Stopwatch.createStarted();

		try {
			Long lastId = shopDao.maxId() + 1; // scan from maxId+1
			int returnSize = PAGE_SIZE;
			long handled = 0;
			while (returnSize == PAGE_SIZE) {
				List<Shop> shops = shopDao.forDump(lastId, PAGE_SIZE);
				if (shops.isEmpty()) {
					break;
				}

				indexShops(shops);
				handled += shops.size();
				lastId = shops.get(shops.size() - 1).getId();
				log.info("has indexed {} shops,and last handled id is {}",
						handled, lastId);
				returnSize = shops.size();
			}
			stopwatch.stop();
			result.setResult(Boolean.TRUE);

		} catch (Exception e) {
			log.error("[FULL_DUMP_SHOP] failed ", e);
			result.setError("shop.dump.fail");
		}

		log.info("[FULL_DUMP_SHOP] shop dump end, took {} ms",
				stopwatch.elapsed(TimeUnit.MILLISECONDS));
		return result;
	}

	private void indexShops(List<Shop> shops) {
		// 先从索引删除非正常状态的店铺
		Iterable<Shop> abnormalShops = Iterables.filter(shops,
				new Predicate<Shop>() {
					@Override
					public boolean apply(Shop shop) {
						return !Objects.equal(shop.getStatus(),
								Shop.Status.OK.value());
					}
				});

		for (Shop abnormalShop : abnormalShops) {
			esClient.delete(SHOP_INDEX_NAME, SHOP_INDEX_TYPE,
					abnormalShop.getId());
		}
		// 更新正常状态的店铺
		List<RichShop> richShops = transformShop2ShopSearchResult(Iterables
				.filter(shops, new Predicate<Shop>() {
					@Override
					public boolean apply(Shop shop) {
						return Objects.equal(shop.getStatus(),
								Shop.Status.OK.value());
					}
				}));
		if (!richShops.isEmpty()) {
			esClient.index(SHOP_INDEX_NAME, SHOP_INDEX_TYPE, richShops);
		}
	}

	/**
	 * 增量dump搜索引擎
	 *
	 * @param intervalInMinutes
	 *            间隔时间,以分钟计算
	 */
	@Override
	public void deltaDump(int intervalInMinutes) {
		log.info("[DELTA_DUMP_SHOP] shop delta refresh start ");
		String compared = DATE_TIME_FORMAT.print(new DateTime()
				.minusMinutes(intervalInMinutes));
		Stopwatch stopwatch = Stopwatch.createStarted();
		int returnSize = PAGE_SIZE;
		Long lastId = shopDao.maxId() + 1; // scan from maxId+1
		int handled = 0;
		while (returnSize == PAGE_SIZE) {
			List<Shop> shops = shopDao
					.forDeltaDump(lastId, compared, PAGE_SIZE);
			if (shops.isEmpty()) {
				break;
			}

			indexShops(shops);
			handled += shops.size();
			lastId = shops.get(shops.size() - 1).getId();
			log.info("has indexed {} shops,and last handled id is {}", handled,
					lastId);
			returnSize = shops.size();
		}
		stopwatch.stop();
		log.info("[DELTA_DUMP_SHOP] shop delta refresh end, took {} ms",
				stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

	private List<RichShop> transformShop2ShopSearchResult(Iterable<Shop> shops) {
		List<RichShop> result = Lists.newArrayListWithCapacity(Iterables
				.size(shops));
		for (Shop s : shops) {
			try {
				RichShop ssr = new RichShop();
				BeanMapper.copy(s, ssr);

				buildProvinceIfNeed(s, ssr);
				buildCityIfNeed(s, ssr);
				buildRegionIfNeed(s, ssr);

				// 店铺宝贝数，-1代表缓存失效
				String itemCount = shopRedisDao.findById(s.getId());
				Integer itemNum = Integer.parseInt(itemCount);
				itemNum = itemNum > 0 ? itemNum : 0;
				ssr.setItemCount(itemNum);
				// shop soldQuantity count
				String soldQuantity = shopRedisDao.findShopSoldQuantityById(s
						.getId());
				ssr.setSoldQuantity(Integer.valueOf(soldQuantity));
				// shop sale count
				String sale = shopRedisDao.findShopSalesById(s.getId());
				ssr.setSale(Long.valueOf(sale));
				result.add(ssr);
			} catch (IllegalStateException e) {
				log.error(
						"fail to transform shop{} to richShop, cause:{},skip it",
						s, e.getMessage());
			} catch (Exception e) {
				log.error(
						"fail to transform shop{} to richShop, cause:{},skip it",
						s, Throwables.getStackTraceAsString(e));
			}
		}
		return result;
	}

	private void buildProvinceIfNeed(Shop shop, RichShop richShop) {
		if (notNull(shop.getProvince())) {
			Response<Address> provinceR = addressService.findById(shop
					.getProvince());

			if (provinceR.isSuccess()) {
				richShop.setProvinceName(provinceR.getResult().getName());
			}
		}
	}

	private void buildCityIfNeed(Shop shop, RichShop richShop) {
		if (notNull(shop.getCity())) {
			Response<Address> cityR = addressService.findById(shop.getCity());

			if (cityR.isSuccess()) {
				richShop.setCityName(cityR.getResult().getName());
			}
		}
	}

	private void buildRegionIfNeed(Shop shop, RichShop richShop) {
		if (notNull(shop.getRegion())) {
			Response<Address> regionR = addressService.findById(shop
					.getRegion());

			if (regionR.isSuccess()) {
				richShop.setRegionName(regionR.getResult().getName());
			}
		}
	}

	public Response<Boolean> approved(String ids, Integer status) {
		Response<Boolean> result = new Response<Boolean>();

		try {
			checkArgument(notEmpty(ids), "ids.can.not.be.empty");
			checkArgument(notNull(status), "status.can.not.be.empty");
			List<String> parts = commaSplitter.splitToList(ids);
			List<Long> idList = convertToLong(parts);
			shopDao.batchUpdateStatus(idList, status);

			// 若店铺审核通过,则为其创建extra
			if (equal(status, Shop.Status.OK.value())) {
				shopEventBus.post(new ApprovePassEvent(idList));
			}
			result.setResult(Boolean.TRUE);

		} catch (IllegalArgumentException e) {
			log.error("failed to approved shops with ids:{} error:{}", ids,
					e.getMessage());
			result.setError(e.getMessage());
		} catch (IllegalStateException e) {
			log.error("failed to approved shops with ids:{} error:{}", ids,
					e.getMessage());
			result.setError(e.getMessage());
		} catch (Exception e) {
			log.error("failed to approved shops with ids:{} cause:{}", ids,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.update.fail");
		}

		return result;
	}

	private List<Long> convertToLong(List<String> parts) {
		return Lists.transform(parts, new Function<String, Long>() {
			@Override
			public Long apply(String input) {
				return Long.parseLong(input);
			}
		});
	}

	@Override
	public Response<Boolean> updateStatusByIds(String ids, Integer status) {
		Response<Boolean> result = new Response<Boolean>();
		if (Strings.isNullOrEmpty(ids)) {
			log.error("ids can not be null or empty");
			result.setError("ids.not.found");
			return result;
		}
		if (status == null) {
			log.error("status can not be null");
			result.setError("status.not.found");
			return result;
		}
		List<String> parts = commaSplitter.splitToList(ids);
		List<Long> idsLong = Lists.transform(parts,
				new Function<String, Long>() {
					@Override
					public Long apply(String input) {
						return Long.parseLong(input);
					}
				});
		try {
			shopDao.batchUpdateStatus(idsLong, status);
			result.setResult(true);
			return result;
		} catch (Exception e) {
			log.error("failed to update shop by ids shopIds:{} cause:{}", ids,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.update.fail");
			return result;
		}
	}

	@Override
	public Response<Paging<ShopDto>> find(
			@ParamInfo("params") Map<String, String> params,
			@ParamInfo("pageNo") Integer pageNo, @ParamInfo("size") Integer size) {
		PageInfo pageInfo = new PageInfo(pageNo, size);
		Response<Paging<ShopDto>> result = new Response<Paging<ShopDto>>();
		try {
			String shopName = params.get("shopName");
			String businessId = params.get("businessId");
			String userName = params.get("sellerName");
			Paging<Shop> shops = shopDao.shops(pageInfo.getOffset(),
					pageInfo.getLimit(), shopName, businessId, userName);
			List<ShopDto> shopDtos = transToShopDto(shops.getData());
			result.setResult(new Paging<ShopDto>(shops.getTotal(), shopDtos));
			return result;
		} catch (NullPointerException e) {
			log.error("address.query.fail", e);
			result.setError("address.query.fail");
			return result;
		} catch (Exception e) {
			log.error("find shop fail", e);
			result.setError("shop.query.fail");
			return result;
		}
	}

	@Override
	public Response<ShopSidebar> findShopSideBar(Long sellerId) {
		Response<ShopSidebar> result = new Response<ShopSidebar>();
		try {
            ShopSidebar shopSidebar = new ShopSidebar();
            if(sellerId==null){//店铺sellerId为空 可以判断为不是店铺客服 增加类型  表明是 mall 客服
                shopSidebar.setShopType(2L);
                shopSidebar.setRReserve(0);//到店有礼是否显示
            }else{
                Shop shop = shopDao.findByUserId(sellerId);

                ShopExtra extra = shopExtraDao.findByShopId(shop.getId());
                if (extra != null) {
                    shopSidebar.setRExpress(extra.score(extra.getRExpress()));
                    shopSidebar.setRQuality(extra.score(extra.getRQuality()));
                    shopSidebar.setRDescribe(extra.score(extra.getRDescribe()));
                    shopSidebar.setRService(extra.score(extra.getRService()));
                    shopSidebar.setNtalkerId(extra.getNtalkerId());
                }
                shopSidebar.setCreateAt(shop.getCreatedAt());
                shopSidebar.setShopId(shop.getId());
                shopSidebar.setShopName(shop.getName());
                shopSidebar.setShopPhone(shop.getPhone());
                shopSidebar.setSellerId(shop.getUserId());
                shopSidebar.setBusinessId(shop.getBusinessId());


                shopSidebar.setShopType(1L);
                Response<ShopGiftConfig> shopGiftConfigResponse =  shopGiftConfigService.findShopGift(shop.getId());
                if(shopGiftConfigResponse.isSuccess()){
                    ShopGiftConfig shopGiftConfig = shopGiftConfigResponse.getResult();

                    if (shopGiftConfig!=null && equal(shopGiftConfig.getEnable(), 1)) {
                        shopSidebar.setShopConfigId(shopGiftConfig.getId());
                        shopSidebar.setRReserve(1);//到店有礼是否显示
                        shopSidebar.setStreet(shop.getStreet()); //店铺地址
                        //获取当前登陆用户的手机和姓名
                        BaseUser baseUser = UserUtil.getCurrentUser();
                        if(baseUser!=null){
                            Response<UserProfile> userProfileResponse = userProfileService.findUserProfileByUserId(baseUser.getId());
                            if(userProfileResponse.isSuccess()){
                                shopSidebar.setLoginName(userProfileResponse.getResult().getRealName());
                            }
                            shopSidebar.setLoginMobile(baseUser.getMobile());
                        }

                        StringBuffer sbstart = new StringBuffer();
                        sbstart.append("上午:").append(formatSToS(shopGiftConfig.getAmstart())).append("-").append(formatSToS(shopGiftConfig.getAmend()));
                        shopSidebar.setArriveStartTime(sbstart.toString());

                        StringBuffer sbend = new StringBuffer();
                        sbend.append("下午:").append(formatSToS(shopGiftConfig.getPmstart())).append("-").append(formatSToS(shopGiftConfig.getPmend()));
                        shopSidebar.setArriveEndTime(sbend.toString());

                        if( (isEnableDate(shopGiftConfig.getAmstart())) || (!isEnableDate(shopGiftConfig.getPmend())) || ( !isEnableDate(shopGiftConfig.getAmend()) && isEnableDate(shopGiftConfig.getPmstart()) ) ){ //当前时间晚于最后时间则无效
                            shopSidebar.setIsEnable(0);//都无效
                        }else if(!isEnableDate(shopGiftConfig.getAmstart()) && isEnableDate(shopGiftConfig.getAmend()) ){
                            shopSidebar.setIsEnable(1);//都有效
                        }else if(!isEnableDate(shopGiftConfig.getPmstart()) && isEnableDate(shopGiftConfig.getPmend())){
                           shopSidebar.setIsEnable(2); //上午无效 下午有效
                        }
                    }
                }
            }

            result.setResult(shopSidebar);
            return result;
        } catch (Exception e) {
            log.error("fail to find shopSideBar by userId={}, cause:{}", sellerId, Throwables.getStackTraceAsString(e));
            result.setError("shop.query.fail");
            return result;
        }
    }


    //判断当前时间是否是预设的时间
    public boolean isEnableDate(String strs){
        int check = Integer.valueOf(strs.substring(0, 2));
        int nowHour = new Date().getHours();
        if(nowHour>=check){
            return false;
        }else{
            return true;
        }
    }

    //时间自定义格式化处理
    public String formatSToS(String apmTime){
        if(apmTime!=null && !apmTime.equals("")){
            return apmTime.substring(0,2).concat(":").concat(apmTime.substring(2));
        }else{
            return "";
        }
    }

    @Override
    public Response<Long> getItemCountByShopId(Long shopId) {
        Response<Long> result = new Response<Long>();
        if (shopId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            String count = shopRedisDao.findById(shopId);
            result.setResult(Long.parseLong(count));
            return result;
        } catch (Exception e) {
            log.error("fail to get item count by shopId={}, cause:{}", shopId, Throwables.getStackTraceAsString(e));
            result.setError("get.itemCount.fail");
            return result;
        }
    }
	@Override
	public Response<Paging<RichShop>> searchShop(int pageNo, int size,
			Map<String, String> params) {
		Response<Paging<RichShop>> result = new Response<Paging<RichShop>>();
		try {
			pageNo = firstNonNull(pageNo, 1);
			size = firstNonNull(size, 20);
			pageNo = pageNo <= 0 ? 1 : pageNo;
			size = size <= 0 ? 20 : size;
			if (params == null) {
				params = Maps.newHashMap();
			}
			params.put("status", String.valueOf(Shop.Status.OK.value())); // 只搜索正常状态下的店铺
			SearchRequestBuilder requestBuilder = esClient
					.searchRequestBuilder(SHOP_INDEX_NAME);
			QueryBuilder queryBuilder;
			List<FilterBuilder> filters = Lists.newArrayList();
			String keywords = params.get("q");
			if (!Strings.isNullOrEmpty(keywords)) {
				queryBuilder = QueryBuilders.matchQuery("name", keywords);
			} else {
				queryBuilder = QueryBuilders.matchAllQuery();
			}
			String status = params.get("status");
			if (!Strings.isNullOrEmpty(status)) {
				filters.add(FilterBuilders.termFilter("status",
						Integer.parseInt(status)));
			}
			String businessId = params.get("businessId");
			if (!Strings.isNullOrEmpty(businessId)) {
				filters.add(FilterBuilders.termFilter("businessId",
						Long.parseLong(businessId)));
			}
			if (!filters.isEmpty()) {
				AndFilterBuilder and = new AndFilterBuilder();
				for (FilterBuilder filter : filters) {
					and.add(filter);
				}
				queryBuilder = new FilteredQueryBuilder(queryBuilder, and);
			}
			requestBuilder.setQuery(queryBuilder);
			String sort = params.get("sort");
			if (!Strings.isNullOrEmpty(sort)) {
				Iterable<String> parts = underscoreSplitter.split(sort);
				String soldQuantity = Iterables.getFirst(parts, "0");
				String sale = Iterables.get(parts, 1, "0");
				switch (Integer.valueOf(soldQuantity)) {
				case 1:
					requestBuilder.addSort("soldQuantity", SortOrder.ASC);
					break;
				case 2:
					requestBuilder.addSort("soldQuantity", SortOrder.DESC);
					break;
				default:
					break;
				}
				switch (Integer.valueOf(sale)) {
				case 1:
					requestBuilder.addSort("sale", SortOrder.ASC);
					break;
				case 2:
					requestBuilder.addSort("sale", SortOrder.DESC);
					break;
				default:
					break;
				}
			}
			requestBuilder.setFrom((pageNo - 1) * size).setSize(size);
			Paging<RichShop> shopP = esClient.search(SHOP_INDEX_TYPE,
					RichShop.class, requestBuilder);
			if (shopP.getTotal() == 0) {
				shopP = new Paging<RichShop>(0l,
						Collections.<RichShop> emptyList());
			}
			result.setResult(shopP);
			return result;
		} catch (Exception e) {
			log.error("failed to search shop", e);
			result.setError("shop.search.fail");
			return result;
		}
	}

	@Override
	public Response<Map<String, RichShop>> starShop(Long businessId) {
		Map<String, String> saleParams = Maps.newHashMap();
		saleParams.put("sort", "0_2");
		if (businessId != null) {
			saleParams.put("businessId", String.valueOf(businessId));
		}
		Paging<RichShop> topSale = searchShop(1, 1, saleParams).getResult();
		Map<String, String> soldQuantityParams = Maps.newHashMap();
		soldQuantityParams.put("sort", "2_0");
		if (businessId != null) {
			soldQuantityParams.put("businessId", String.valueOf(businessId));
		}
		Paging<RichShop> topSoldQuantity = searchShop(1, 1, soldQuantityParams)
				.getResult();
		Response<Map<String, RichShop>> result = new Response<Map<String, RichShop>>();
		Map<String, RichShop> resultMap = Maps.newHashMap();
		resultMap.put("topSale", topSale.getData().get(0));
		resultMap.put("topSoldQuantity", topSoldQuantity.getData().get(0));
		result.setResult(resultMap);
		return result;
	}

	@Override
	public Response<Boolean> incrShopSalesBySellerId(Long sellerId, Long sale) {
		Response<Boolean> result = new Response<Boolean>();
		if (sellerId == null || sale == null) {
			log.error("sellerId and sale can not be null");
			result.setError("illegal.params");
			return result;
		}
		try {
			Shop shop = shopDao.findByUserId(sellerId);
			shopRedisDao.incrShopSalesCount(shop.getId(), sale);
			result.setResult(Boolean.TRUE);
			return result;
		} catch (Exception e) {
			log.error("failed to incr shop sales by sellerId={}, cause:{}",
					sellerId, Throwables.getStackTraceAsString(e));
			result.setError("incr.shop.sale.fail");
			return result;
		}
	}

	@Override
	public Response<Boolean> incrShopSoldQuantityBySellerId(Long sellerId,
			Long soldQuantity) {
		Response<Boolean> result = new Response<Boolean>();
		if (sellerId == null || soldQuantity == null) {
			log.error("sellerId and soldQuantity both can not be null");
			result.setError("illegal.params");
			return result;
		}
		try {
			Shop shop = shopDao.findByUserId(sellerId);
			shopRedisDao.incrShopSoldQuantityCount(shop.getId(), soldQuantity);
			result.setResult(Boolean.TRUE);
			return result;
		} catch (Exception e) {
			log.error(
					"failed to incr shop sold quantity by sellerId={}, cause:{}",
					sellerId, Throwables.getStackTraceAsString(e));
			result.setError("incr.shop.soldQuantity.fail");
			return result;
		}
	}

	@Override
	public Response<Long> setExtra(ShopExtra extra) {
		Response<Long> result = new Response<Long>();
		if (extra == null || extra.getShopId() == null) {
			log.error("shopId in shopExtra should not be null");
			result.setError("illegal.params");
			return result;
		}

		ShopExtra exist = shopExtraDao.findByShopId(extra.getShopId());
		if (exist != null) {
			shopExtraDao.updateByShopId(extra);
		} else {
			shopExtraDao.create(extra);
		}
		result.setResult(extra.getId());
		return result;
	}

	@Override
	public Response<Boolean> updateExtra(ShopExtra extra) {

		Response<Boolean> result = new Response<Boolean>();

		try {
			checkArgument(notNull(extra), "extra.can.not.be.null");
			Boolean success = shopExtraDao.update(extra);
			checkState(success, "shop.extra.not.found");

			result.setResult(Boolean.TRUE);
		} catch (IllegalArgumentException e) {
			log.error("fail to update shopExtra:{}, error:{}", extra,
					e.getMessage());
			result.setError(e.getMessage());
		} catch (IllegalStateException e) {
			log.error("fail to update shopExtra:{}, error:{}", extra,
					e.getMessage());
			result.setError(e.getMessage());
		} catch (Exception e) {
			log.error("fail to update shopExtra:{}", extra, e);
			result.setError("shop.extra.update.fail");
		}

		return result;
	}

	private List<ShopDto> transToShopDto(List<Shop> shops) {
		List<ShopDto> result = Lists.newArrayList();
		for (Shop shop : shops) {
			ShopDto shopDto = new ShopDto();
			BeanMapper.copy(shop, shopDto);

			if (notNull(shop.getProvince())) {
				Address province = getAddress(shop.getProvince());
				shopDto.setProvinceName(province.getName());
			}

			if (notNull(shop.getCity())) {
				Address city = getAddress(shop.getCity());
				shopDto.setCityName(city.getName());
			}

			if (notNull(shop.getRegion())) {
				Address region = getAddress(shop.getRegion());
				shopDto.setRegionName(region.getName());
			}

			ShopExtra extra = shopExtraDao.findByShopId(shop.getId());
			if (isNull(extra)) {
				log.warn("failed  to find shopExtra of shop(id:{})",
						shop.getId());
			}
			shopDto.setExtra(extra);
			result.add(shopDto);
		}
		return result;
	}

	private Address getAddress(Integer id) {
		Response<Address> addressQueryResult = addressService.findById(id);
		if (addressQueryResult.isSuccess()) {
			return addressQueryResult.getResult();
		} else {
			log.warn("failed to find address with:{}", id);
			return null;
		}
	}

	/**
	 * 获取店铺的额外信息
	 *
	 * @param sellerId
	 *            卖家id
	 * @return 店铺的额外信息
	 */
	@Override
	public Response<ShopExtra> getExtra(Long sellerId) {
		Response<ShopExtra> result = new Response<ShopExtra>();

		try {

			checkArgument(notNull(sellerId), "seller.id.can.not.be.null");
			Shop shop = shopDao.findByUserId(sellerId);
			checkState(notNull(shop), "shop.not.found");

			ShopExtra extra = shopExtraDao.findByShopId(shop.getId());
			checkState(notNull(extra), "shop.extra.not.found");

			result.setResult(extra);
		} catch (IllegalArgumentException e) {
			log.error("fail to get shopExtra with sellerId:{}, error:{} ",
					sellerId, e.getMessage());
			result.setError(e.getMessage());
		} catch (IllegalStateException e) {
			log.error("fail to get shopExtra with sellerId:{}, error:{} ",
					sellerId, e.getMessage());
			result.setError(e.getMessage());
		} catch (Exception e) {
			log.error("fail to get shopExtra with sellerId:{}, cause:{} ",
					sellerId, Throwables.getStackTraceAsString(e));
			result.setError("shop.extra.get.fail");
		}

		return result;
	}

	@Override
	public Response<List<Shop>> findByOuterCode(String outerCode) {
		Response<List<Shop>> result = new Response<List<Shop>>();
		if (Strings.isNullOrEmpty(outerCode)) {
			log.error("outerCode can not be null");
			result.setError("illegal.params");
			return result;
		}
		try {
			List<ShopExtra> shopExtras = shopExtraDao
					.findByOuterCode(outerCode);
			if (CollectionUtils.isEmpty(shopExtras)) {
				result.setError("shop.extra.not.found");
				return result;
			}

			List<Shop> shops = shopDao.findByIds(Lists.transform(shopExtras,
					new Function<ShopExtra, Long>() {
						@Override
						public Long apply(ShopExtra input) {
							return input.getShopId();
						}
					}));
			result.setResult(shops);
			return result;
		} catch (Exception e) {
			log.error("failed to find shop by outerCode{}, cause:{}",
					outerCode, Throwables.getStackTraceAsString(e));
			result.setError("shop.query.fail");
			return result;
		}
	}

	@Override
	public Response<List<Long>> bulkUpdateShopExtraScore(List<ShopExtra> extras) {
		Response<List<Long>> result = new Response<List<Long>>();

		try {
			List<Long> ids = shopManager.bulkUpdateShopExtraScore(extras);

			result.setResult(ids);
			return result;
		} catch (Exception e) {
			log.error("`batchSetExtra` invoke fail params={}. e:{}", extras,
					Throwables.getStackTraceAsString(e));
			result.setError("shop.batch.set.extra.fail");
			return result;
		}
	}

	@Override
	public Response<Boolean> updateShopExtraScore(ShopExtra extra) {
		Response<Boolean> result = new Response<Boolean>();

		try {
			shopManager.updateShopExtraScore(extra);
			result.setResult(Boolean.TRUE);
			return result;
		} catch (Exception e) {
			log.error(
					"fail to full update shopExtras score by extras{}, cause:{}",
					extra, Throwables.getStackTraceAsString(e));
			result.setError("shop.batch.set.extra.fail");
			return result;
		}
	}

	@Override
	public Response<Boolean> fullUpdateShopExtraScore(ShopExtra extra) {
		Response<Boolean> result = new Response<Boolean>();

		try {
			shopManager.fullUpdateShopExtraScore(extra);
			result.setResult(Boolean.TRUE);
			return result;
		} catch (Exception e) {
			log.error(
					"fail to full update shopExtras score by extras{}, cause:{}",
					extra, Throwables.getStackTraceAsString(e));
			result.setError("shop.batch.set.extra.fail");
			return result;
		}
	}

	/**
	 * 根据店铺id查询店铺证件
	 *
	 * @param shopId
	 *            店铺id
	 * @return 店铺证件
	 */
	@Override
	public Response<ShopPaperwork> findByShopId(Long shopId) {
		Response<ShopPaperwork> result = new Response<ShopPaperwork>();
		if (shopId == null) {
			log.error("shop id can not be null");
			result.setError("shop.id.null");
			return result;
		}
		try {
			ShopPaperwork shopPaperwork = shopPaperworkDao.findByShopId(shopId);
			result.setResult(shopPaperwork);
			return result;
		} catch (Exception e) {
			log.error("failed to find shop shopPaperwork(shopId={}), cause:{}",
					shopId, Throwables.getStackTraceAsString(e));
			result.setError("shopPaperwork.query.fail");
			return result;
		}
	}

	@Override
	public Response<Paging<ShopExtra>> findExtraBy(Integer pageNo, Integer size) {

		Response<Paging<ShopExtra>> result = new Response<Paging<ShopExtra>>();
		try {
			PageInfo pageInfo = new PageInfo(pageNo, size);
			Map<String, Object> params = Maps.newHashMap();
			Paging<ShopExtra> paging = shopExtraDao.findBy(params,
					pageInfo.offset, pageInfo.limit);
			result.setResult(paging);

		} catch (Exception e) {
			log.error("fail to find shopExtra", e);
			result.setError("shop.extra.query.fail");
		}

		return result;
	}

	/**
	 * shop 记录的最大 id
	 */
	@Override
	public Response<Long> maxId() {
		Response<Long> result = new Response<Long>();

		try {
			Long maxId = shopDao.maxId();
			if (isNull(maxId)) {
				log.warn("find max if of shop fail, maybe shop has no records.");
				maxId = Long.MAX_VALUE - 1;
			}
			result.setResult(maxId);
			return result;

		} catch (Exception e) {
			log.error("`maxId` invoke fail. e:{}", e);
			result.setError("order.get.max.id.fail");
			return result;
		}
	}

	/**
	 * 获取全量统计用的id列表
	 *
	 * @param lastId
	 *            上次处理的最大id
	 * @param limit
	 *            每次处理的记录数量
	 * @return 因为shop extra 不一定存在，可能返回空列表
	 */
	@Override
	public Response<List<Shop>> forDump(Long lastId, Integer limit) {
		Response<List<Shop>> result = new Response<List<Shop>>();

		if (isNull(lastId) || isNull(limit)) {
			log.error("argument can't be null: last id:{}, limit:{}", lastId,
					limit);
			result.setError("illegal.param");
			return result;
		}

		try {
			List<Shop> ids = shopDao.forDump(lastId, limit);
			result.setResult(ids);
			return result;

		} catch (Exception e) {
			log.error("`forDump` invoke fail. e:{}", e);
			result.setError("order.get.id.for.dump.fail");
			return result;
		}
	}

	/**
	 * 批量根据税务登记号更新
	 *
	 * @param taxNo
	 *            税务登记号
	 * @param outerCode
	 *            商户88码
	 * @return 被更新的店铺信息
	 */
	@Override
	public Response<List<Shop>> batchUpdateOuterCodeWithTaxNo(String taxNo,
			String outerCode) {
		Response<List<Shop>> result = new Response<List<Shop>>();

		try {
			checkArgument(notEmpty(outerCode) && outerCode.length() == 10,
					"outer.code.not.valid");
			checkArgument(notEmpty(taxNo) && taxNo.length() <= 20,
					"tax.no.not.valid");
			List<Shop> shops = shopDao.findByTaxRegisterNo(taxNo);
			List<Shop> updated = Lists.newArrayListWithCapacity(shops.size());

			for (Shop shop : shops) {
				ShopExtra shopExtra = shopExtraDao.findByShopId(shop.getId());
				if (notNull(shopExtra)) {
					if (!equalWith(shopExtra.getOuterCode(), outerCode)) { // 若店铺extra且outerCode发生变化则更新
						shopExtra.setOuterCode(outerCode);
						boolean success = shopExtraDao.update(shopExtra);
						if (!success) {
							log.error(
									"fail to update outerCode from {} -> {} of shop(id:{}, name:{})",
									shopExtra.getOuterCode(), outerCode,
									shop.getId(), shop.getName());
						}

						updated.add(shop);
					}
					continue;
				}

				shopExtra = new ShopExtra();
				shopExtra.setShopId(shop.getId());
				shopExtra.setOuterCode(outerCode);
				Long id = shopExtraDao.create(shopExtra);
				if (isNull(id)) {
					log.error("fail to create shopExtra:{}", shopExtra);
				}

				updated.add(shop);

			}

			result.setResult(updated);

		} catch (IllegalArgumentException e) {
			log.error("failed to update outerCode:{} with taxNo:{}", outerCode,
					taxNo, e.getMessage());
			result.setError(e.getMessage());
		} catch (IllegalStateException e) {
			log.error("failed to update outerCode:{} with taxNo:{}", outerCode,
					taxNo, e.getMessage());
			result.setError(e.getMessage());
		} catch (Exception e) {
			log.error("failed to update outerCode:{} with taxNo:{}", outerCode,
					taxNo, Throwables.getStackTraceAsString(e));
			result.setError("batch.update.shop.outer.code.fail");
		}

		return result;
	}

    /**
     * 查询税务等级号不为空的shopExtra
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return shopExtra分页信息
     */
    @Override
    public Response<Paging<Shop>> findWithTaxNo(Integer pageNo, Integer pageSize) {
        Response<Paging<Shop>> result = new Response<Paging<Shop>>();

        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            Paging<Shop> paging = shopDao.findWithTaxNo(pageInfo.offset, pageInfo.limit);
            result.setResult(paging);

        } catch (Exception e) {
            log.error("failed to find shops with pageNo:{}, pageSize:{}, cause:{}",
                    pageNo, pageSize, Throwables.getStackTraceAsString(e));
        }

        return result;
    }


}
