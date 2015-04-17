package com.aixforce.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.rrs.kjtpay.dto.BatchTransferToAccountAsyncNotice;
import com.rrs.kjtpay.dto.CreateAccredit;
import com.rrs.kjtpay.dto.CreateAccreditSyncNotice;
import com.rrs.kjtpay.model.KjtpayAccount;
import com.rrs.kjtpay.service.KjtpayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 快捷通Controller
 * @author jiangpeng
 * @createAt 2015/1/5 13:15
 */
@Slf4j
@Controller
@RequestMapping("/api/kjtpay")
public class KjtpayController {


    @Autowired
    private KjtpayService kjtpayService;

    /**
     * 绑定快捷通
     * @author jiangpeng
     * @createAt 2015/1/5 13:26
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/createAccredit", method = RequestMethod.POST)
    public String createAccredit(CreateAccredit createAccredit) throws Exception {
            //执行请求 并获取结果
           return  kjtpayService.createAccredit(createAccredit);
    }

    /**
     * 账户绑定之同步通知
     * @author jiangpeng
     * @createAt 2015/1/6 9:42
     * @param response
     * @param notice 通知消息
     * @throws Exception
     */
    @RequestMapping(value = "/recieveSyncNotice",method = RequestMethod.GET)
    public String createAccreditSyncNotice(HttpServletResponse response, CreateAccreditSyncNotice notice) throws Exception{

        try {
            KjtpayAccount account = kjtpayService.findAccountByUserId(notice.getPlat_user_id());
            if(account==null) {
                //执行请求 并获取结果
                Response<Boolean> result = kjtpayService.executeCreateAccreditSyncNotice(notice);
                if (!result.getResult()) {
                    //发生异常
                    log.error("kjtpay recieveSyncNotice error:{}",result.getError());
                    throw new JsonResponseException(500, result.getError());
                } else {
                    return "views/seller/account_bound_success";
                }
            }else{
                //重复提交
                return "views/seller/account_bound_info";
            }
        }catch (Exception e) {
            log.error("execute kjtpay recieveSyncNotice error");
            //TODO 异常信息在配置文件处理 代码除注释外不得出现汉字
            throw new JsonResponseException(500, "未知异常");
        }
    }

    /**
     * 返款接口
     * @author jiangpeng
     * @createAt 2015/1/6
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/rebates", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<String> rebates(@RequestParam("batch_no")  String batch_no,@RequestParam("transfer_num")String transfer_num,@RequestParam("transfer_amount") String transfer_amount,@RequestParam("transfer_list") String transfer_list,@RequestParam("operator_id") String operator_id,@RequestParam("identity_no") String identity_no,@RequestParam("identity_type") String identity_type,@RequestParam("memo")String memo) throws Exception{

        Map<String,String> reqMap = new HashMap<String,String>();

        //请求业务参数
        reqMap.put("batch_no",batch_no);//批号
        reqMap.put("transfer_num",transfer_num);//转账笔数
        reqMap.put("transfer_amount",transfer_amount);//转账金额
        reqMap.put("transfer_list",transfer_list);//转账列表
        reqMap.put("operator_id",operator_id);//操作员id
        reqMap.put("identity_no",identity_no);//会员账号
        reqMap.put("identity_type",identity_type);//标识类型
        reqMap.put("memo",memo);

        try {
            //执行返款 并获取响应结果
            return kjtpayService.executeRebates(reqMap);
        }catch (Exception e){
            //TODO 异常信息在配置文件处理 代码除注释外不得出现汉字
            throw new JsonResponseException(500, "未知异常");
        }

    }

    /**
     * 返款之异步通知
     * @author jiangpeng
     * @createAt 2015/1/6 13:19
     * @param notice
     * @throws Exception
     */
    @RequestMapping(value = "/recieveBatchTransferToAccountAsyncNotice", method = RequestMethod.POST)
    public void createBatchTransferToAccountAsyncNotice(BatchTransferToAccountAsyncNotice notice) throws Exception {
        kjtpayService.executeCreateBatchTransferToAccountAsyncNotice(notice);
    }

    /**
     * 根据userID查找快捷通信息
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/findKjtpayAccountInfo/{id}",method = RequestMethod.GET)
    public KjtpayAccount findKjtpayAccountInfo(@PathVariable String id){
        try {
            //执行 并获取响应结果
            return kjtpayService.findAccountByUserId(id);
        }catch (Exception e){
            //TODO 异常信息在配置文件处理 代码除注释外不得出现汉字
            throw new JsonResponseException(500, "未知异常");
        }
    }

    /**
     * 验证账户快捷通是否绑定
     * @author jiangpeng
     * @createAt 2015/1/8 14:43
     * @param id 商户ID
     * @return 跳转页面
     */
    @RequestMapping(value="/valid/info/{id}",method = RequestMethod.GET)
    public String validBoundAccountInfo(@PathVariable String id){
        try {
            //执行
            //获取快捷通账户
            //TODO 定时同步或者去快捷通查询
            KjtpayAccount account = kjtpayService.findAccountByUserId(id);
            //TODO 获取银行卡账户
            if(account==null){
                //不存在
                //TODO 需要同时判断银行卡和 快捷通账户是否存在
                return "views/seller/account_bound_notes";
        }
            return "views/seller/account_bound_info";
        }catch (Exception e){
            //TODO 异常信息在配置文件处理 代码除注释外不得出现汉字
            throw new JsonResponseException(500, "未知异常");
        }
    }
}
