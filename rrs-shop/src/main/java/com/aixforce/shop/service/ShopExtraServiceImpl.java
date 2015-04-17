package com.aixforce.shop.service;

import com.aixforce.common.model.Response;
import com.aixforce.shop.dao.ShopExtraDao;
import com.aixforce.shop.model.ShopExtra;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Author:  songrenfei
 * Date: 2014-08-01
 */
@Service
@Slf4j
public class ShopExtraServiceImpl implements ShopExtraService {

    @Autowired
    private ShopExtraDao shopExtraDao;

    @Override
    public Response<ShopExtra> findByShopId(Long shopId) {

        Response<ShopExtra> result = new Response<ShopExtra>();
        try {
            ShopExtra shopExtra = shopExtraDao.findByShopId(shopId);
            if (shopExtra == null) {
                log.error("no shopExtra(id={}) found", shopId);
                result.setError("shopExtra.not.found");
                return result;
            }
            result.setResult(shopExtra);
            return result;
        } catch (Exception e) {
            log.error("failed to find shopExtra where id = {},cause:{}", shopId, Throwables.getStackTraceAsString(e));
            result.setError("shopExtra.query.fail");
            return result;
        }

    }

    @Override
    public  Response<Boolean> updateIsStorePayByShopId(ShopExtra extra){

        Response<Boolean> result = new Response<Boolean>();

        try{
            shopExtraDao.updateIsStorePayByShopId(extra);
            result.setSuccess(true);
            return result;
        } catch (Exception e){
            log.error("failed to update shopExtra at isStorePay where shopid = {},cause:{}", extra.getShopId(), Throwables.getStackTraceAsString(e));
            result.setError("shopExtra.update.isStorePay.fail");
            result.setSuccess(false);

            return result;
        }

    }

    @Override
    public Response<Long> create(ShopExtra extra){
        Response<Long> result = new Response<Long>();

        try {
            Long id = shopExtraDao.create(extra);
            result.setResult(id);
            result.setSuccess(Boolean.TRUE);
            return result;
        }catch (Exception e){
            log.error("failed to create shopExtra  where shopid = {},cause:{}", extra.getShopId(), Throwables.getStackTraceAsString(e));
            result.setError("shopExtra.create.fail");
            result.setSuccess(false);
            return result;
        }
    }
}
