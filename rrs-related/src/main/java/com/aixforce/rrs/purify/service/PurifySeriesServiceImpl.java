package com.aixforce.rrs.purify.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.dao.PurifySeriesDao;
import com.aixforce.rrs.purify.manager.PurifyManager;
import com.aixforce.rrs.purify.model.PurifySeries;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
@Slf4j
@Service
public class PurifySeriesServiceImpl implements PurifySeriesService {
    @Autowired
    private PurifySeriesDao purifySeriesDao;

    @Autowired
    private PurifyManager purifyManager;

    /*
        创建系列对象
        // 字符串长度的验证后期一起处理
     */
    @Override
    public Response<Boolean> createPurifySeries(PurifySeries purifySeries) {
        Response<Boolean> result = new Response<Boolean>();

        //系列名称
        Response<Boolean> response = existSeries(purifySeries.getSiteId() , purifySeries.getSeriesName(), null);
        if(response.isSuccess()){
            if(response.getResult()){
                log.error("series have existed, siteId={} seriesName={}", purifySeries.getSiteId() , purifySeries.getSeriesName());
                result.setError("purify.series.name.existed");
            }
        }else{
            result.setError(response.getError());
            return result;
        }

        //系列介绍信息
        if(Strings.isNullOrEmpty(purifySeries.getSeriesIntroduce())){
            log.error("create purify series needs introduce");
            result.setError("purify.series.introduce.null");
            return result;
        }

        //系列图片信息
        if(Strings.isNullOrEmpty(purifySeries.getSeriesImage())){
            log.error("create purify series needs image");
            result.setError("purify.series.image.null");
            return result;
        }

        //站点编号
        if(purifySeries.getSiteId() == null){
            log.error("create purify series needs siteId");
            result.setError("purify.series.siteId.null");
            return result;
        }

        try{
            Long seriesId = purifySeriesDao.create(purifySeries);
            if(seriesId == null){
                result.setError("purify.series.create.failed");
            }else{
                result.setResult(true);
            }
        }catch(Exception e){
            log.error("create purify filed, error code={}", e);
            result.setError("purify.series.create.failed");
        }

        return result;
    }

    /*
        更新系列对象
     */
    @Override
    public Response<Boolean> updatePurifySeries(PurifySeries purifySeries) {
        Response<Boolean> result = new Response<Boolean>();

        if(purifySeries.getId() == null){
            log.error("update purify series needs seriesId");
            result.setError("purify.series.seriesId.null");
            return result;
        }

        //判断需要更新的名称是否已存在
        Response<Boolean> response = existSeries(purifySeries.getSiteId() , purifySeries.getSeriesName(), purifySeries.getId());
        if(response.isSuccess()){
            if(response.getResult()){
                log.error("series name have existed, siteId={} seriesName={}", purifySeries.getSiteId() , purifySeries.getSeriesName());
                result.setError("purify.series.name.existed");
            }
        }else{
            result.setError(response.getError());
            return result;
        }

        try{
            result.setResult(purifySeriesDao.update(purifySeries));
        }catch(Exception e){
            log.error("update purify filed, siteId={} error code={}", purifySeries.getSiteId(), e);
            result.setError("purify.series.update.failed");
        }

        return result;
    }

    /*
        通过编号查询系列信息
     */
    @Override
    public Response<PurifySeries> findById(Long seriesId) {
        Response<PurifySeries> result = new Response<PurifySeries>();

        if(seriesId == null){
            log.error("find purify series needs seriesId");
            result.setError("purify.series.seriesId.null");
            return result;
        }

        try{
            result.setResult(purifySeriesDao.findById(seriesId));
        }catch(Exception e){
            log.error("find purify series filed, seriesId={} error code={}", seriesId, e);
            result.setError("purify.series.find.failed");
        }

        return result;
    }

    @Override
    public Response<Boolean> existSeries(Long siteId, String seriesName, Long seriesId) {
        Response<Boolean> result = new Response<Boolean>();

        //站点编号
        if(siteId == null){
            log.error("find series needs siteId");
            result.setError("purify.series.siteId.null");
            return result;
        }

        //系列名称
        if(Strings.isNullOrEmpty(seriesName)){
            log.error("find series needs series name");
            result.setError("purify.series.name.null");
            return result;
        }

        try{
            if(seriesId == null){
                result.setResult(!purifySeriesDao.findByName(siteId, seriesName).isEmpty());
            }else{
                //判断新名称与旧名称是否相同
                PurifySeries purifySeries = purifySeriesDao.findById(seriesId);
                if(purifySeries != null && !Objects.equal(purifySeries.getSeriesName() , seriesName)){
                    result.setResult(!purifySeriesDao.findByName(siteId, seriesName).isEmpty());
                }else{
                    result.setResult(false);
                }
            }
        }catch(Exception e){
            log.error("find series failed , error code={}", e);
            result.setError("purify.series.find.failed");
        }

        return result;
    }

    @Override
    public Response<Boolean> deletePurifySeries(Long seriesId) {
        Response<Boolean> result = new Response<Boolean>();

        if(seriesId == null){
            log.error("delete purify series need seriesId");
            result.setError("purify.series.seriesId.null");
            return result;
        }

        try{
            purifyManager.deleteSeries(seriesId);
            result.setResult(true);
        }catch(Exception e){
            log.error("delete purify series filed, seriesId={} error code={}", seriesId, e);
            result.setError("purify.series.delete.failed");
        }

        return result;
    }

    /*
        通过站点编号查询系列信息
     */
    @Override
    public Response<List<PurifySeries>> findSiteSeries(Long siteId) {
        Response<List<PurifySeries>> result = new Response<List<PurifySeries>>();

        if(siteId == null){
            log.error("find purify series needs siteId");
            result.setError("purify.series.siteId.null");
            return result;
        }

        try{
            result.setResult(purifySeriesDao.findSiteSeries(siteId));
        }catch(Exception e){
            log.error("find purify series filed, siteId={} error code={}", siteId, e);
            result.setError("purify.series.find.failed");
        }

        return result;
    }
}
