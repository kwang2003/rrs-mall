package com.aixforce.web;

import com.aixforce.search.ESClient;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-25
 */
@Controller
@Slf4j
@RequestMapping("/api")
public class Miscs {

    @Autowired
    private ESClient esClient;

    private final Splitter splitter = Splitter.on("$").omitEmptyStrings().trimResults().limit(2);

    private final LoadingCache<String,List<String>> suggestCache;

    public Miscs() {
        this.suggestCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(String key) throws Exception {
                        List<String> parts = splitter.splitToList(key);
                        String indexName = parts.get(0);
                        String term = parts.get(1);
                        return esClient.suggest(indexName, "name", term);
                    }
                });
    }

    /**
     * 供前台异步获取吊顶信息
     * @return 登陆用户信息
     */
    @RequestMapping(value = "/ceiling", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BaseUser userInfo(){
        return UserUtil.getCurrentUser();
    }


    @RequestMapping(value="/suggest",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> suggest(@RequestParam("t") String indexName,@RequestParam("q")String term){
        return suggestCache.getUnchecked(indexName+"$"+term);
    }
}
