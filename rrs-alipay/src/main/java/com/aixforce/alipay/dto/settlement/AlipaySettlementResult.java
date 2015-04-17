package com.aixforce.alipay.dto.settlement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 10:42 AM  <br>
 * Author: xiao
 */
@ToString
public class AlipaySettlementResult {

    @Getter
    @Setter
    @XStreamAlias("account_page_query_result")
    private AlipaySettlementPaging paging;

}
