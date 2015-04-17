package com.aixforce.alipay.dto;

import com.aixforce.alipay.dto.settlement.AlipaySettlementParam;
import com.aixforce.alipay.dto.settlement.AlipaySettlementResult;
import com.google.common.base.Objects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static com.aixforce.common.utils.Arguments.*;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 10:37 AM  <br>
 * Author: xiao
 */
@ToString
@XStreamAlias("alipay")
public class AlipaySettlementResponse {

    public Boolean isSuccess() {
        return notEmpty(success) && Objects.equal(success, "T");
    }


    @Setter
    @XStreamAlias("is_success")
    private String success = "F";             // 是否成功，T-成功，F-失败

    @Getter
    @Setter
    @XStreamAlias("request")
    private List<AlipaySettlementParam> request;    // 请求


    @Getter
    @Setter
    @XStreamAlias("response")
    private AlipaySettlementResult result;      // 返回结果


    @Getter
    @Setter
    @XStreamAlias("sign")
    private String sign;                        // 签名


    @Getter
    @Setter
    @XStreamAlias("sign_type")
    private String signType;                    // 签名方式




    public Boolean hasNextPage() {
        return notNull(result) && notNull(result.getPaging())
                && notEmpty(result.getPaging().getHasNextPage())
                && equalWith(result.getPaging().getHasNextPage(), "T");
    }

}
