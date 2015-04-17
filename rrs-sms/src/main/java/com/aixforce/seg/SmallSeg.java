package com.aixforce.seg;

import java.util.List;

public class SmallSeg {
    private final Seg seg;

    private SmallSeg() {
        this.seg = new Seg();
        try {
            seg.useDefaultDict();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class SingletonHolder {
        static SmallSeg instance = new SmallSeg();
    }

    public static SmallSeg instance() {
        return SingletonHolder.instance;
    }

    public List<String> cut(String text) {
        return seg.cut(text);
    }
}
