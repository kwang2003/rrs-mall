package com.aixforce.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by zhum01 on 2014/11/21.
 */
public class JsonValue implements Serializable {
    @Getter
    @Setter
    private Long status;

    @Getter
    @Setter
    private String data;

    @Getter
    @Setter
    private String points;

    @Getter
    @Setter
    private String message;
}
