package com.aixforce.admin.service;

import com.aixforce.admin.dto.ListShopDto;
import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.shop.dto.ShopDto;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.site.model.Site;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.UserExtra;
import com.aixforce.user.service.UserExtraService;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.service.BrandClubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.not;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.user.util.UserVerification.isSiteOwner;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-1-24
 */
@Slf4j
@Service
public class ShopListService {
    @Autowired
    private ShopService shopService;
    @Autowired
    private SiteService siteService;
    @Autowired
    private UserExtraService userExtraService;

    @Autowired
    private BrandClubService brandClubService;
    /**
     * 调用 shopService.find 并且额外填上二级域名的信息
     * @param params     参数
     * @param pageNo     页面
     * @param size       单页数量
     * @param business   行业
     * @return 结果
     */
    public Response<Paging<ListShopDto>> brandShoplist(@ParamInfo("params") Map<String, String> params,
                                                       @ParamInfo("pageNo") Integer pageNo,
                                                       @ParamInfo("size") Integer size,
                                                       @ParamInfo("business") @Nullable Long business,
                                                       @ParamInfo("baseUser") BaseUser baseUser) {

        Response<Paging<ListShopDto>> result = new Response<Paging<ListShopDto>>();

        try {
            if (isSiteOwner(baseUser)) {
                UserExtra extra = getUserExtra(baseUser.getId());
                // checkState(notNull(extra.getBusinessId()), "user.business.id..empty");
                // params.put("businessId", extra.getBusinessId().toString());
            } //else if (business != null) {
            // params.put("businessId", business.toString());
            // }
            // 数据过滤开始
            BrandClub bc = this.brandClubService.findByUser(baseUser.getId().intValue());
            params.put("brandClubKey",bc.getId()+"");
            // 数据过滤结束
            Response<Paging<ShopDto>> shopQueryResult = shopService.findShopsByBrand(params, pageNo, size);
            Paging<ShopDto> shops = shopQueryResult.getResult();
            List<ListShopDto> listShops = transToListShopDtos(shops);
            result.setResult(new Paging<ListShopDto>(shops.getTotal(), listShops));
        } catch (Exception e) {
            log.error("fail to query ListShopDto with params:{}, pageNo:{}, size:{}", params);

        }

        return result;
    }

    /**
     * 调用 shopService.find 并且额外填上二级域名的信息
     * @param params     参数
     * @param pageNo     页面
     * @param size       单页数量
     * @param business   行业
     * @return 结果
     */
    public Response<Paging<ListShopDto>> list(@ParamInfo("params") Map<String, String> params,
                                              @ParamInfo("pageNo") Integer pageNo,
                                              @ParamInfo("size") Integer size,
                                              @ParamInfo("business") @Nullable Long business,
                                              @ParamInfo("baseUser") BaseUser baseUser) {
        Response<Paging<ListShopDto>> result = new Response<Paging<ListShopDto>>();

        try {
            if (isSiteOwner(baseUser)) {
                UserExtra extra = getUserExtra(baseUser.getId());
                checkState(notNull(extra.getBusinessId()), "user.business.id..empty");
                params.put("businessId", extra.getBusinessId().toString());
            } else if (business != null) {
                params.put("businessId", business.toString());
            }


            Response<Paging<ShopDto>> shopQueryResult = shopService.find(params, pageNo, size);
            Paging<ShopDto> shops = shopQueryResult.getResult();
            List<ListShopDto> listShops = transToListShopDtos(shops);
            result.setResult(new Paging<ListShopDto>(shops.getTotal(), listShops));
        } catch (Exception e) {
            log.error("fail to query ListShopDto with params:{}, pageNo:{}, size:{}", params);

        }

        return result;
    }


    private List<ListShopDto> transToListShopDtos(Paging<ShopDto> shops) {
        List<ListShopDto> listShops = Lists.newArrayList();

        for (ShopDto shopDto : shops.getData()) {
            ListShopDto listShopDto = new ListShopDto();
            BeanMapper.copy(shopDto, listShopDto);
            // 只有非初始化状态的 shop 才有 site 实例
            if (!Objects.equal(shopDto.getStatus(), Shop.Status.INIT.value())) {
                Response<Site> siteR = siteService.findShopByUserId(shopDto.getUserId());
                if (not(siteR.isSuccess())) {
                    log.warn("find user {} 's site error:{}", shopDto.getUserId(), siteR.getError());
                } else {
                    listShopDto.setSubDomain(siteR.getResult().getSubdomain());
                }
            }

            listShops.add(listShopDto);
        }

        return listShops;
    }

    private UserExtra getUserExtra(Long id) {
        Response<UserExtra> extraQueryResult = userExtraService.findByUserId(id);
        checkState(extraQueryResult.isSuccess(), extraQueryResult.getError());

        return extraQueryResult.getResult();
    }
}
