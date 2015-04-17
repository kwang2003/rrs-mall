package com.aixforce.alipay.dto.settlement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 10:43 AM  <br>
 * Author: xiao
 */
@ToString
public class AlipaySettlementPaging {


    @Getter
    @Setter
    @XStreamAlias("account_log_list")
    private List<AlipaySettlementDto> accountLogList;


    @Getter
    @Setter
    @XStreamAlias("has_next_page")
    private String hasNextPage = "F";

    @Getter
    @Setter
    @XStreamAlias("page_no")
    private String pageNo;

    @Getter
    @Setter
    @XStreamAlias("page_size")
    private String pageSize;
}
