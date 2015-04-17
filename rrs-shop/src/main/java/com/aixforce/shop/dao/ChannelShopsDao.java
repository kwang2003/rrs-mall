package com.aixforce.shop.dao;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jack.yang on 14-8-11.
 */
@Slf4j
@Repository
public class ChannelShopsDao extends SqlSessionDaoSupport{

    public List<HashMap> findChannelShops(String channel) {
        log.info("search.database.for.channel.info");
        return getSqlSession().selectList("ChannelShops.select", channel);
    }

    public List<HashMap> findAuthRoles(String method) {
        log.info("search.database.for.channel.auth:method{}", method);
        return getSqlSession().selectList("ChannelShops.authSelect", method);
    }

    public HashMap findSmsMessage(Long id) {
        log.info("search.database.for.smg.content:id{}", id);
        return getSqlSession().selectOne("ChannelShops.getSmsById", id);
    }
}
