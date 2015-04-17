/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.service;

import com.aixforce.category.model.Spu;
import com.aixforce.category.service.SpuService;
import com.aixforce.item.BaseServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-03
 */
public class SpuServiceImplTest extends BaseServiceTest {

    @Autowired
    private SpuService spuService;

    private Spu spu1;

    @Before
    public void setUp() throws Exception {
        spu1 = newSpu(11L, "spu1");
        spuService.create(spu1);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(spu1.getId(), notNullValue());
    }

//    @Test
//    public void testUpdate() throws Exception {
//        Spu updated = new Spu();
//        updated.setId(spu1.getId());
//        updated.setName("spu1_updated");
//        spuService.update(updated);
//        assertThat(spuService.findById(updated.getId()).getName(), is(updated.getName()));
//
//    }
//
//    @Test
//    public void testFindById() throws Exception {
//        assertThat(spuService.findById(spu1.getId()), is(spu1));
//    }

    @Test
    public void testDelete() throws Exception {
        spuService.delete(spu1.getId());
        assertThat(spuService.findById(spu1.getId()),nullValue());
    }

//    @Test
//    public void testFindByCategoryId() throws Exception {
//        Spu spu2 = newSpu(11L,"spu2");
//        spuService.create(spu2);
//        assertThat(spuService.findByCategoryId(11L),hasItems(spu1,spu2));
//    }


    private Spu newSpu(long categoryId, String name) {
        Spu spu = new Spu();
        spu.setCategoryId(categoryId);
        spu.setName(name);
        return spu;
    }
}
