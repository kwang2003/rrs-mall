package com.rrs.kjtpay.dao;

import com.rrs.kjtpay.model.KjtpayAccount;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * 快捷通支付Dao
 * @author jiangpeng
 * @createAt 2015/1/5 12:59
 */
@Repository
public class KjtpayDao extends SqlSessionDaoSupport {

    /**
     * 新增快捷通支付帐号
     * @author jiangpeng
     * @createAt 2015/15 12:59
     * @param account 快捷通支付帐号
     * @return 操作结果，成功为true失败为false
     */
    public Boolean createKjtpayAccount(KjtpayAccount account) {

        //执行插入操作
        getSqlSession().insert("Kjtpay.create", account);
        //返回操作结果
        return account.getId()!=null && account.getId()>0;

    }

    public KjtpayAccount getByUserId(String userId){
        return getSqlSession().selectOne("Kjtpay.findByUserId",userId);
    }
}
