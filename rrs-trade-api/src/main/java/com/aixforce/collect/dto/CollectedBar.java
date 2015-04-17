package com.aixforce.collect.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-14 1:48 PM  <br>
 * Author: xiao
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CollectedBar implements Serializable {

    private static final long serialVersionUID = -8702757347823394638L;

    @Getter
    @Setter
    private Long itemId;

    @Getter
    @Setter
    private Boolean hasCollected;
}
