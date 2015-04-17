package com.aixforce.rrs.presale.dao;

import com.aixforce.rrs.grid.dto.BrandSellersDto;
import com.aixforce.rrs.grid.dto.SellerBrandsDto;
import com.aixforce.rrs.grid.model.BrandsSellers;
import com.aixforce.rrs.grid.model.UnitBrand;
import com.aixforce.rrs.grid.model.UnitSeller;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Date: 4/26/14
 * Time: 10:54
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Repository
public class BrandsSellersDao extends SqlSessionDaoSupport {

    public void create(BrandsSellers brandsSellers) {
        getSqlSession().insert("BrandsSellers.create", brandsSellers);
    }

    public BrandsSellers findOneBy(BrandsSellers brandsSellers) {
        return getSqlSession().selectOne("BrandsSellers.findOneBy", brandsSellers);
    }

    public List<BrandsSellers> findBy(BrandsSellers brandsSellers) {
        return getSqlSession().selectList("BrandsSellers.findBy", brandsSellers);
    }

    public BrandSellersDto findSellersByBrand(Long brandId) {
        List<UnitSeller> sellers = getSqlSession().selectList("BrandsSellers.findSellersByBrand", brandId);
        BrandSellersDto brandSellersDto = new BrandSellersDto();
        brandSellersDto.getBrand().setBrandId(brandId);
        brandSellersDto.setSellers(sellers);
        return brandSellersDto;
    }

    public SellerBrandsDto findBrandsBySeller(Long sellerId) {
        List<UnitBrand> brands = getSqlSession().selectList("BrandsSellers.findBrandsBySeller", sellerId);
        SellerBrandsDto sellerBrandsDto = new SellerBrandsDto();
        sellerBrandsDto.setBrands(brands);
        sellerBrandsDto.getSeller().setSellerId(sellerId);
        return sellerBrandsDto;
    }

    public List<UnitSeller> findSellersByBrands(List<Long> ids) {
        return getSqlSession().selectList("BrandsSellers.findSellersByBrands", ids);
    }

    public void deleteByBrandIdAndSellerId(Long brandId, Long sellerId) {
        getSqlSession().delete("BrandsSellers.deleteByBrandIdAndSellerId", ImmutableMap.of("brandId", brandId, "sellerId", sellerId));
    }

    public List<Long> findShopIdsByBrandId(Long brandId){
        return getSqlSession().selectList("BrandsSellers.findShopIdsByBrandId", brandId);
    }
}
