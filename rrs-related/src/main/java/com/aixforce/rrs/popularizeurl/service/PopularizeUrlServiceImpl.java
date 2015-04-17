package com.aixforce.rrs.popularizeurl.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.popularizeurl.dao.PopularizeUrlRedisDao;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 王猛 on 14-9-23
 */
@Service
@Slf4j
public class PopularizeUrlServiceImpl implements PopularizeUrlService {

    @Autowired
    PopularizeUrlRedisDao popularizeUrlRedisDao;

    @Override
    public Response<String> getUrl(String popUrl) {
        log.info("Get popularizeUrl begin");
        Response<String> result = new Response<String>();

        try {
            if (popUrl == null) {
                log.error("popUrl(code={}) not found", popUrl);
                result.setError("popUrl.not.found");
                return result;
            }
            String url = popularizeUrlRedisDao.findUrlByPopUrlContext(popUrl);
            result.setResult(url);
            return result;
        } catch (Exception e) {
            log.error("failed to find popUrl(popUrl={}), cause:{}", popUrl, Throwables.getStackTraceAsString(e));
            result.setError("presale.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> createPopUrl(String popUrl, String url) {
        log.info("Get popularizeUrl begin");
        Response<Boolean> result = new Response<Boolean>();

        try {
            if (popUrl == null) {
                log.error("popUrl(code={}) not found", popUrl);
                result.setError("popUrl.not.found");
                return result;
            }
            popularizeUrlRedisDao.createByPopUrlContext(popUrl, url);
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("failed to create popUrl(popUrl={}), cause:{}", popUrl, Throwables.getStackTraceAsString(e));
            result.setError("presale.query.fail");
            return result;
        }
    }
}
