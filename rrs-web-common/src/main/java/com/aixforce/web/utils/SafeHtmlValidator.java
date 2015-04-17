package com.aixforce.web.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import jodd.lagarto.dom.Node;

import java.util.Set;

/**
 * made by IntelliJ
 * author: haolin
 * at: 14-4-16.
 */
public class SafeHtmlValidator {

    private static final String ALL = "*";

    private static final String SCRIPT_TAG = "script";

    /**
     * html元素上的事件
     */
    private static final Set<String> TAGS_EVENTS_SETS = Sets.newHashSet(
            "onabort", "onblur", "onhelp", "onchange", "onclick", "ondbclick",
            "onerror", "onfocus", "onkeydown", "onkeypress", "onkeyup",
            "onload", "onmousedown", "onmousemove", "onmouseout", "onmouseover",
            "onmouseup", "onreset", "onresize", "onselect", "onsubmit", "onunload"
    );


    /**
     * 检查html中是否含有script标签脚本
     * true: 有script, false: 无script
     */
    public static boolean checkScript(String html) {
        if (Strings.isNullOrEmpty(html)) {
            return false;
        }
        Jerry doc = Jerry.jerry(html);
        return checkScript(doc);
    }

    private static boolean checkScript(Jerry doc) {
        return doc.$(SCRIPT_TAG).length() > 0;
    }

    /**
     * 检查html元素上是否有监听事件
     * true: 有监听事件, false: 无监听事件
     */
    public static boolean checkEvent(String html) {
        if (Strings.isNullOrEmpty(html)) {
            return false;
        }
        Jerry doc = Jerry.jerry(html);
        return checkEvent(doc);
    }

    private static boolean checkEvent(Jerry doc) {
        Node[] ns = doc.$(ALL).get();
        Attribute attr;
        for (Node n : ns) {
            int attrCount = n.getAttributesCount();
            for (int i = 0; i < attrCount; i++) {
                attr = n.getAttribute(i);
                if (TAGS_EVENTS_SETS.contains(attr.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查html内容是否有script或监听事件
     * 包含脚本或事件，返回true, 否则返回false
     */
    public static boolean checkScriptAndEvent(String html) {
        if (Strings.isNullOrEmpty(html)) {
            return false;
        }
        Jerry doc = Jerry.jerry(html);
        return checkScript(doc) || checkEvent(doc);
    }
}
