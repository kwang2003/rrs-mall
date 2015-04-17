package com.rrs.brand.dao;

import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandUser;
import com.rrs.brand.model.RrsBrand;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by temp on 2014/7/10.
 */

@Repository
public class BrandUserDao extends SqlSessionDaoSupport {
    /**\
     * 查询 品牌商登陆
     * @param brandUser
     * @return
     */
    public  BrandUser searchBrandUser(BrandUser brandUser){
        BrandUser brand  = getSqlSession().selectOne("BrandUser.findByPwd",brandUser);
        return brand;
    }
    /**
     * 校验品牌商
     */
    public int insertBrandVerity(BrandClub brandClub){
        return  (Integer)getSqlSession().selectOne("BrandClub.vertify",brandClub);
    }

    public boolean insertBrandUserProfiles(BrandClub brandClub){
        int result = getSqlSession().insert("BrandClub.create",brandClub);
        if(result==1){
            return true;
        }else {
            return false;
        }
    }
    /**
     * 插入品牌商
     * @param
     * @returnt
     */
    public boolean  insertBrandUser(BrandUser brandUser) {
        int result = getSqlSession().insert("BrandUser.create", brandUser);
        if(result ==1){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 展示需要批准的品牌商
     */
    public List<BrandClub> showBrandUser(String sellerName,String brandSearchName, int pinpai, int status){
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("sellerName",sellerName);
        map.put("brandSearchName",brandSearchName);
        map.put("pinpai",pinpai);
        map.put("status",status);
        return  getSqlSession().selectList("BrandClub.showAll",map);
    }
    /**
     * 校验品牌信息是否被入驻
     * @return
     */
    public int brandIsExist(RrsBrand brand){
        return (Integer)getSqlSession().selectOne("RrsBrand.isExistsBrand",brand);
    }
    /**
     * 审核成功改变状态
     */
    public boolean updateSucc(BrandClub brandClub,RrsBrand brand){
        int count = (Integer)getSqlSession().selectOne("RrsBrand.countBrand",brand);
        if(count==0){
            return false;
        }else {
            RrsBrand brand2 = getSqlSession().selectOne("RrsBrand.findBrandId", brand);
            brandClub.setBrandOutId(brand2.getId());
            getSqlSession().update("BrandClub.update", brandClub);
            getSqlSession().update("BrandClub.updateUser", brandClub);
            return true;
        }
    }
    /**
     * 审核失败改变状态
     */
    public void updateFail(BrandClub brandClub){
        getSqlSession().update("BrandClub.updateFail",brandClub);

    }
    /**
     * 审核冻结改变状态
     */
    public void updateFrozen(BrandClub brandClub){
        getSqlSession().update("BrandClub.updateFrozen",brandClub);

    }

    /**
     * 审核解冻改变状态
     */
    public void updateUnFrozen(BrandClub brandClub){
        getSqlSession().update("BrandClub.updateUnFrozen",brandClub);

    }
    /**
     * 插入保证金
     */
    public void insertFee(BrandClub brandClub){
        getSqlSession().insert("BrandClub.createFee",brandClub);

    }

    /**
     * 校验是否在保质金
     */
    public int feeVerity(BrandClub brandClub){
        return  (Integer)getSqlSession().selectOne("BrandClub.verityFee",brandClub);
    }

    /**
     * 更新保证金
     */
    public void updateFee(BrandClub brandClub){
        getSqlSession().insert("BrandClub.updateFee",brandClub);

    }
    public BrandClub verifyBrand(BrandClub brandClub){
        return getSqlSession().selectOne("BrandClub.searchBrandUserByUserId",brandClub);
    }
    public BrandClub updateBrandInfo(long userId){
        return getSqlSession().selectOne("BrandClub.brandUserUpdateInfo",userId);
    }
    public void updateBrandInfos(BrandClub brandClub){getSqlSession().update("BrandClub.updateInfos",brandClub);}

}
