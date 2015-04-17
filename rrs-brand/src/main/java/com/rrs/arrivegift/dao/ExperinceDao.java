package com.rrs.arrivegift.dao;

import com.rrs.arrivegift.model.Experince;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by zhum01 on 2014/10/24.
 */
@Repository
public class ExperinceDao extends SqlSessionDaoSupport {
    public Experince queryExperinceByMap(Long shopId){
        return getSqlSession().selectOne("Experince.queryExperinceByMap", shopId);
    }

}
