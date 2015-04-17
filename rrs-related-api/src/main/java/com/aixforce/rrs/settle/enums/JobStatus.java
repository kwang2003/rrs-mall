package com.aixforce.rrs.settle.enums;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-13 10:12 AM  <br>
 * Author: xiao
 */
public enum JobStatus {

    FAIL(-1, "处理失败"),
    NOT(0, "未处理"),
    ING(1, "处理中"),
    DONE(2, "处理完成");


    private final int value;

    private final String description;

    private JobStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int value() {
        return this.value;
    }

    @Override
    public String toString() {
        return description;
    }
}
