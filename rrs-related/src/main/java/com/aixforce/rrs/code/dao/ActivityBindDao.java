package com.aixforce.rrs.code.dao;

import com.aixforce.rrs.code.model.ActivityBind;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by songrefnei on 7/3/14.
 */

@Repository
public class ActivityBindDao extends SqlSessionDaoSupport {

    public Long create(ActivityBind activitybind) {
        getSqlSession().insert("ActivityBind.create", activitybind);
        return activitybind.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("ActivityBind.delete", id) == 1;
    }

    public boolean update(ActivityBind activitybind) {
        return getSqlSession().update("ActivityBind.update", activitybind) == 1;
    }

    public ActivityBind findById(Long id) {
        return getSqlSession().selectOne("ActivityBind.findById", id);
    }

    public ActivityBind findByActivityId(Long activityId) {
        return getSqlSession().selectOne("ActivityBind.findByActivityId", activityId);
    }



    public List<Long> findBindIdsByActivityId(Long activityId, Integer targetType){
        return  getSqlSession().selectList("ActivityBind.findBindIdsByActivityId", ImmutableMap.of("activityId", activityId, "targetType", targetType));
    }


    public boolean deleteActivityBindByActivityId(Long activityId){
            return getSqlSession().delete("ActivityBind.deleteActivityBindByActivityId", activityId) == 1;
    }
}
