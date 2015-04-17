package com.rrs.kjtpay.service;

import com.aixforce.common.model.Response;
import com.rrs.kjtpay.dto.BatchTransferToAccountAsyncNotice;
import com.rrs.kjtpay.dto.CreateAccredit;
import com.rrs.kjtpay.dto.CreateAccreditSyncNotice;
import com.rrs.kjtpay.model.KjtpayAccount;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.util.Map;

/**
 * 快捷通支付Servcice接口
 * @author jiangpeng
 * @createAt 2015/1/5 11:30
 */

public interface KjtpayService {

    /**
     * 新增快捷通账户信息
     * @param sParaTemp 账户信息
     * @return 操作结果
     */
    Response<Boolean> saveKjtpayAccount(Map<String, String> sParaTemp);

    /**
     * 绑定接口调用
     * @author jiangpeng
     * @createAt 2015/1/5 13:31
     * @param createAccredit 绑定相关信息
     */
   String createAccredit(CreateAccredit createAccredit);

    /**
     * 处理账户绑定同步通知
     * @author jiangpeng
     * @createAt 2015/1/6 9:46
     * @param notice
     * @return 响应结果
     */
   Response<Boolean> executeCreateAccreditSyncNotice(CreateAccreditSyncNotice notice);


    /**
     * 执行快捷通批量返款操作
     * @author jiangpeng
     * @createAt 2015/1/6 11:24
     * @param reqMap 请求参数
     * @return 响应结果
     */
   Response<String> executeRebates(Map<String, String> reqMap);

    /**
     * 执行批量返款异步通知
     * @author jiangpeng
     * @createAt 2015/1/6 13:27
     * @param notice 通知
     */
    void executeCreateBatchTransferToAccountAsyncNotice(BatchTransferToAccountAsyncNotice notice);

    /**
     * 根据用户ID查找快捷通账户信息
     * @author jiangpeng
     * @createAt 2015/1/8 14:14
     * @param userId
     * @return
     */
    KjtpayAccount findAccountByUserId(String userId);

}
