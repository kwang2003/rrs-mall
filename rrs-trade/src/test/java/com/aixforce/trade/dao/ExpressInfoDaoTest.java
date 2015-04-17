package com.aixforce.trade.dao;

import com.aixforce.trade.model.ExpressInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
/**
 * 快递信息Dao测试
 * Author: haolin
 * On: 9/22/14
 */
public class ExpressInfoDaoTest extends BaseDaoTest {

    @Autowired
    private ExpressInfoDao expressInfoDao;

    @Test
    public void testCreate(){
        assertNotNull(mock("kd", "kd", "kd100"));
    }

    @Test
    public void testUpdate(){
        ExpressInfo expressInfo = mock("kd_update", "kd_update", "kd100");
        expressInfoDao.create(expressInfo);

        expressInfo.setName("kd_updated");
        expressInfo.setStatus(ExpressInfo.Status.ENABLED.value());
        expressInfoDao.update(expressInfo);

        ExpressInfo updated = expressInfoDao.load(expressInfo.getId());
        assertEquals("kd_updated", updated.getName());
        assertEquals(ExpressInfo.Status.ENABLED.value(), updated.getStatus().intValue());
    }

    @Test
    public void testList(){
        ExpressInfo expressInfo = mock("kd_list0", "kd_list0", "kd100");
        expressInfoDao.create(expressInfo);

        expressInfo = mock("kd_list1", "kd_list1", "kd100");
        expressInfoDao.create(expressInfo);
        expressInfo = mock("kd_list2", "kd_list2", "kd100");
        expressInfoDao.create(expressInfo);
        expressInfo = mock("kd_list3", "kd_list3", "kd100");
        expressInfoDao.create(expressInfo);

        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("type", "kd_list");
        criteria.put("status", ExpressInfo.Status.DISABLED.value());
        assertEquals(4, expressInfoDao.list(criteria).size());

        criteria.put("status", ExpressInfo.Status.ENABLED.value());
        assertEquals(0, expressInfoDao.list(criteria).size());

        expressInfo.setStatus(ExpressInfo.Status.ENABLED.value());
        expressInfoDao.update(expressInfo);

        criteria.put("status", ExpressInfo.Status.ENABLED.value());
        assertEquals(1, expressInfoDao.list(criteria).size());
    }

    @Test
    public void testPaging(){
        ExpressInfo expressInfo = mock("kd_pg0", "kd_pg0", "kd100");
        expressInfoDao.create(expressInfo);

        expressInfo = mock("kd_pg1", "kd_pg1", "kd100");
        expressInfoDao.create(expressInfo);
        expressInfo = mock("kd_pg2", "kd_pg2", "kd100");
        expressInfoDao.create(expressInfo);
        expressInfo = mock("kd_pg3", "kd_pg3", "kd100");
        expressInfoDao.create(expressInfo);

        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("name", "kd_pg");
        assertEquals(4, expressInfoDao.paging(0, 10, criteria).getTotal().intValue());
        assertEquals(2, expressInfoDao.paging(0, 2, criteria).getData().size());

    }

    private ExpressInfo mock(String name, String code, String interfaceName){
        ExpressInfo expressInfo = new ExpressInfo();
        expressInfo.setName(name);
        expressInfo.setCode(code);
        expressInfo.setInterfaceName(interfaceName);
        expressInfo.setStatus(ExpressInfo.Status.DISABLED.value());
        return expressInfo;
    }
}
