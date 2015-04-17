package com.aixforce.rrs.grid.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.presale.dao.BrandsSellersDao;
import com.aixforce.rrs.grid.dto.BrandSellersDto;
import com.aixforce.rrs.grid.dto.SellerBrandsDto;
import com.aixforce.rrs.grid.model.BrandsSellers;
import com.aixforce.rrs.grid.model.UnitSeller;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Date: 4/26/14
 * Time: 11:23
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class BrandsSellersDaoTest extends BaseDaoTest {

    @Autowired
    BrandsSellersDao brandsSellersDao;

    private BrandsSellers bs;

    @Before
    public void setup() {
        bs = new BrandsSellers(1l, "b1", 1l , "u1",11l);
        brandsSellersDao.create(bs);

        bs = new BrandsSellers(1l, "b1", 2l , "u2",11l);
        brandsSellersDao.create(bs);

        bs = new BrandsSellers(2l, "b2", 1l , "u1",22l);
        brandsSellersDao.create(bs);

        bs = new BrandsSellers(2l, "b2", 2l , "u2",22l);
        brandsSellersDao.create(bs);
    }

    @Test
    public void shouldFindOne() {
        BrandsSellers criteria = new BrandsSellers();
        criteria.setSellerId(1l);
        criteria.setBrandId(2l);

        bs = brandsSellersDao.findOneBy(criteria);
        assertNotNull(bs);
        assertEquals(bs.getBrandId(), Long.valueOf(2));
        assertEquals(bs.getSellerId(), Long.valueOf(1));
    }

    @Test
    public void shouldFindSellers() {
        BrandSellersDto brandSellersDto = brandsSellersDao.findSellersByBrand(1l);

        assertEquals(2L, brandSellersDto.getSellers().size());
    }

    @Test
    public void shouldFindBrands() {
        SellerBrandsDto sellerBrandsDto = brandsSellersDao.findBrandsBySeller(1l);

        assertEquals(2, sellerBrandsDto.getBrands().size());
    }

    @Test
    public void shouldFindByIds() {
        List<Long> ids = Lists.newArrayList();
        ids.add(1l);
        ids.add(2l);

        List<UnitSeller> sellers = brandsSellersDao.findSellersByBrands(ids);
        assertEquals(4L, sellers.size());
    }

    @Test
    public void shouldDelete() {
        brandsSellersDao.deleteByBrandIdAndSellerId(1l,11l);
        BrandSellersDto bsd = brandsSellersDao.findSellersByBrand(2l);
        assertEquals(2L, bsd.getSellers().size());
    }
}
