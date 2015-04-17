package com.aixforce.rrs.code.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.code.model.CodeUsage;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notNull;

/**
 * Created by songrefnei on 7/3/14.
 */

@Repository
public class CodeUsageDao extends SqlSessionDaoSupport {

    public Long create(CodeUsage codeusage) {
        getSqlSession().insert("CodeUsage.create", codeusage);
        return codeusage.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("CodeUsage.delete", id) == 1;
    }

    public boolean update(CodeUsage codeusage) {
        return getSqlSession().update("CodeUsage.update", codeusage) == 1;
    }

    public CodeUsage findById(Long id) {
        return getSqlSession().selectOne("CodeUsage.findById", id);
    }

    public CodeUsage findByName(String codename) {
        return getSqlSession().selectOne("CodeUsage.findByName", codename);
    }

    public Paging<CodeUsage> getCodeUsageByActivityId(Long activityId,Integer pageNo,Integer count){
        Long total = getSqlSession().selectOne("CodeUsage.countCodeUsageByActivityId", activityId);
        if (total==0) {
            return new Paging<CodeUsage>(0L, Collections.<CodeUsage>emptyList());
        }

                List<CodeUsage> cuList = getSqlSession().selectList("CodeUsage.getCodeUsageByActivityId", ImmutableMap.of("activityId",activityId,
                "pageNo",pageNo,"count",count));

        if(notNull(cuList)){
            return new Paging<CodeUsage>(total, cuList);
        }else{
            return new Paging<CodeUsage>(0L, Collections.<CodeUsage>emptyList());
        }
    }

    public List<CodeUsage> getAllCodeUsageByActivityId(Long activityId){

        return getSqlSession().selectList("CodeUsage.getAllCodeUsageByActivityId",activityId);
    }

    public CodeUsage getCodeUsageByOrderId(Long orderId){

        return getSqlSession().selectOne("CodeUsage.getCodeUsageByOrderId",orderId);
    }


    /**
     * 更新总订单的code
     * @param oldId
     * @param newId
     */
    public void updateOrderId(Long oldId, Long newId) {
        if(oldId==null||newId==null){
            return;
        }
        getSqlSession().update("CodeUsage.updateOrderId", ImmutableMap.of("oldId", oldId, "newId", newId));
    }
}
