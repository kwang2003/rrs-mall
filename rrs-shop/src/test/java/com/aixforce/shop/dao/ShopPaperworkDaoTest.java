package com.aixforce.shop.dao;

import com.aixforce.shop.model.ShopPaperwork;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class ShopPaperworkDaoTest extends BaseDaoTest {

    @Autowired
    private ShopPaperworkDao shopPaperworkDao;

    private ShopPaperwork s;

    @Before
    public void setUp() throws Exception {
        s = new ShopPaperwork();
        s.setShopId(2L);
        s.setBusinessLicence("bl");
        s.setAccountPermit("ap");
        s.setTaxCertificate("tc");
        s.setCorporateIdentity("ci");
        s.setCorporateIdentityB("ci");
        s.setContractImage1("image1");
        s.setContractImage2("image2");

        shopPaperworkDao.create(s);
    }

    @Test
    public void testCreate() throws Exception {
       assertThat(s.getId(), notNullValue());
    }

    @Test
    public void testFindById() throws Exception {
       ShopPaperwork actual = shopPaperworkDao.findById(s.getId());

        assertThat(actual, is(s));
    }

    @Test
    public void testFindByShopId() throws Exception {
        ShopPaperwork actual = shopPaperworkDao.findByShopId(s.getShopId());
        assertThat(actual, is(s));
    }

    @Test
    public void testUpdateByShopId(){
        assertThat(shopPaperworkDao.updateByShopId(s) , is(true));
    }

    @Test
    public void testDeleteById() throws Exception {
        assertThat(shopPaperworkDao.delete(s.getId()), is(true));
    }
}