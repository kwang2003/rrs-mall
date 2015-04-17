package com.aixforce.rrs.jde;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-19 2:09 PM  <br>
 * Author: xiao
 */
@ToString
@XStreamAlias("ROWSET")
public class JdeVoteResponse {

    @Getter
    @Setter
    @XStreamAlias("ROW")
    private JdeResult result;       // 如果success = true,则通过result可以获得调用结果

    @Getter
    @Setter
    @XStreamOmitField
    private Boolean success;


    public boolean isSuccess() {
        return result != null
                && !Strings.isNullOrEmpty(result.getStatus())
                && Objects.equal(result.getStatus(), "S");
    }


    public String getError() {
        checkState(notNull(result), "jde.result.empty");
        return result.getMessage();
    }
}
