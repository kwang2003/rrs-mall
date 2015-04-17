package com.aixforce.item.dao.mysql;

import com.aixforce.item.BaseDaoTest;
import com.aixforce.item.model.Brand;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by yangzefeng on 14-3-25
 */
public class BrandDaoTest extends BaseDaoTest {

    @Autowired
    private BrandDao brandDao;

    private Brand brand;

    @Before
    public void init() {
        brand = new Brand();
        brand.setDescription("test");
        brand.setName("name");
        brand.setEnglishName("english name");
        brandDao.create(brand);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(brand.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        brand.setName("name1");
        brandDao.update(brand);
        Brand actual = brandDao.findById(brand.getId());
        assertThat(actual.getName(), is("name1"));
    }

    @Test
    public void testCountByName() {
        brand = new Brand();
        brand.setDescription("test");
        brand.setName("name2");
        brandDao.create(brand);
        Long count = brandDao.pagingByName("name", 0, 20).getTotal();
        assertEquals(Long.valueOf(2), count);
    }
}
