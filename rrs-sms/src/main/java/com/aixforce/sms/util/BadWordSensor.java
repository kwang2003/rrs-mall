package com.aixforce.sms.util;

import com.aixforce.seg.SmallSeg;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 违禁词过滤
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-06-04
 */
public class BadWordSensor {

    private final Set<String> badWords = Sets.newHashSetWithExpectedSize(2500);

    public BadWordSensor() throws Exception {
        Resources.readLines(Resources.getResource("dic/badword.dic"), Charsets.UTF_8, new LineProcessor<Void>() {
            @Override
            public boolean processLine(String s) throws IOException {
                if (!Strings.isNullOrEmpty(s) && s.trim().length() > 0) {
                    badWords.add(s.trim());
                }
                return true;
            }

            @Override
            public Void getResult() {
                return null;
            }
        });
    }

    /**
     * 检查输入内容是否含有敏感词
     *
     * @param input 输入内容
     * @return 内容中的敏感词列表, 如果列表为空, 则表示内容合法
     */
    public List<String> filterBadWords(String input) {
        List<String> result = SmallSeg.instance().cut(input);
        List<String> filtered = Lists.newArrayList();
        for (String word : result) {
            if (badWords.contains(word)) {
                filtered.add(word);
            }
        }
        return filtered;
    }
}
