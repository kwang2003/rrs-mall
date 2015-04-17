package com.aixforce.user.service;

import com.aixforce.common.model.Response;
import com.aixforce.user.model.UserAccountSummary;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-09 5:43 PM  <br>
 * Author: xiao
 */
public interface UserAccountSummaryService {



    /**
     * 创建用户引流统计记录
     *
     * @param summary    用户统计信息
     * @return  如果创建成功则返回id
     */
    Response<Long> create(UserAccountSummary summary);










}
