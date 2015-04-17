package com.rrs.brand.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yea01 on 2014/9/11.
 */
public class Addresses implements Serializable {
    private static final long serialVersionUID = -5712530324361262501L;
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String cityName;
}
