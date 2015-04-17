package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.purify.model.PurifyAssembly;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc:净水组件实体对象信息
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
public class PurifyAssemblyDaoTest extends BaseDaoTest {
    @Autowired
    private PurifyAssemblyDao purifyAssemblyDao;

    /**
     * 创建组件实体信息\
     */
    @Test
    public void createTest(){
        purifyAssemblyDao.create(mockPurifyAssembly());
    }

    /**
     * 更新组件实体信息
     */
    @Test
    public void updateTest(){
        PurifyAssembly purifyAssembly = new PurifyAssembly();
        purifyAssembly.setId(1l);
        purifyAssembly.setAssemblyName("updateTestName");

        purifyAssemblyDao.update(purifyAssembly);
    }

    /**
     * 删除系列信息
     */
    @Test
    public void deleteTest(){
        purifyAssemblyDao.delete(1l);
    }

    /**
     * 批量删除
     */
    @Test
    public void deleteByAssemblyIdsTest(){
        purifyAssemblyDao.deleteByCategoryIds(new Long[]{1l , 2l, 3l, 100l});
    }

    /**
     * 批量查询
     */
    @Test
    public void findByCategoryIds(){
        purifyAssemblyDao.findByCategoryIds(new Long[]{1l , 2l, 3l, 4l});
    }

    /**
     * 通过编号查询组件实体信息
     */
    @Test
    public void findByIdTest(){
        assertThat(purifyAssemblyDao.findById(1l) , notNullValue());
    }

    /**
     * 通过类目编号查询该类目编号下的全部组件实体
     */
    @Test
    public void findByCategoryTest(){
        List<PurifyAssembly> purifyAssemblyList = purifyAssemblyDao.findByCategory(1l);
        System.out.println("purifyAssemblyList.size->"+purifyAssemblyList.size());
        assertThat(purifyAssemblyList , notNullValue());
    }

    /**
     * 通过上级组件实体编号查询对应的下级实体编号列表
     */
    @Test
    public void findByAssemblyTest(){
        List<PurifyAssembly> purifyAssemblyList = purifyAssemblyDao.findByAssembly(1l);
        System.out.println("purifyAssemblyList.size->"+purifyAssemblyList.size());
        assertThat(purifyAssemblyList , notNullValue());
    }

    private PurifyAssembly mockPurifyAssembly(){
        PurifyAssembly purifyAssembly = new PurifyAssembly();
        purifyAssembly.setAssemblyName("类目实体1");
        purifyAssembly.setCategoryId(1l);
        purifyAssembly.setAssemblyImage("http://127.0.0.1/image/zero.png");
        purifyAssembly.setAssemblyIntroduce("测试类目实体对象");
        purifyAssembly.setAssemblyTotal(10000);
        purifyAssembly.setCreatedAt(DateTime.now().toDate());
        purifyAssembly.setUpdatedAt(DateTime.now().toDate());

        return purifyAssembly;
    }
}
