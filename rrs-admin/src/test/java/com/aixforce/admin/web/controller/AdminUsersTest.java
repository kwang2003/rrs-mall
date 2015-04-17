package com.aixforce.admin.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created with IntelliJ IDEA.
 * User: yangzefeng
 * Date: 13-11-5
 * Time: 下午5:35
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("classpath*:/spring/root-context.xml")
public class AdminUsersTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    public void testPass() throws Exception {
        ResultActions ra = this.mockMvc.perform(MockMvcRequestBuilders
                .get(""));
    }

    @Test
    public void testFindShops() throws Exception {
        ResultActions ra = this.mockMvc.perform(MockMvcRequestBuilders
                .get("/users/shops/1"))
                .andExpect(MockMvcResultMatchers.status().is(200));
//                .andExpect(MockMvcResultMatchers.content().toString().equals(""));
//                .andExpect(MockMvcResultMatchers);
    }

    @Test
    public void testFindShopById() throws Exception {

    }

    @Test
    public void testFindBy() throws Exception {

    }
}
