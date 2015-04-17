package com.aixforce.trade.dao;

import com.aixforce.trade.model.ExpressInfo;
import com.aixforce.trade.model.InstallInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 快递信息Dao测试
 * Author: haolin
 * On: 9/22/14
 */
public class InstallInfoDaoTest extends BaseDaoTest {

    @Autowired
    private InstallInfoDao installInfoDao;

    @Test
    public void testCreate(){
        assertNotNull(mock("az", "az", "az100"));
    }

    @Test
    public void testUpdate(){
        InstallInfo installInfo = mock("az_update", "az_update", "az100");
        installInfoDao.create(installInfo);

        installInfo.setName("az_updated");
        installInfo.setStatus(ExpressInfo.Status.ENABLED.value());
        installInfoDao.update(installInfo);

        InstallInfo updated = installInfoDao.load(installInfo.getId());
        assertEquals("az_updated", updated.getName());
        assertEquals(ExpressInfo.Status.ENABLED.value(), updated.getStatus().intValue());
    }

    @Test
    public void testList(){
        InstallInfo installInfo = mock("az_list0", "az_list0", "az100");
        installInfoDao.create(installInfo);

        installInfo = mock("az_list1", "az_list1", "az100");
        installInfoDao.create(installInfo);
        installInfo = mock("az_list2", "az_list2", "az100");
        installInfoDao.create(installInfo);
        installInfo = mock("az_list3", "az_list3", "az100");
        installInfoDao.create(installInfo);

        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("name", "az_list");
        criteria.put("name", ExpressInfo.Status.DISABLED.value());
        assertEquals(4, installInfoDao.list(criteria).size());

        criteria.put("status", ExpressInfo.Status.ENABLED.value());
        assertEquals(0, installInfoDao.list(criteria).size());

        installInfo.setStatus(ExpressInfo.Status.ENABLED.value());
        installInfoDao.update(installInfo);

        criteria.put("status", ExpressInfo.Status.ENABLED.value());
        assertEquals(1, installInfoDao.list(criteria).size());
    }

    @Test
    public void testPaging(){
        InstallInfo installInfo = mock("az_pg0", "az_pg0", "az100");
        installInfoDao.create(installInfo);

        installInfo = mock("az_pg1", "az_pg1", "az100");
        installInfoDao.create(installInfo);
        installInfo = mock("az_pg2", "az_pg2", "az100");
        installInfoDao.create(installInfo);
        installInfo = mock("az_pg3", "az_pg3", "az100");
        installInfoDao.create(installInfo);

        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("name", "az_pg");
        assertEquals(4, installInfoDao.paging(0, 10, criteria).getTotal().intValue());
        assertEquals(2, installInfoDao.paging(0, 2, criteria).getData().size());

    }

    private InstallInfo mock(String name, String code, String interfaceName){
        InstallInfo installInfo = new InstallInfo();
        installInfo.setName(name);
        installInfo.setCode(code);
        installInfo.setInterfaceName(interfaceName);
        installInfo.setStatus(InstallInfo.Status.DISABLED.value());
        installInfo.setType(InstallInfo.Type.JIADIAN.value());
        return installInfo;
    }
}
