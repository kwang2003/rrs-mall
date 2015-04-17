package com.rrs.third.dao;

import com.rrs.third.model.ThirdUser;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by zhaop01 on 2014/9/2.
 */
@Repository
public class ThirdPartLoginDao extends SqlSessionDaoSupport {
    // 保存第三方平台用户信息
    public void saveThirdUser(ThirdUser thirdUser){
        getSqlSession().insert("NS_ThirdUser.saveThirdUser", thirdUser);
    }
    // 判断第三方平台用户信息是否存在
    public boolean isExists(ThirdUser thirdUser){
        ThirdUser temp = getSqlSession().selectOne("NS_ThirdUser.isExists", thirdUser);
        if(temp!=null){
            return true;
        }
        return false;
    }

    public ThirdUser findThirdUserBySourceName(ThirdUser thirdUser) {
        ThirdUser temp = getSqlSession().selectOne("NS_ThirdUser.findThirdUserBySourceName", thirdUser);
        return temp;
    }
    public void updateUserInfo(ThirdUser thirdUser) {
        getSqlSession().selectOne("NS_ThirdUser.updateUserInfo", thirdUser);
    }
}
