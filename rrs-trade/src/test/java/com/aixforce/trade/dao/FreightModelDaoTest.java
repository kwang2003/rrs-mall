package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.FreightModel;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc:dao测试处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-21.
 */
public class FreightModelDaoTest extends BaseDaoTest{
    @Autowired
    private FreightModelDao freightModelDao;

    /*
     * 创建运费模板
     */
    @Test
    public void createTest(){
        freightModelDao.create(mockFreightModel());
    }

    /*
     * 更新运费模板
     */
    @Test
    public void updateTest(){
        FreightModel freightModel = mockFreightModel();
        freightModel.setId(1l);
        freightModel.setModelName("数据信息");

        freightModelDao.update(freightModel);
    }

    /*
     * 删除运费模板
     */
    @Test
    public void deleteTest(){
        freightModelDao.delete(1l);
    }

    /*
     * 通过模板编号查询信息
     */
    @Test
    public void findByIdTest(){
        assertThat(freightModelDao.findById(1l), notNullValue());
    }

    @Test
    public void findBySellerIdTest(){
        assertThat(freightModelDao.findBySellerId(1l), notNullValue());
    }

    /*
     * 通过卖家编号查询卖家的全部运费模板
     */
    @Test
    public void findByParamsTest(){
        Map<String , Object> params = Maps.newHashMap();
        params.put("modelName" , "model1");
        params.put("countWay" , 1);
        params.put("costWay" , 1);
        params.put("size" , 10);
        params.put("offset" , 0);
        Paging<FreightModel> freightModelList = freightModelDao.findByParams(1l, params);
        assertThat(freightModelList, notNullValue());
        assertThat(freightModelList.getTotal(), is(1L));
        System.out.println("length->" + freightModelList.getTotal());
    }

    /*
     * 是否一存在
     */
    @Test
    public void existModel(){
        freightModelDao.existModel(1l, "model1", 1l);
    }

    private FreightModel mockFreightModel(){
        Integer countWay = 2;
        FreightModel freightModel = new FreightModel();
        freightModel.setSellerId(1l);
        freightModel.setModelName("testingModel");
        freightModel.setCountWay(FreightModel.CountWay.from(countWay) == null ? 1 : countWay);
        freightModel.setCostWay(1);
        freightModel.setFirstAmount(10);
        freightModel.setFirstFee(10);
        freightModel.setAddAmount(10);
        freightModel.setAddFee(100);
        freightModel.setStatus(FreightModel.Status.ENABLED.value());
        freightModel.setSpecialExist(1);
        freightModel.setCreatedAt(DateTime.now().toDate());
        freightModel.setUpdatedAt(DateTime.now().toDate());

        return freightModel;
    }
}
