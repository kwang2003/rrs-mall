package com.aixforce.rrs.jde.mdm;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-25 2:19 PM  <br>
 * Author: xiao
 */
@Slf4j
public class JdeMdmRequest {

    private final static XStream xstream = new XStream();
    private Map<String, Object> params = Maps.newHashMap();

    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";


    private String url;

    static {
        xstream.registerConverter(new MapEntryConverter());
        xstream.alias("in", Map.class);
    }

    private JdeMdmRequest(String url) {
        this.url = url;
        params.put("Department", "rrs.com");
    }

    public static JdeMdmRequest build(String url) {
        return new JdeMdmRequest(url);
    }

    public JdeMdmRequest pageNo(Integer pageNo) {
        params.put("page", pageNo);
        return this;
    }

    public JdeMdmRequest startAt(Date startAt) {
        params.put("startDate", DFT.print(new DateTime(startAt)));
        return this;
    }

    public JdeMdmRequest endAt(Date endAt) {
        params.put("endDate", DFT.print(new DateTime(endAt)));
        return this;
    }

    @SuppressWarnings("unused")
    public JdeMdmRequest taxNo(String taxNo) {
        params.put("TAXNO", taxNo);
        return this;
    }

    public String test(){
        String xml = xstream.toXML(params);
        log.debug("send: {}", xml);
        String ack = HttpRequest.post(url).send(xml).connectTimeout(3000).readTimeout(3000).body();
        log.debug("ack: {}", ack);
        ack = ack.replace("&lt;", "<");
        ack = ack.replace("&gt;", ">");
        return ack;
    }

    public MdmPagingResponse load() {
        return load(null);
    }


    /**
     * 加载数据
     * @param min 限制数据的时间有效范围(分钟)
     */
    public MdmPagingResponse load(Integer min) {
        String xml = xstream.toXML(params);
        log.info("send: {}", xml);
        String ack = HttpRequest.post(url).send(XML_HEADER + xml).connectTimeout(3000).readTimeout(3000).body();
        log.info("ack: {}", ack);

        return convertToMdmPagingResponse(ack, min);
    }

    private MdmPagingResponse convertToMdmPagingResponse(String ack, Integer min) {
        MdmPagingResponse result = new MdmPagingResponse();

        if (isEmpty(ack)) {
            result.setError("jde.mdm.ack.empty");
            return result;
        }

        // 转义一些字符
        ack = ack.replace("&lt;", "<");
        ack = ack.replace("&gt;", ">");

        Document xmlDoc = Jsoup.parse(ack, "", Parser.xmlParser());
        Element error = xmlDoc.select("MESSAGE").first();

        if (notNull(error)) {   // EAI返回错误信息
            Element msg = xmlDoc.select("FAULT").first();
            result.setError(error.text().trim(), notNull(msg) ? msg.text().trim() : "");
            return result;
        }

        List<MdmUpdating> data = Lists.newArrayList();
        Elements rows = xmlDoc.select("row");
        for (Element row : rows) {
            try {
                MdmUpdating updating = convertToMdmUpdating(row, min);
                data.add(updating);
            } catch (Exception e) {
                log.error("fail to convert row to updating with row:{}", row, e);
            }
        }

        result.setResult(data);

        Integer countPage = Integer.parseInt(xmlDoc.select("countPage").text().trim());
        Integer currentPage = Integer.parseInt(xmlDoc.select("currentPage").text().trim());
        result.setNext(!equalWith(currentPage, countPage) && data.size() != 0L);

        return result;
    }


    private MdmUpdating convertToMdmUpdating(Element row, Integer min){
        MdmUpdating updating = new MdmUpdating();

        Element updateDateEle = row.select("PCC_GXTIME").first();
        checkState(notNull(updateDateEle), "row.field.empty");
        String updateDate = updateDateEle.text().trim();
        updateDate = updateDate.replace("T", " ");
        Date updatedAt = DFT.parseDateTime(updateDate).toDate();

        if (notNull(min)) {
            checkState(Minutes.minutesBetween(new DateTime(updatedAt), DateTime.now()).getMinutes() <= min, "data.time.range.incorrect");
        }
        updating.setUpdatedAt(updatedAt);

        Element taxNoEle = row.select("PCC_TAXNO").first();
        checkState(notNull(taxNoEle), "row.field.empty");
        updating.setTaxNo(taxNoEle.text().trim());

        Element outerCodeEle = row.select("PCC_MDMCODE").first();
        checkState(notNull(outerCodeEle), "row.field.empty");
        updating.setOuterCode(outerCodeEle.text().trim());

        return updating;
    }

}
