package com.rrs.brand.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yea01 on 2014/9/11.
 */
public class ExperinceMall  implements Serializable {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String mallName;

}
