package com.aixforce.alipay;

import com.aixforce.alipay.dto.AlipayRefundData;
import com.aixforce.alipay.dto.AlipaySyncResponse;
import com.aixforce.common.model.Response;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-05-30
 */
@Slf4j
public class AlipayUtil {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DFT_BATCH = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter DFT_TIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


    private static final Joiner detailJoiner = Joiner.on("^").skipNulls();
    private static final Joiner refundJoiner = Joiner.on("#").skipNulls();

    private static XStream xStream;

    static {
        xStream = new XStream();
        xStream.autodetectAnnotations(true);
        xStream.processAnnotations(AlipaySyncResponse.class);
    }

    /**
     *
     * @param params    构造权限
     * @return  查询参数
     */
    private static Map<String, String> buildParams(Map<String, String> params) {
        try {
            String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);

            String sign = Hashing.md5().newHasher()
                    .putString(toVerify, Charsets.UTF_8)
                    .putString(AlipayConfig.ALIPAY_KEY, Charsets.UTF_8).hash().toString();

            params.put("sign", sign);
            params.put("sign_type", "MD5");
            String subject = params.get("subject");
            if (!Strings.isNullOrEmpty(subject)) {
                params.put("subject", URLEncoder.encode(subject, "utf-8"));
            }

            String detail = params.get("detail_data");
            if (!Strings.isNullOrEmpty(detail)) {
                params.put("detail_data", URLEncoder.encode(detail, "utf-8"));
            }

            return params;
        }  catch (Exception e) {
            log.error("failed to build params", e);
            throw new RuntimeException("failed to build params", e);
        }
    }


    /**
     * 构建即时到帐接口的查询参数
     *
     * @param ids  订单id(对应外部id)
     * @param subject  商品名称
     * @param totalFee 总价,以分表示
     */
    public static Map<String, String> buildParams(String ids, String subject, Integer totalFee, boolean enableScanCode) {
        Map<String, String> params = Maps.newTreeMap();
        params.put("service", "create_direct_pay_by_user");
        params.put("partner", AlipayConfig.ALIPAY_PID);
        params.put("_input_charset", "utf-8");
        params.put("out_trade_no", String.valueOf(ids));
        params.put("subject", subject);
        params.put("payment_type", "1");
        params.put("seller_email", AlipayConfig.SELLER_ACCOUNT);
        params.put("total_fee", DECIMAL_FORMAT.format(totalFee / 100.0));
        params.put("return_url", AlipayConfig.RETURN_URL);
        params.put("notify_url", AlipayConfig.NOTIFY_URL);

        if (enableScanCode) {
            params.put("qr_pay_mode", "2");
        }

        return buildParams(params);
    }

    /**
     * 构建退款参数列表
     *
     * @param refunds     退款信息
     * @return 参数列表
     */
    public static Map<String, String> buildRefundParams(String batchNo, List<AlipayRefundData> refunds) {
        checkArgument(!Strings.isNullOrEmpty(batchNo), "illegal batch no");
        if (refunds == null || refunds.size() == 0) {
            throw new IllegalArgumentException("refunds cannot be empty");
        }

        Map<String, String> params = Maps.newTreeMap();
        params.put("service", "refund_fastpay_by_platform_nopwd");
        params.put("partner", AlipayConfig.ALIPAY_PID);
        params.put("_input_charset", "utf-8");
        params.put("batch_no", batchNo);
        params.put("batch_num", refunds.size() + "");
        String detail = getRefundDetail(refunds);
        params.put("detail_data", detail);
        params.put("notify_url", AlipayConfig.REFUND_NOTIFY_URL);

        return buildParams(params);
    }

    /**
     * 获取退款详情
     * 第一笔交易#第二笔交易#第三笔交易, 以'#'分割<br/>
     * 每笔交易: 原付款支付宝交易号^退款总金额^退款理由<br/>
     * 样例: <br/>
     * 20000000123^100.00^退定金#20000000123^50.00^退尾款
     *
     *
     * @param refunds 退款列表
     * @return  退款详情
     */
    private static String getRefundDetail(List<AlipayRefundData> refunds) {
        List<String> refundDetails = Lists.newArrayListWithCapacity(refunds.size());
        for (AlipayRefundData refund : refunds) {
            String tradeNo = refund.getTradeNo();
            Integer refundAmount = refund.getRefundAmount();
            String reason = refund.getReason();
            String detail = detailJoiner.join(tradeNo, DECIMAL_FORMAT.format(refundAmount / 100.0), reason);
            refundDetails.add(detail);
        }
        return refundJoiner.join(refundDetails);
    }


    /**
     * 构造url链接, 此方法不会开启扫码方式支付
     *
     * @param ids      订单标识
     * @param subject  商品名称
     * @param totalFee 总价,以分表示
     * @return  支付宝即时到帐支付请求
     */
    public static String buildUrl(String ids, String subject, Integer totalFee) {
        return buildUrl(ids, subject, totalFee, false);
    }

    /**
     * 构造url链接
     *
     * @param ids        订单id
     * @param subject        商品名称
     * @param totalFee       总价,以分表示
     * @param enableScanCode 是否以扫码方式支付
     * @return  支付宝即时到帐支付请求
     */
    public static String buildUrl(String ids, String subject, Integer totalFee, boolean enableScanCode) {
        Map<String, String> params = buildParams(ids, subject, totalFee, enableScanCode);
        String suffix = Joiner.on('&').withKeyValueSeparator("=").join(params);
        return AlipayConfig.ALIPAY_GATEWAY_NEW + suffix;
    }

    /**
     * 构造退款的url链接
     *
     * @param   batchNo  批次号
     * @param   refunds  多笔退款
     * @return  支付报退款URL请求
     */
    public static String buildRefundUrl(String batchNo, List<AlipayRefundData> refunds) {
        Map<String,String> params = buildRefundParams(batchNo, refunds);
        String suffix = Joiner.on('&').withKeyValueSeparator("=").join(params);
        return AlipayConfig.ALIPAY_GATEWAY_REFUND + suffix;
    }

    /**
     * 去支付宝退款
     *
     * @param batchNo   批次号
     * @param refunds   退款信息列表
     *
     * @return 是否请求成功,不代表处理成功
     */
    public static Response<Boolean> refund(String batchNo, List<AlipayRefundData> refunds) {
        Response<Boolean> result = new Response<Boolean>();
        String url = buildRefundUrl(batchNo, refunds);
        String body = HttpRequest.get(url).connectTimeout(10000).readTimeout(10000).body();

        if (Strings.isNullOrEmpty(body)) {
            result.setError("alipay.refund.fail");
            return result;
        }

        AlipaySyncResponse refundResponse = (AlipaySyncResponse)xStream.fromXML(body);
        if (!refundResponse.isSuccess()) {
            log.error("Invoke alipay raise error={}", refundResponse.getError());
            result.setError("alipay.refund.fail");
            return result;
        }

        result.setResult(Boolean.TRUE);
        return result;
    }


    /**
     * 验证签名
     * @param params    参数
     * @param sign      签名
     * @return  校验通过
     */
    public static boolean verify(Map<String, String> params, String sign) {
        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);

        String expect = Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(AlipayConfig.ALIPAY_KEY, Charsets.UTF_8).hash().toString();
        return Objects.equal(expect, sign);
    }

    private static Map<String, String> buildQuerySettlementParams(int pageNo, int pageSize,
                                                   String transCode,
                                                   Date beginAt, Date endAt) {
        Map<String, String> params = Maps.newTreeMap();
        params.put("service", "account.page.query");
        params.put("partner", AlipayConfig.ALIPAY_PID);
        params.put("_input_charset", "utf-8");
        params.put("page_ no", String.valueOf(pageNo));
        params.put("page_size",String.valueOf(pageSize));
        params.put("trans_code", transCode);
        params.put("gmt_start_time", DFT_TIME.print(new DateTime(beginAt)));
        params.put("gmt_end_time", DFT_TIME.print(new DateTime(endAt)));


        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);
        String sign = Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(AlipayConfig.ALIPAY_KEY, Charsets.UTF_8).hash().toString();

        params.put("sign", sign);
        params.put("sign_type", "MD5");

        return params;
    }


    public static String buildQuerySettlementUrl(int pageNo, int pageSize,
                                                  String transCode,
                                                  Date beginAt, Date endAt) {

        Map<String, String> params = buildQuerySettlementParams(pageNo, pageSize, transCode, beginAt, endAt);
        String suffix = Joiner.on('&').withKeyValueSeparator("=").join(params);
        return AlipayConfig.ALIPAY_GATEWAY_NEW + suffix;
    }




}
