package com.aixforce;

import com.aixforce.seg.SmallSeg;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;


public class Benchmark {
    public static void main(String[] args) throws IOException {
        String text = Resources.toString(Resources.getResource("text.txt"), Charsets.UTF_8);
        for (int i = 1; i < 101; i++) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < i; j++)
                SmallSeg.instance().cut(text);
            long cost = System.currentTimeMillis() - start;
            System.out.println(i + "times,cost:" + cost);
        }
    }
}
