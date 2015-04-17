package com.rrs.third.common;

/**
 * Created by zhaop01 on 2014/9/3.
 */
public enum ThirdPartEnum {
    HAIER("haier"),
    EHAIER("ehaier");

    private String name;

    private ThirdPartEnum(String name){
        this.name = name;
    }
    public static void main(String[] args){

        String value = ThirdPartEnum.HAIER.name;
        System.out.println(value);

    }
}
