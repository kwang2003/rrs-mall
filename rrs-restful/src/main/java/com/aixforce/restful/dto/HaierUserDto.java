package com.aixforce.restful.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-10 9:52 PM  <br>
 * Author: xiao
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HaierUserDto implements Serializable {


    private static final long serialVersionUID = -7649075753473440068L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String mobile;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String token;

}
