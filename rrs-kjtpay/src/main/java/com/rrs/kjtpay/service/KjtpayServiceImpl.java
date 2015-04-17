package com.rrs.kjtpay.service;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.settle.service.SettlementService;
import com.rrs.kjtpay.core.KjtConfig;
import com.rrs.kjtpay.core.KjtCore;
import com.rrs.kjtpay.core.VerifyClient;
import com.rrs.kjtpay.dao.KjtpayDao;
import com.rrs.kjtpay.dto.BatchTransferToAccountAsyncNotice;
import com.rrs.kjtpay.dto.CreateAccredit;
import com.rrs.kjtpay.dto.CreateAccreditSyncNotice;
import com.rrs.kjtpay.model.KjtpayAccount;
import com.rrs.kjtpay.util.HttpClientUtilsExt;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


/**
 * 快捷通支付Service实现
 * @author jiangpeng
 * @createAt 2015/1/5 11:29
 */
@Service
public class KjtpayServiceImpl implements KjtpayService {


    private final static Logger log = LoggerFactory.getLogger(KjtpayServiceImpl.class);

    @Autowired
    private KjtpayDao kjtpayDao;

//    @Autowired
//    private SettlementService settlementService;

    @Autowired
    private KjtConfig kjtConfig;



    /**
     * 保存快捷通账户
     * @author jiangpeng
     * @createAt 2015/1/5 13:06
     * @param sParaTemp 帐号信息
     * @return 操作结果
     */
    public Response<Boolean> saveKjtpayAccount(Map<String, String> sParaTemp) {

        //构建快捷通支付帐号信息
        KjtpayAccount account = new KjtpayAccount();
        account.setPartnerId(sParaTemp.get("partner_id"));
        account.setPlatUserId(sParaTemp.get("plat_user_id"));
        account.setPlatUser(sParaTemp.get("plat_user"));
        account.setMemberId(sParaTemp.get("member_id"));
        account.setMemberName(sParaTemp.get("member_name"));

        //定义返回结果
        Response<Boolean> result = new Response<Boolean>();
        try{
            //执行插入操作并返回操作结果
            Boolean istrue = kjtpayDao.createKjtpayAccount(account);
            result.setResult(istrue);
        }catch(Exception e){
            log.error("failed to update brand, cause:", e);
            result.setError("kjtpayAccount.create.fail");
        }
        return result;
    }

    /**
     * 绑定接口调用
     * @author jiangpeng
     * @createAt 2015/1/5 13:32
     * @param createAccredit 绑定相关信息
     * @return 响应参数
     */
    public String createAccredit(CreateAccredit createAccredit) {
        Map<String,String> reqMap = new HashMap<String,String>();

        //基本请求参数
        reqMap.put("service", kjtConfig.getCreateAccreditService());//接口名称
        reqMap.put("version",kjtConfig.getVersion());//版本
        reqMap.put("partner_id",kjtConfig.getPartnerId());//商户号
        reqMap.put("_input_charset",kjtConfig.getInputCharset());//编码集
        reqMap.put("return_url",kjtConfig.getCreateAccreditReturnUrl());//同步通知url
        reqMap.put("memo",createAccredit.getMemo());//备注

        //业务请求参数
        reqMap.put("plat_user_id",createAccredit.getPartnerId());//当前用户id
        reqMap.put("plat_user",createAccredit.getPartnerName());//当前用户登录名
        //TODO 需要验证公司名称
        reqMap.put("compay_name",createAccredit.getCompanyName());//公司名称
        reqMap.put("check_flag",kjtConfig.getCreateAccreditCheckFlag());//是否检查公司名，0否1是

        //构建请求参数 包括 签名及签名类型
        try {
            reqMap = KjtCore.buildRequestPara(reqMap, kjtConfig.getSignType(), "", kjtConfig.getInputCharset());
            log.info("execute createAccredit url:"+kjtConfig.getGatewayUrl()+"?"+ KjtCore.createLinkString(reqMap,false));
            CloseableHttpResponse res = HttpClientUtilsExt.post(kjtConfig.getGatewayUrl(), reqMap, kjtConfig.getInputCharset());
            StatusLine line = res.getStatusLine();
            if (line.getStatusCode() == 302) {
                //重定向
               return  res.getLastHeader("Location").getValue();
            } else if (line.getStatusCode() == 200) {
                String result = EntityUtils.toString(res.getEntity());
                if (result.indexOf("is_success=F") != -1) {
                    //响应 并且异常
                    log.error("kjtpay createAccredit error:{}",result);
                    //throw new JsonResponseException(500, result);
                    return KjtCore.getResultValue(result,"memo");
                }
            }
        } catch (Exception e) {
            log.error("failed to execute createAccredit by Kjt, cause:{}", e.getMessage());
            e.printStackTrace();
        }

        //未响应 或出现异常
        return null;

    }


