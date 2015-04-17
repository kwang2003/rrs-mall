package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.purify.model.PurifyCategory;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc:净水选择类目测试
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
public class PurifyCategoryDaoTest extends BaseDaoTest {
    @Autowired
    private PurifyCategoryDao purifyCategoryDao;

    /**
     * 创建类目信息
     */
    @Test
    public void createTest(){
        purifyCategoryDao.create(mockPurifyCategory());
    }

    /**
     * 更新类目信息
     */
    @Test
    public void updateTest(){
        PurifyCategory purifyCategory = mockPurifyCategory();
        purifyCategory.setStage(7);
        purifyCategory.setCategoryName("updateTestName");
        purifyCategory.setId(1l);

        purifyCategoryDao.update(purifyCategory);
    }

    /**
     * 删除系列信息
     */
    @Test
    public void deleteTest(){
        purifyCategoryDao.delete(1l);
    }

    /**
     * 批量删除
     */
    @Test
    public void deleteByCategoryIdsTest(){
        purifyCategoryDao.deleteBySeriesIds(new Long[]{1l, 2l, 3l, 100l});
    }

    /**
     * 通过编号查询类目信息
     */
    @Test
    public void findByIdTest(){
        assertThat(purifyCategoryDao.findById(1l) , notNullValue());
    }

    /**
     * 查询最深阶段
     */
    @Test
    public void findMaxStageTest(){
        PurifyCategory purifyCategory = purifyCategoryDao.findMaxStage(1l);
        assertThat(purifyCategory, notNullValue());
        System.out.println("next Stage->"+(purifyCategory.getStage()+1));
    }

    /*
        查询默认的类目
     */
    @Test
    public void findDefaultBySeriesIdTest(){
        PurifyCategory purifyCategory = purifyCategoryDao.findDefaultBySeriesId(1l);
        assertThat(purifyCategory, notNullValue());
    }

    /**
     *  查询类目列表
     */
    @Test
    public void findByNameTest(){
        List<PurifyCategory> purifyCategoryList = purifyCategoryDao.findByName(1l , "过滤方式");
        assertThat(purifyCategoryList , notNullValue());
        System.out.println("purifyCategoryList.size->"+purifyCategoryList.size());
    }

    /**
     * 通过系列编号查询该序列下的全部组件类目
     */
    @Test
    public void findBySeriesIdTest(){
        List<PurifyCategory> purifyCategoryList = purifyCategoryDao.findBySeriesId(1l);
        System.out.println("purifyCategoryList.size->"+purifyCategoryList.size());
        assertThat(purifyCategoryList , notNullValue());
    }

    /**
     * 通过类目编号数组查询全部的类目详细信息
     */
    @Test
    public void findByCategoryIdsTest(){
        List<PurifyCategory> purifyCategoryList = purifyCategoryDao.findByCategoryIds(new Long[]{1l, 2l, 3l, 400l});
        System.out.println("purifyCategoryList.size->"+purifyCategoryList.size());
        assertThat(purifyCategoryList , notNullValue());
    }

    /**
     * 通过组件编号查询类目编号
     */
    @Test
    public void findByAssemblyIdsTest(){
        List<PurifyCategory> purifyCategoryList = purifyCategoryDao.findByAssemblyIds(new Long[]{1l, 2l, 3l, 400l});
        System.out.println("purifyCategoryList.size->"+purifyCategoryList.size());
        assertThat(purifyCategoryList , notNullValue());
    }

    private PurifyCategory mockPurifyCategory(){
        PurifyCategory purifyCategory = new PurifyCategory();
        purifyCategory.setCategoryName("组件类目1");
        purifyCategory.setCategoryImage("http://127.0.0.1/asjdi.png");
        purifyCategory.setSeriesId(1l);
        purifyCategory.setStage(1);
        purifyCategory.setCreatedAt(DateTime.now().toDate());
        purifyCategory.setUpdatedAt(DateTime.now().toDate());

        return purifyCategory;
    }
}
