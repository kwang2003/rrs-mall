package com.aixforce.item.dao.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.item.BaseDaoTest;
import com.aixforce.item.model.SpuBundle;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 *
 * CREATED BY: IntelliJ IDEA
 * AUTHOR: haolin
 * ON: 14-4-21
 */
public class SpuBundleDaoTest extends BaseDaoTest {
    @Autowired
    private SpuBundleDao spuBundleDao;

    private SpuBundle spuBundle;

    @Before
    public void init() {
        spuBundle = new SpuBundle();
        spuBundle.setName("spuBundle1.name");
        spuBundle.setDescription("spuBundle1.desc");
        spuBundle.setIdOne(1L);
        spuBundle.setQuantityOne(10);
        spuBundle.setIdTwo(2L);
        spuBundle.setQuantityTwo(20);
        spuBundle.setIdThree(3L);
        spuBundle.setQuantityOne(30);
        spuBundle.setIdFour(4L);
        spuBundle.setQuantityFour(40);
        spuBundle.setUserId(1L);
        spuBundle.setStatus(SpuBundle.Status.ON.value());
        spuBundleDao.create(spuBundle);
        assertThat(spuBundle.getId(), notNullValue());
    }


    @Test
    public void shouldFindByUids() {
        spuBundle = new SpuBundle();
        spuBundle.setName("spuBundle1.name");
        spuBundle.setDescription("spuBundle1.desc");
        spuBundle.setIdOne(1L);
        spuBundle.setQuantityOne(10);
        spuBundle.setIdTwo(2L);
        spuBundle.setQuantityTwo(20);
        spuBundle.setIdThree(3L);
        spuBundle.setQuantityOne(30);
        spuBundle.setIdFour(4L);
        spuBundle.setQuantityFour(40);
        spuBundle.setUserId(2L);
        spuBundle.setStatus(SpuBundle.Status.ON.value());
        spuBundleDao.create(spuBundle);

        List<Long> ids = Lists.newArrayList();
        ids.add(1l);
        ids.add(2l);
        Paging<SpuBundle> res = spuBundleDao.pagingByUsers(ids,1,20);
        assertEquals(2L, res.getTotal().longValue());
    }

    @Test
    public void testFindById(){
       spuBundleDao.create(spuBundle);
       SpuBundle sb = spuBundleDao.findById(1L);
       assertEquals(sb.getName(), "spuBundle1.name");
    }

    @Test
    public void testUpdate(){
        spuBundleDao.create(spuBundle);
        String newName = "spuBundle1.newname";
        String newDesc = "spuBundle1.newdesc";
        spuBundle.setName(newName);
        spuBundle.setDescription(newDesc);

        spuBundleDao.update(spuBundle);

        SpuBundle newSpuBundle = spuBundleDao.findById(spuBundle.getId());
        assertEquals(newSpuBundle.getDescription(),newDesc);
        assertEquals(newSpuBundle.getName(),newName);
    }

    @Test
    public void testDelete(){
        spuBundleDao.create(spuBundle);
        assertNotNull(spuBundleDao.findById(spuBundle.getId()));
        spuBundleDao.delete(spuBundle.getId());
        assertNull(spuBundleDao.findById(spuBundle.getId()));
    }

    @Test
    public void testOnOff(){
        spuBundleDao.create(spuBundle);
        assertEquals(spuBundleDao.findById(spuBundle.getId()).getStatus().intValue(), SpuBundle.Status.ON.value());
        spuBundleDao.onOff(spuBundle.getId(), SpuBundle.Status.OFF);
        assertEquals(spuBundleDao.findById(spuBundle.getId()).getStatus().intValue(), SpuBundle.Status.OFF.value());

    }

    @Test
    public void testPaging(){
            long total = 109;
            SpuBundle sb;
            int offCount = 0;
            int onCount = 0;
            for (int i=0; i<total; i++){
                sb = new SpuBundle();
                sb.setName("spuBundle.name" + i);
                sb.setDescription("spuBundle.desc" + i);
                sb.setIdOne(1L);
                sb.setQuantityOne(10);
                sb.setIdTwo(2L);
                sb.setQuantityTwo(20);
                sb.setIdThree(3L);
                sb.setQuantityOne(30);
                sb.setIdFour(4L);
                sb.setQuantityFour(40);
                sb.setUserId(1L+i);
                if (i % 3 == 0){
                    sb.setStatus(SpuBundle.Status.ON.value());
                    onCount++;
                } else{
                    sb.setStatus(SpuBundle.Status.OFF.value());
                    offCount++;
                }
                spuBundleDao.create(sb);
            }
            Map<String, Object> cris = new HashMap<String, Object>();
            Paging<SpuBundle> res = spuBundleDao.paging(cris, 0, 20);
            assertEquals(total, res.getTotal().longValue());
            assertEquals(20, res.getData().size());
            assertEquals("spuBundle.name0", res.getData().get(0).getName());

            cris.clear();
            cris.put("status", SpuBundle.Status.OFF.value());
            res = spuBundleDao.paging(cris, 0, 20);
            assertEquals(offCount, res.getTotal().intValue());
            assertEquals(20L, res.getData().size());

            cris.clear();
            cris.put("status", SpuBundle.Status.ON.value());
            res = spuBundleDao.paging(cris, 0, 20);
            assertEquals(onCount, res.getTotal().intValue());
            assertEquals(20L, res.getData().size());

            cris.clear();
            cris.put("name", "name");
            res = spuBundleDao.paging(cris, 0, 20);
            assertEquals(total, res.getTotal().intValue());
            assertEquals(20L, res.getData().size());
    }
}
