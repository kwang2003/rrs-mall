package com.aixforce.rrs.grid.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.grid.service.BrandsSellersService;
import com.aixforce.rrs.presale.dao.BrandsSellersDao;
import com.aixforce.rrs.grid.dto.BrandSellersDto;
import com.aixforce.rrs.grid.dto.SellerBrandsDto;
import com.aixforce.rrs.grid.model.UnitBrand;
import com.aixforce.rrs.grid.model.UnitSeller;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.aixforce.common.utils.Arguments.negative;
import static com.aixforce.common.utils.Arguments.notNull;

/**
 * Date: 4/26/14
 * Time: 13:33
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Service
@Slf4j
public class BrandsSellersServiceImpl implements BrandsSellersService {

    private final static String LOG_ERROR = "brands.selelrs.";

    @Autowired
    private BrandsSellersDao brandsSellersDao;

    @Override
    public Response<SellerBrandsDto> findBrandsBySeller(Long sellerId) {
        Response<SellerBrandsDto> result = new Response<SellerBrandsDto>();

        try {
            if (!notNull(sellerId)||negative(sellerId)) {
                log.error("seller id null or negative: sellerId:{}", sellerId);
                result.setError("illegal.param");
                return result;
            }

            SellerBrandsDto sellerBrandsDto = brandsSellersDao.findBrandsBySeller(sellerId);
            result.setResult(sellerBrandsDto);
            return result;

        } catch (Exception e) {
            log.error("`findBrandsBySeller` invoke fail. seller id:{}, cause:{}", sellerId,
                    Throwables.getStackTraceAsString(e));
            result.setError(LOG_ERROR+"find.brands.by.seller.fail");
            return result;
        }
    }

    @Override
    public Response<BrandSellersDto> findSellersByBrand(Long brandId) {
        Response<BrandSellersDto> result = new Response<BrandSellersDto>();

        try {
            if (brandId==null||negative(brandId)) {
                log.error("seller id null or negative: brandId:{}", brandId);
                result.setError("illegal.param");
                return result;
            }

            BrandSellersDto brandSellersDto = brandsSellersDao.findSellersByBrand(brandId);
            result.setResult(brandSellersDto);
            return result;

        } catch (Exception e) {
            log.error("`findSellersByBrand` invoke fail. brand id:{}, cause:{}", brandId,
                    Throwables.getStackTraceAsString(e));
            result.setError(LOG_ERROR+"find.sellers.by.brand.fail");
            return result;
        }
    }

    @Override
    public Response<List<UnitSeller>> findSellersByBrands(List<UnitBrand> brands) {
        Response<List<UnitSeller>> result = new Response<List<UnitSeller>>();

        try {
            if (!notNull(brands)||brands.isEmpty()) {
                result.setResult(Lists.<UnitSeller>newArrayList());
                return result;
            }

            List<Long> ids = Lists.newArrayList();
            for (UnitBrand ub : brands) {
                ids.add(ub.getBrandId());
            }

            List<UnitSeller> sellers = brandsSellersDao.findSellersByBrands(ids);
            result.setResult(sellers);
            return result;
        } catch (Exception e) {
            log.error("failed to find sellers by brands {}, cause:{}", brands, Throwables.getStackTraceAsString(e));
            result.setError(LOG_ERROR + "find.sellers.by.brand.fail");
            return result;
        }
    }
}
