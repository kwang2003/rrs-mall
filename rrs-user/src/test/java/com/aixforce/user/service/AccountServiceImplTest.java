/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.service;

import com.aixforce.user.BaseServiceTest;
import com.aixforce.user.model.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/*
* Author: jlchen
* Date: 2012-11-08
*/
public class AccountServiceImplTest extends BaseServiceTest {
    @Autowired
    private AccountService<User> accountService;

//    @Autowired
//    private PasswordService passwordService;

    private User user;

    @Before
    public void setUp() throws Exception {
        user = newUser("dadu", "test");
        accountService.createUser(user);
    }


    @Test
    public void testChangePassword() throws Exception {
        Long id = user.getId();
        String origin = user.getEncryptedPassword();
        accountService.changePassword(user.getId(), "test", "new");
        User updated = (User)accountService.findUserById(id).getResult();
        assertThat(origin, not(is(updated.getEncryptedPassword())));
    }

    private User newUser(String name, String password) {
        User user = new User();
        user.setEmail(name + "@example.com");
        user.setStatus(0);
        user.setEncryptedPassword(password);
        user.setName(name);
        user.setType(1);
        return user;
    }
}
