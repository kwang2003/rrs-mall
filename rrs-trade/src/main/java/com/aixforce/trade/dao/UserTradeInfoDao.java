package com.aixforce.trade.dao;


import com.aixforce.trade.model.UserTradeInfo;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-12
 */
@Repository
public class UserTradeInfoDao extends SqlSessionDaoSupport {

    @Nonnull
    public List<UserTradeInfo> findValidByUserId(@Nonnull Long userId) {
        return getSqlSession().selectList("UserTradeInfo.findValidByUserId", userId);
    }

    @Nullable
    public UserTradeInfo findById(@Nonnull Long id) {
        return getSqlSession().selectOne("UserTradeInfo.findById", id);
    }

    public void create(@Nonnull UserTradeInfo userTradeInfo) {
        getSqlSession().insert("UserTradeInfo.create", userTradeInfo);
    }

    @Deprecated
    public void delete(@Nonnull Long id) {
        getSqlSession().delete("UserTradeInfo.delete", id);
    }

    public void update(@Nonnull UserTradeInfo userTradeInfo) {
        getSqlSession().update("UserTradeInfo.update", userTradeInfo);
    }

    public void invalidate(Long id) {
        getSqlSession().update("UserTradeInfo.invalidate", id);
    }

    public void makeDefault(Long id) {
        UserTradeInfo userTradeInfo = new UserTradeInfo();
        userTradeInfo.setId(id);
        userTradeInfo.setIsDefault(1);
        update(userTradeInfo);
    }

    public Integer countOf(@Nonnull Long userId) {
        return getSqlSession().selectOne("UserTradeInfo.countOf", userId);
    }

    public List<UserTradeInfo> findInIds(Long... ids) {
        if (ids.length == 0)
            return Collections.emptyList();

        return getSqlSession().selectList("UserTradeInfo.findInIds", ImmutableMap.of("ids", ids));
    }


    public UserTradeInfo findDefault(Long userId) {
        return getSqlSession().selectOne("UserTradeInfo.findDefault", userId);
    }

    public List<UserTradeInfo> findTradeInfoByUserAndDistrict(Long userId,Integer districtId){
        return getSqlSession().selectList("UserTradeInfo.findTradeInfoByUserAndDistrict",ImmutableMap.of("userId",userId,"districtId",districtId));
    }
}
