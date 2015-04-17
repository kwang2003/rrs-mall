package com.aixforce.user.mysql;

import com.aixforce.user.model.UserAccountSummary;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-09 6:27 PM  <br>
 * Author: xiao
 */
@Repository
public class UserAccountSummaryDao extends SqlSessionDaoSupport {


    public void create(UserAccountSummary userAccountSummary) {
        getSqlSession().insert("UserAccountSummary.create", userAccountSummary);
    }

    public UserAccountSummary get(Long id) {
        return getSqlSession().selectOne("UserAccountSummary.get", id);

    }

    public void delete(Long id) {
        getSqlSession().delete("UserAccountSummary.delete", id);
    }

}

