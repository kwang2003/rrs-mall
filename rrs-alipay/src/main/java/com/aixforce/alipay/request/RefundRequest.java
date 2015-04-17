package com.aixforce.alipay.request;

import com.aixforce.alipay.dto.AlipayRefundData;
import com.aixforce.common.model.Response;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-25 11:47 AM  <br>
 * Author: xiao
 */
@Slf4j
public class RefundRequest extends Request {

    private static final Joiner DETAIL_JOINER = Joiner.on("^").skipNulls();
    private static final Joiner REFUND_JOINER = Joiner.on("#").skipNulls();
    private static final DateTimeFormatter DFT_BATCH = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter DFT_REFUND = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");



    private RefundRequest(Token token) {
        super(token);
        params.put("service", "refund_fastpay_by_platform_nopwd");
        params.put("refund_date", DFT_REFUND.print(DateTime.now()));
    }

    public static RefundRequest build(Token token) {
        return new RefundRequest(token);
    }

    private String getRefundDetail(List<AlipayRefundData> refunds) {
        List<String> refundDetails = Lists.newArrayListWithCapacity(refunds.size());
        for (AlipayRefundData refund : refunds) {
            String tradeNo = refund.getTradeNo();
            Integer refundAmount = refund.getRefundAmount();
            String reason = refund.getReason();
            String detail = DETAIL_JOINER.join(tradeNo, DECIMAL_FORMAT.format(refundAmount / 100.0), reason);
            refundDetails.add(detail);
        }
        return REFUND_JOINER.join(refundDetails);
    }


    /**
     * 后台通知
     * @param notify 通知
     */
    public RefundRequest notify(CallBack notify) {
        if (notNull(notify)) {
            params.put("notify_url", notify);
        }
        return this;
    }


    /**
     * 批次号，根据订单生成
     * @param batchNo 批次号
     */
    public RefundRequest batch(String batchNo) {
        checkArgument(!Strings.isNullOrEmpty(batchNo), "alipay.refund.batch.no.empty");
        params.put("batch_no", batchNo);
        return this;
    }

    /**
     * 退货详情
     * @param refunds 退货列表
     */
    public RefundRequest detail(List<AlipayRefundData> refunds) {
        String detail = getRefundDetail(refunds);
        params.put("detail_data", detail);
        params.put("batch_num", refunds.size() + "");
        return this;
    }


    /**
     * 向支付宝网关发送退货请求
     *
     * @return 退货请求结果
     */
    public Response<Boolean> refund() {
        String url = super.url();
        log.debug("refund url: {}", url);
        String body = HttpRequest.get(url).connectTimeout(10000).readTimeout(10000).body();
        log.debug("refund result: {}", body);
        return convertToResponse(body);
    }



    /**
     * 根据子订单标识<br/>
     * 8位当天日期 + 24位流水（中间补0)
     *
     * @param refundAt      退款时间
     * @param orderItemId   子订单编号
     * @return  支付宝退款批次号
     */
    public static String toBatchNo(Date refundAt, Long orderItemId) {
        checkArgument(orderItemId != null, "order.item.id.null");
        checkArgument(refundAt != null, "refund.at.null");

        String prefix = DFT_BATCH.print(new DateTime(refundAt));
        String suffix = "000000000000000000000000" + orderItemId;
        suffix = suffix.substring(suffix.length() - 24, suffix.length());
        return prefix + suffix;
    }

    /**
     * 将支付宝退款批次号转换成对应的子订单号
     *
     * @param batchNo   批次号
     * @return  子订单id
     */
    public static Long fromBatchNo(String batchNo) {
        checkArgument(!Strings.isNullOrEmpty(batchNo), "batch.no.empty");
        checkArgument(batchNo.length() == 32, "batch.no.length.illegal");
        int len = batchNo.length();
        return Long.valueOf(batchNo.substring(len - 24, len));
    }

    @Override
    public void sign() {
        try {
            super.sign();
            String refundDate = (String)params.get("refund_date");
            if (!Strings.isNullOrEmpty(refundDate)) {
                params.put("refund_date", URLEncoder.encode(refundDate, "utf-8"));
            }

            String detailData = (String)params.get("detail_data");
            if (!Strings.isNullOrEmpty(detailData)) {
                params.put("detail_data", URLEncoder.encode(detailData, "utf-8"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
