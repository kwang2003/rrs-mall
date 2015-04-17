package com.aixforce.trade.dao;

import com.aixforce.trade.model.LogisticsSpecial;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc:dao测试处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-21.
 */
public class LogisticsSpecialDaoTest extends BaseDaoTest{
    @Autowired
    private LogisticsSpecialDao logisticsSpecialDao;

    /*
     * 创建特殊区域运费信息
     */
    @Test
    public void createTest(){
        logisticsSpecialDao.create(mockLogisticsSpecial());
    }

    /*
     * 批量创建
     */
    @Test
    public void createBatchTest(){
        logisticsSpecialDao.createBatch(mockList());
    }

    /*
     * 更新特殊区域运费信息
     */
    @Test
    public void updateTest(){
        LogisticsSpecial logisticsSpecial = mockLogisticsSpecial();
        logisticsSpecial.setId(1l);
        logisticsSpecial.setAddressModel("{p:100001,p:123010}");

        logisticsSpecialDao.update(logisticsSpecial);
    }

    /*
     * 删除特殊区域运费信息
     */
    @Test
    public void deleteTest(){
        logisticsSpecialDao.delete(1l);
    }

    /*
     * 删除根据模型编号
     */
    @Test
    public void deleteByModelIdTest(){
        logisticsSpecialDao.deleteByModelId(1l);
    }

    /*
     * 通过编号查询信息
     */
    @Test
    public void findByIdTest(){
        assertThat(logisticsSpecialDao.findById(1l), notNullValue());
    }

    /*
     * 通过模板编号查询
     */
    @Test
    public void findByModelId(){
        List<LogisticsSpecial> logisticsSpecials = logisticsSpecialDao.findByModelId(1l);
        assertThat(logisticsSpecials, notNullValue());
        System.out.println("size->"+logisticsSpecials.size());
    }

    private LogisticsSpecial mockLogisticsSpecial(){
        LogisticsSpecial logisticsSpecial = new LogisticsSpecial();
        logisticsSpecial.setModelId(1l);
        logisticsSpecial.setAddressModel("{p:100001,p:100002,a:1}");//a:area,p:province
        logisticsSpecial.setFirstAmount(10);
        logisticsSpecial.setFirstFee(10);
        logisticsSpecial.setAddAmount(10);
        logisticsSpecial.setAddFee(100);
        logisticsSpecial.setCreatedAt(DateTime.now().toDate());
        logisticsSpecial.setUpdatedAt(DateTime.now().toDate());

        return logisticsSpecial;
    }

    private List<LogisticsSpecial> mockList(){
        List<LogisticsSpecial> logisticsSpecials = Lists.newArrayList();
        LogisticsSpecial logistics1 = mockLogisticsSpecial();
        logistics1.setId(1l);
        logistics1.setAddressModel("{p:100001,p:123010}");

        LogisticsSpecial logistics2 = mockLogisticsSpecial();
        logistics2.setId(15l);
        logistics2.setAddressModel("{p:100001,p:123010}");

        logisticsSpecials.add(logistics1);
        logisticsSpecials.add(logistics2);

        return logisticsSpecials;
    }
}