    /**
     * 处理账户绑定同步通知
     * @author jiangpeng
     * @createAt 2015/1/6 9:46
     * @param notice
     * @return 响应结果
     */
    @Transactional
    public Response<Boolean> executeCreateAccreditSyncNotice(CreateAccreditSyncNotice notice){

        //定义返回结果
        Response<Boolean> result = new Response<Boolean>();
        String inputCharset = "UTF-8";
        //组装map 并操作验证签名
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("is_success", notice.getIs_success());
//        sParaTemp.put("error_code", notice.getError_code());
//        sParaTemp.put("error_message", notice.getError_message());
//        sParaTemp.put("memo", notice.getMemo());
//        sParaTemp.put("partner_id", notice.getPartner_id());
        sParaTemp.put("_input_charset",inputCharset);
        sParaTemp.put("sign", notice.getSign());
        sParaTemp.put("sign_type", notice.getSign_type());
        sParaTemp.put("plat_user_id", notice.getPlat_user_id());
        sParaTemp.put("plat_user",notice.getPlat_user());
        sParaTemp.put("member_id", notice.getMember_id());
        sParaTemp.put("member_name", notice.getMember_name());

        String signType = notice.getSign_type();

        boolean isSuccess = false;
        try {
            //验签
            isSuccess = VerifyClient.verifyBasic(inputCharset, signType, sParaTemp).isSuccess();
        }catch (Exception e){
            log.error("failed to execute CreateAccreditSyncNotice by Kjt, cause:{}", e);
            result.setResult(false);
            result.setError("execute CreateAccreditSyncNotice fail");
            e.printStackTrace();
        }
     //   isSuccess = true;
        if(!isSuccess){
            //验签失败
            log.error("CreateAccreditSyncNotice verify false");
        }
        result = saveKjtpayAccount(sParaTemp);
        return result;
    }


    public Response<String> executeRebates(Map<String,String> reqMap){
        //定义返回结果
        Response<String> result = new Response<String>();
        result.setSuccess(true);
        //请求基本参数
        reqMap.put("service",kjtConfig.getCreateBatchTransferToAccountService());//接口名称
        reqMap.put("version",kjtConfig.getVersion());//版本号
        reqMap.put("partner_id",kjtConfig.getPartnerId());//商户号
        reqMap.put("_input_charset", kjtConfig.getInputCharset());//编码集
        reqMap.put("notify_url",kjtConfig.getCreateBatchTransferToAccountNotifyUrl()); //异步通知url
     //   reqMap.put("return_url",createBatchTransferToAccountUrl);//同步通知url

        //构建请求参数 包括 签名及签名类型
        try {
            reqMap = KjtCore.buildRequestPara(reqMap, kjtConfig.getSignType(), "", kjtConfig.getInputCharset());
            log.info("execute rebates url:"+kjtConfig.getGatewayUrl()+"?"+ KjtCore.createLinkString(reqMap,false));
            CloseableHttpResponse res = HttpClientUtilsExt.post(kjtConfig.getGatewayUrl(), reqMap, kjtConfig.getInputCharset());
            int statusCode = res.getStatusLine().getStatusCode();
            String resContent = EntityUtils.toString(res.getEntity());
            if(statusCode != 200 || resContent.indexOf("is_success=F") != -1 ){
                //未正常响应或异常
                log.error("failed to execute rebates by Kjt");
                result.setSuccess(false);
                result.setError("execute rebates fail");
            }
            result.setResult(resContent);
        } catch (Exception e) {
            log.error("failed to execute createAccredit by Kjt, cause:{}", e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 执行批量返款异步通知
     * @author jiangpeng
     * @createAt 2015/1/6 13:27
     * @param notice 通知
     */
    @Transactional
    public void executeCreateBatchTransferToAccountAsyncNotice(BatchTransferToAccountAsyncNotice notice){

        //组装map 并验签
        Map<String, String> sParaTemp = new HashMap<String, String>();

        sParaTemp.put("notify_id", notice.getNotify_id());
        sParaTemp.put("notify_type", notice.getNotify_type());
        sParaTemp.put("notify_time", notice.getNotify_time());
        sParaTemp.put("_input_charset", notice.get_input_charset());
        sParaTemp.put("sign", notice.getSign());
        sParaTemp.put("sign_type", notice.getSign_type());
        sParaTemp.put("version", notice.getVersion());
        sParaTemp.put("outer_trade_no", notice.getOuter_trade_no());
        sParaTemp.put("inner_trade_no", notice.getInner_trade_no());
        sParaTemp.put("transfer_amount", notice.getTransfer_amount());
        sParaTemp.put("transfer_status", notice.getTransfer_status());
        sParaTemp.put("fail_reason", notice.getFail_reason());
        sParaTemp.put("gmt_transfe", notice.getGmt_transfe());

        try {
          boolean  isSuccess = VerifyClient.verifyBasic(notice.get_input_charset(), notice.getSign_type(), sParaTemp).isSuccess();
            if(!isSuccess){
                //验签失败
                log.error("CreateAccreditSyncNotice verify false");
                return;
            }
       //     settlementService.updateSettleStatus(notice.getOuter_trade_no(),"5");
        }catch (Exception e) {
            log.error("failed to execute CreateBatchTransferToAccountAsyncNotice by Kjt, cause:{}", e);
            e.printStackTrace();
        }

    }

    /**
     * 根据用户ID查找快捷通账户信息
     * @author jiangpeng
     * @createAt 2015/1/8 14:14
     * @param userId
     * @return
     */
    public KjtpayAccount findAccountByUserId(String userId){
        return kjtpayDao.getByUserId(userId);
    }
}
