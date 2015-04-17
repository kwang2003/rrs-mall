package com.aixforce.user.mysql;

import com.aixforce.user.model.UserExtra;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by yangzefeng on 14-3-4
 */
@Repository
public class UserExtraDao extends SqlSessionDaoSupport {

    public void create(UserExtra userExtra) {
        getSqlSession().insert("UserExtra.create", userExtra);
    }

    public void update(UserExtra userExtra) {
        getSqlSession().update("UserExtra.update", userExtra);
    }

    public UserExtra findById(Long id) {
        return getSqlSession().selectOne("UserExtra.findById", id);
    }

    public UserExtra findByUserId(Long userId) {
        return getSqlSession().selectOne("UserExtra.findByUserId", userId);
    }

    public void replase(UserExtra extra) {
        getSqlSession().insert("UserExtra.replace", extra);
    }

    public void insertOrReplace(UserExtra extra) {
        getSqlSession().insert("UserExtra.insertOrReplace", extra);

    }

    public void insertOrUpdate(UserExtra extra) {
        getSqlSession().insert("UserExtra.insertOrUpdate", extra);

    }
}
