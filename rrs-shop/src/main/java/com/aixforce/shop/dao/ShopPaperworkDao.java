package com.aixforce.shop.dao;

import com.aixforce.shop.model.ShopPaperwork;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-05-12
 */
@Repository
public class ShopPaperworkDao extends SqlSessionDaoSupport {

    public Long create(ShopPaperwork shopPaperwork) {
        getSqlSession().insert("ShopPaperwork.create", shopPaperwork);
        return shopPaperwork.getId();
    }

    public ShopPaperwork findById(Long id){
        return getSqlSession().selectOne("ShopPaperwork.findById", id);
    }

    public ShopPaperwork findByShopId(Long shopId){
        return getSqlSession().selectOne("ShopPaperwork.findByShopId", shopId);
    }

    public Boolean updateByShopId(ShopPaperwork shopPaperwork){
        return getSqlSession().update("ShopPaperwork.updateByShopId" , shopPaperwork) > 0;
    }

    public boolean delete(Long id){
        return getSqlSession().delete("ShopPaperwork.delete", id) == 1;
    }

}
