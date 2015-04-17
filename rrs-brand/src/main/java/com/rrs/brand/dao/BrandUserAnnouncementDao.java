package com.rrs.brand.dao;


import com.aixforce.common.model.Response;
import com.google.common.collect.ImmutableMap;
import com.rrs.brand.model.BrandUserAnnouncement;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zhum01 on 2014/7/23.
 */

@Repository
public class BrandUserAnnouncementDao extends SqlSessionDaoSupport{
    public List<BrandUserAnnouncement> findAll(int id){
        return getSqlSession().selectList("BrandUserAnnouncement.findAll",id);
    }

    public List<BrandUserAnnouncement> findByTime(String starttime,String endtime){
        return getSqlSession().
                selectList("BrandUserAnnouncement.findByTime", ImmutableMap.of("starttime", starttime, "endtime", endtime));
    }

    public List<BrandUserAnnouncement> findByShopId(int shopid){
        return getSqlSession().
                selectList("BrandUserAnnouncement.findByShopId", shopid);
    }

    public BrandUserAnnouncement findById(int id){
        return getSqlSession().selectOne("BrandUserAnnouncement.findById",id);
    }

    public void addAnn(BrandUserAnnouncement brandUserAnnouncement){
        getSqlSession().insert("BrandUserAnnouncement.addAnn",brandUserAnnouncement);
    }

    public void delAnn(int[] idlist){
        int id=0;
        for(int i=0;i<idlist.length;i++){
            id=idlist[i];
            getSqlSession().delete("BrandUserAnnouncement.delAnn",id);
        }
    }
}
