package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.purify.model.PurifySeries;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc:净水系列测试
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
public class PurifySeriesDaoTest extends BaseDaoTest {
    @Autowired
    private PurifySeriesDao purifySeriesDao;

    /**
     * 创建系列信息
     */
    @Test
    public void createTest(){
        purifySeriesDao.create(mockPurifySeries());
    }

    /**
     * 更新系列信息
     */
    @Test
    public void updateTest(){
        PurifySeries purifySeries = mockPurifySeries();
        purifySeries.setId(1l);
        purifySeries.setSeriesName("updateTest测试");

        purifySeriesDao.update(purifySeries);
    }

    /**
     * 删除系列信息
     */
    @Test
    public void deleteTest(){
        purifySeriesDao.delete(1l);
    }

    /**
     * 批量删除
     */
    @Test
    public void deleteBySeriesIds(){
        purifySeriesDao.deleteBySeriesIds(new Long[]{1l , 2l, 3l, 100l});
    }

    /**
     * 通过编号查询系列信息
     */
    @Test
    public void findByIdTest(){
        assertThat(purifySeriesDao.findById(1l), notNullValue());
    }

    /**
     * 通过站点编号以及系列名称查询是否存在
     */
    @Test
    public void findByNameTest(){
        List<PurifySeries> purifySeriesList = purifySeriesDao.findByName(1l , "厨房系列");
        System.out.println("purifySeriesList.size->"+purifySeriesList.size());
        assertThat(purifySeriesList, notNullValue());
    }

    /**
     * 通过站点编号查询该站点下的全部系列信息
     */
    @Test
    public void findSiteSeriesTest(){
        List<PurifySeries> purifySeriesList = purifySeriesDao.findSiteSeries(1l);
        System.out.println("purifySeriesList.size->"+purifySeriesList.size());
        assertThat(purifySeriesList, notNullValue());
    }

    private PurifySeries mockPurifySeries(){
        PurifySeries purifySeries = new PurifySeries();
        purifySeries.setSeriesName("测试系列");
        purifySeries.setSeriesImage("http://asdmaos.asdoko.asdmm.img");
        purifySeries.setSeriesIntroduce("测试数据信息");
        purifySeries.setSiteId(1l);
        purifySeries.setCreatedAt(DateTime.now().toDate());
        purifySeries.setUpdatedAt(DateTime.now().toDate());

        return purifySeries;
    }
}
