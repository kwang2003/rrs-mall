package com.aixforce.alipay.request;

import com.aixforce.alipay.dto.AlipaySettlementResponse;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URLEncoder;
import java.util.Date;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-14 6:10 PM  <br>
 * Author: xiao
 */
@Slf4j
public class PageQueryRequest extends Request {

    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final XStream xstream = new XStream();

    static {
        xstream.autodetectAnnotations(true);
        xstream.processAnnotations(AlipaySettlementResponse.class);
    }


    private Date start;
    private Date end;


    private PageQueryRequest(Token token) {
        super(token);
        params.put("service", "account.page.query");
    }


    public static PageQueryRequest build(Token token) {
        return new PageQueryRequest(token);
    }


    public PageQueryRequest start(Date start) {             // 查询开始时间
        params.put("gmt_start_time", DFT.print(new DateTime(start)));
        this.start = start;
        return this;
    }

    public PageQueryRequest end(Date end) {                 // 查询截止时间
        params.put("gmt_end_time", DFT.print(new DateTime(end)));
        this.end = end;
        return this;
    }

    public PageQueryRequest pageNo(Integer pageNo) {        // 页码
        pageNo = Objects.firstNonNull(pageNo, 1);
        params.put("page_no", pageNo);
        return this;
    }

    public PageQueryRequest pageSize(Integer pageSize) {    // 页码
        pageSize = Objects.firstNonNull(pageSize, 1000);
        params.put("page_size", pageSize);
        return this;
    }

    public PageQueryRequest tradeNo(String tradeNo) {       // 支付宝交易流水
        params.put("trade_no", tradeNo);
        return this;
    }

    public PageQueryRequest merchantOutOrderNo(String merchantOutOrderNo) {       // 支付宝交易流水
        params.put("merchant_out_order_no", merchantOutOrderNo);
        return this;
    }


    public AlipaySettlementResponse query() {
        checkArgument(notNull(start), "start.can.not.be.empty");
        checkArgument(notNull(end),  "end.can.not.be.empty");
        DateTime startAt = new DateTime(start);
        DateTime endAt = new DateTime(end);
        // 间隔不能超过24小时
        checkArgument(endAt.minusHours(24).isBefore(startAt) || endAt.minusHours(24).isEqual(startAt));


        String url = super.url();
        log.debug("query url: {}", url);
        String body = HttpRequest.get(url).connectTimeout(10000000).readTimeout(10000000).body();
        return transform(body);
    }




    /**
     * 将支付宝响应报文转换成对象
     * @param xml 支付宝响应报文
     * @return AlipaySettlementResponse对象
     */
    private AlipaySettlementResponse transform (String xml) {
        return (AlipaySettlementResponse) xstream.fromXML(xml);
    }

    @Override
    public void sign() {
        try {
            super.sign();
            String begin = (String)params.get("gmt_start_time");
            if (!Strings.isNullOrEmpty(begin)) {
                params.put("gmt_start_time", URLEncoder.encode(begin, "utf-8"));
            }

            String end = (String)params.get("gmt_end_time");
            if (!Strings.isNullOrEmpty(end)) {
                params.put("gmt_end_time", URLEncoder.encode(end, "utf-8"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
