package com.aixforce.user.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.user.model.User;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-12
 */
@Repository
public class UserDao extends SqlSessionDaoSupport {
    public User findById(Long id) {
        return getSqlSession().selectOne("User.findById", id);
    }

    public User findByEmail(String email) {
        return getSqlSession().selectOne("User.findByEmail", email);
    }

    public void create(User user) {
        getSqlSession().insert("User.create", user);
    }

    public int delete(Long id) {
        return getSqlSession().delete("User.delete", id);
    }

    public boolean update(User user) {
        return getSqlSession().update("User.update", user) == 1;
    }

    public void batchUpdateStatus(List<Long> ids, Integer status) {
        getSqlSession().update("User.batchUpdateStatus", ImmutableMap.of("ids", ids, "status", status));
    }

    public Paging<User> findUsers(Integer status, Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("User.count", status);
        List<User> users = getSqlSession().selectList("User.pagination",
                ImmutableMap.of("status", status, "offset", offset, "limit", limit));
        return new Paging<User>(total, users);
    }

    public User findByName(String name) {
        return getSqlSession().selectOne("User.findByName", name);
    }

    public User findByThirdPartTypeAndId(User.ThirdPartType thirdPartType, String thirdPartId) {
        return getSqlSession().selectOne("User.findByThirdPartTypeAndId",
                ImmutableMap.of("thirdPartType", thirdPartType.value(), "thirdPartId", thirdPartId));
    }

    public User findByMobile(String mobile) {
        return getSqlSession().selectOne("User.findByMobile", mobile);
    }

    public Paging<User> findByTypes(Integer offset, Integer size, List<Integer> list) {
        Long count = getSqlSession().selectOne("User.countByTypes", list);
        List<User> users = getSqlSession().selectList("User.findByTypes",
                ImmutableMap.of("offset", offset, "limit", size, "type", list));
        return new Paging<User>(count, users);
    }

    public Paging<User> findByType(Integer offset, Integer size, Integer type) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("offset", offset);
        map.put("limit", size);
        if (type != null) map.put("type", type);
        Long count = getSqlSession().selectOne("User.countByType", map);
        if (count == 0) {
            return new Paging<User>(0L, Collections.<User>emptyList());
        }
        List<User> users = getSqlSession().selectList("User.findByType", map);
        return new Paging<User>(count, users);
    }

    public List<User> findByIds(List<Long> ids) {
        return getSqlSession().selectList("User.findByIds", ids);
    }

    public Paging<User> paginationAll(Integer offset, Integer limit) {
        Long count = getSqlSession().selectOne("User.countAll");
        if(Objects.equal(count, 0l)) {
            return new Paging<User>(0l, Collections.<User>emptyList());
        }
        List<User> users = getSqlSession().selectList("User.findAll",
                ImmutableMap.of("offset", offset, "limit", limit));
        return new Paging<User>(count, users);
    }
}
