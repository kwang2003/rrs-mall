package com.aixforce.web;

import com.aixforce.search.ESClient;
import com.aixforce.search.ESHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-07-15
 */
@Component
public class ESInitiator {

    private static final Set<String> indices = ImmutableSet.<String>builder()
            .add("shops/shop")
            .add("items/item")
            .build();

    private final ESClient esClient;

    @Autowired
    public ESInitiator(ESClient esClient) {
        this.esClient = esClient;
    }

    @PostConstruct
    public void init() {
        for (String index : indices) {
            List<String> parts = Splitter.on('/').omitEmptyStrings().trimResults().splitToList(index);
            ESHelper.createIndexIfNeeded(esClient.getClient(), parts.get(0), parts.get(1));
        }
    }
}
