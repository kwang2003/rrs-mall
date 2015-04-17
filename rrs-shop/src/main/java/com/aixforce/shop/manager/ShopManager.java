package com.aixforce.shop.manager;

import com.aixforce.shop.dao.ShopDao;
import com.aixforce.shop.dao.ShopExtraDao;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.aixforce.common.utils.Arguments.notNull;

/**
 * Date: 14-2-26
 * Time: PM2:12
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
@Component
@Slf4j
public class ShopManager {

    @Autowired
    ShopExtraDao shopExtraDao;

    @Autowired
    ShopDao shopDao;

    @Transactional
    public List<Long> bulkUpdateShopExtraScore(List<ShopExtra> extras) {
        List<Long> ids = Lists.newArrayList();

        for (ShopExtra extra : extras) {
            ShopExtra exist = shopExtraDao.findByShopId(extra.getShopId());

            //isCorrect(exist);//这里只做了判断，打印log 并没有对其进行任何操作

            if (null == exist) {
                exist = new ShopExtra();
                exist.setShopId(extra.getShopId());
                exist.setRQuality(extra.getRQuality());
                exist.setRService(extra.getRService());
                exist.setRExpress(extra.getRExpress());
                exist.setRDescribe(extra.getRDescribe());
                exist.setTradeQuantity(
                extra.getTradeQuantity()>0 ? extra.getTradeQuantity() : 0l);
                //暂时不持久化
                //shopExtraDao.create(exist);
                ids.add(exist.getId());
                continue;
            }
            exist.addRQuality(extra.getRQuality());
            exist.addRService(extra.getRService());
            exist.addRExpress(extra.getRExpress());
            exist.addRDescribe(extra.getRDescribe());
            exist.setTradeQuantity(
           // extra.getTradeQuantity()>0 ? extra.getTradeQuantity() : 0l);
            exist.getTradeQuantity()+ extra.getTradeQuantity());
            //暂时不持久化
            //shopExtraDao.updateByShopId(exist);
            ids.add(exist.getId());
        }
        return ids;
    }



    @Transactional
    public void updateShopExtraScore(ShopExtra extra) {

        ShopExtra exist = shopExtraDao.findByShopId(extra.getShopId());

        if(exist == null) {
            ShopExtra created = new ShopExtra();
            created.setShopId(extra.getShopId());
            created.setRQuality(extra.getRQuality());
            created.setRService(extra.getRService());
            created.setRExpress(extra.getRExpress());
            created.setRDescribe(extra.getRDescribe());
            created.setTradeQuantity(
                    extra.getTradeQuantity()>0 ? extra.getTradeQuantity() : 0l);
            shopExtraDao.create(created);
            return;
        }
        //ShopExtra updated = new ShopExtra();
        //updated.setShopId(extra.getShopId());
        exist.addRQuality(extra.getRQuality());
        exist.addRService(extra.getRService());
        exist.addRExpress(extra.getRExpress());
        exist.addRDescribe(extra.getRDescribe());
        exist.addTradeQuantity(
                extra.getTradeQuantity()>0 ? extra.getTradeQuantity() : 0l);
        shopExtraDao.updateByShopId(exist);
    }


    @Transactional
    public void bulkCreateExtraOfShops(List<Long> ids) {
        for (Long shopId : ids) {
            ShopExtra extra = shopExtraDao.findByShopId(shopId);
            if (notNull(extra)) continue;
            ShopExtra creating = new ShopExtra();
            creating.setShopId(shopId);
            creating.setRate(0.0000);
            creating.setDepositNeed(0L);
            creating.setTechFeeNeed(0L);
            shopExtraDao.create(creating);
        }
    }

    /**
     * 全量更新shop_extras 表中店铺评分的信息。分值不进行累加
     * @param extra shop_extra列表
     */

    @Transactional
    public void fullUpdateShopExtraScore(ShopExtra extra) {

        ShopExtra exist = shopExtraDao.findByShopId(extra.getShopId());
        if(exist == null) {
            ShopExtra created = new ShopExtra();
            created.setShopId(extra.getShopId());
            created.setRQuality(extra.getRQuality());
            created.setRService(extra.getRService());
            created.setRExpress(extra.getRExpress());
            created.setRDescribe(extra.getRDescribe());
            created.setTradeQuantity(
            extra.getTradeQuantity()>0 ? extra.getTradeQuantity() : 0l);
            shopExtraDao.create(created);
            return;
        }
        ShopExtra updated = new ShopExtra();
        updated.setShopId(extra.getShopId());
        updated.setRQuality(extra.getRQuality());
        updated.setRService(extra.getRService());
        updated.setRExpress(extra.getRExpress());
        updated.setRDescribe(extra.getRDescribe());
        updated.setTradeQuantity(
        extra.getTradeQuantity()>0 ? extra.getTradeQuantity() : 0l);
        shopExtraDao.updateByShopId(updated);
    }

    @Transactional
    public void createShopAndExtra(Shop shop) {
        shopDao.create(shop);

        ShopExtra extra = new ShopExtra();
        extra.setShopId(shop.getId());
        extra.setRate(0.0000);
        extra.setDepositNeed(0L);
        extra.setTechFeeNeed(0L);
        shopExtraDao.create(extra);
    }
}
