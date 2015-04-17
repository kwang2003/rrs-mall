package com.aixforce.user.mysql;

import com.aixforce.user.BaseMysqlDaoTest;
import com.aixforce.user.model.UserAccountSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-09 6:38 PM  <br>
 * Author: xiao
 */
public class UserAccountSummaryDaoTest extends BaseMysqlDaoTest {

    private UserAccountSummary u;

    @Autowired
    private UserAccountSummaryDao userAccountSummaryDao;

    @Before
    public void setUp() throws Exception {
        u = mock();
        userAccountSummaryDao.create(u);

        UserAccountSummary actual = userAccountSummaryDao.get(u.getId());
        u.setCreatedAt(actual.getCreatedAt());
        u.setUpdatedAt(actual.getUpdatedAt());
        assertThat(u, is(actual));
    }

    private UserAccountSummary mock() {
        UserAccountSummary mock = new UserAccountSummary();

        mock.setActivity("活动");
        mock.setChannel("渠道");
        mock.setFrom("来源");
        mock.setLoginType(3L);
        mock.setUserId(1L);
        mock.setUserName("买家");
        return mock;
    }


    @Test
    public void test() {}






}
