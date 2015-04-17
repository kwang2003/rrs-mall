package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.purify.model.PurifyRelation;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc:净水组件上下级关系测试
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
public class PurifyRelationDaoTest extends BaseDaoTest {
    @Autowired
    private PurifyRelationDao purifyRelationDao;

    /**
     * 创建组件上下级关系信息
     */
    @Test
    public void createTest(){
        purifyRelationDao.create(mockPurifyRelation());
    }

    /**
     * 更新组件上下级关系信息
     */
    @Test
    public void updateTest(){
        PurifyRelation purifyRelation = mockPurifyRelation();
        purifyRelation.setId(1l);
        purifyRelation.setAssemblyParent(2l);

        purifyRelationDao.update(purifyRelation);
    }

    /**
     * 删除系列信息
     */
    @Test
    public void deleteTest(){
        purifyRelationDao.delete(1l);
    }

    /**
     * 通过编号查询组件上下级关系信息
     */
    @Test
    public void findByIdTest(){
        assertThat(purifyRelationDao.findById(1l), notNullValue());
    }

    /**
     * 根据上级组件&下级组件编号确定唯一的Relation
     */
    @Test
    public void findRelationTest(){
        assertThat(purifyRelationDao.findRelation(1l, 4l), notNullValue());
    }
     private PurifyRelation mockPurifyRelation(){
        PurifyRelation purifyRelation = new PurifyRelation();
        purifyRelation.setAssemblyParent(1l);
        purifyRelation.setAssemblyChild(4l);
        purifyRelation.setProductId(0l);
        purifyRelation.setCreatedAt(DateTime.now().toDate());
        purifyRelation.setUpdatedAt(DateTime.now().toDate());

        return purifyRelation;
    }
}
