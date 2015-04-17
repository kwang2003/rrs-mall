package com.aixforce.rrs.jde.mdm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-25 3:33 PM  <br>
 * Author: xiao
 */
@ToString
public class MdmUpdating implements Serializable {

    private static final long serialVersionUID = 1566512047483358018L;

    @Getter
    @Setter
    private String taxNo;

    @Getter
    @Setter
    private String outerCode;

    @Getter
    @Setter
    private Date updatedAt;

}
