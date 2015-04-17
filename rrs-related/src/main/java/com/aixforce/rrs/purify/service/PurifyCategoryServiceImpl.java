package com.aixforce.rrs.purify.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.dao.PurifyCategoryDao;
import com.aixforce.rrs.purify.manager.PurifyManager;
import com.aixforce.rrs.purify.model.PurifyCategory;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:净水组件类目处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
@Slf4j
@Service
public class PurifyCategoryServiceImpl implements PurifyCategoryService {
    @Autowired
    private PurifyCategoryDao purifyCategoryDao;

    @Autowired
    private PurifyManager purifyManager;

    /*
        创建净水类目
        // 字符串长度的验证后期一起处理
     */
    @Override
    public Response<Boolean> createCategory(PurifyCategory purifyCategory) {
        Response<Boolean> result = new Response<Boolean>();

        //类目名称
        Response<Boolean> response = existCategory(purifyCategory.getSeriesId() , purifyCategory.getCategoryName(), null);
        if(response.isSuccess()){
            //该系列下是否已存在该类目名称
            if(response.getResult()){
                log.error("category have existed, seriesId={} categoryName={}", purifyCategory.getSeriesId(), purifyCategory.getCategoryName());
                result.setError("purify.category.name.existed");
            }
        }else{
            result.setError(response.getError());
            return result;
        }

        //系列编号
        if(purifyCategory.getSeriesId() == null){
            log.error("create category needs seriesId");
            result.setError("purify.category.seriesId.null");
            return result;
        }

        //类目层次
        Integer stageId = findMaxStage(purifyCategory.getSeriesId());
        if(stageId == -1){
            log.error("find max stageId failed, seriesId", purifyCategory.getSeriesId());
            result.setError("purify.category.findMaxStage.failed");
            return result;
        }else{
            purifyCategory.setStage(stageId);
        }

        try{
            Long categoryId = purifyCategoryDao.create(purifyCategory);
            if(categoryId == null){
                result.setError("purify.category.create.failed");
            }else{
                result.setResult(true);
            }
        }catch(Exception e){
            log.error("create category failed , error code={}", e);
            result.setError("purify.category.create.failed");
        }

        return result;
    }

    /*
        更新类目信息
     */
    @Override
    public Response<Boolean> updateCategory(PurifyCategory purifyCategory) {
        Response<Boolean> result = new Response<Boolean>();

        if(purifyCategory.getId() == null){
            log.error("update purify category needs categoryId");
            result.setError("purify.category.categoryId.null");
            return result;
        }

        //判断需要更新的名称是否已存在
        Response<Boolean> response = existCategory(purifyCategory.getSeriesId() , purifyCategory.getCategoryName(), purifyCategory.getId());
        if(response.isSuccess()){
            if(response.getResult()){
                log.error("category name have existed, siteId={} seriesName={}", purifyCategory.getSeriesId() , purifyCategory.getCategoryName());
                result.setError("purify.category.name.existed");
            }
        }else{
            result.setError(response.getError());
            return result;
        }

        try{
            result.setResult(purifyCategoryDao.update(purifyCategory));
        }catch(Exception e){
            log.error("update category filed, seriesId={} error code={}", purifyCategory.getSeriesId(), e);
            result.setError("purify.category.update.failed");
        }

        return result;
    }

    /*
        删除类目
     */
    @Override
    public Response<Boolean> deleteCategory(Long categoryId){
        Response<Boolean> result = new Response<Boolean>();

        if(categoryId == null){
            log.error("delete purify category need categoryId");
            result.setError("purify.category.categoryId.null");
            return result;
        }

        try{
            purifyManager.deleteCategory(categoryId);
            result.setResult(true);
        }catch(Exception e){
            log.error("delete purify category filed, categoryId={} error code={}", categoryId, e);
            result.setError("purify.category.delete.failed");
        }

        return result;
    }

    /*
        通过编号查询类目信息
     */
    @Override
    public Response<PurifyCategory> findById(Long categoryId) {
        Response<PurifyCategory> result = new Response<PurifyCategory>();

        if(categoryId == null){
            log.error("find purify category needs categoryId");
            result.setError("purify.category.categoryId.null");
            return result;
        }

        try{
            result.setResult(purifyCategoryDao.findById(categoryId));
        }catch(Exception e){
            log.error("find purify category filed, categoryId={} error code={}", categoryId, e);
            result.setError("purify.category.find.failed");
        }

        return result;
    }

    /*
        通过系列编号&类目名称查询是否存在该类目
     */
    @Override
    public Response<Boolean> existCategory(Long seriesId , String categoryName, Long categoryId) {
        Response<Boolean> result = new Response<Boolean>();

        //系列编号
        if(seriesId == null){
            log.error("find category needs seriesId");
            result.setError("purify.category.seriesId.null");
            return result;
        }

        //类目名称
        if(Strings.isNullOrEmpty(categoryName)){
            log.error("find category needs category name");
            result.setError("purify.category.name.null");
            return result;
        }

        try{
            if(categoryId == null){
                result.setResult(!purifyCategoryDao.findByName(seriesId, categoryName).isEmpty());
            }else{
                //判断新名称与旧名称是否相同
                PurifyCategory purifyCategory = purifyCategoryDao.findById(categoryId);
                if(purifyCategory != null && !Objects.equal(purifyCategory.getCategoryName(), categoryName)){
                    result.setResult(!purifyCategoryDao.findByName(seriesId, categoryName).isEmpty());
                }else{
                    result.setResult(false);
                }
            }
        }catch(Exception e){
            log.error("find category failed ,seriesId={} categoryName={} error code={}", seriesId, categoryName, e);
            result.setError("purify.category.find.failed");
        }

        return result;
    }

    /*
        通过系列编号查询该序列下的全部组件类目
     */
    @Override
    public Response<List<PurifyCategory>> findBySeriesId(Long seriesId) {
        Response<List<PurifyCategory>> result = new Response<List<PurifyCategory>>();

        //系列编号
        if(seriesId == null){
            log.error("find categories needs seriesId");
            result.setError("purify.category.seriesId.null");
            return result;
        }

        try{
            List<PurifyCategory> purifyCategoryList = purifyCategoryDao.findBySeriesId(seriesId);
            if(purifyCategoryList == null){
                result.setError("purify.category.find.failed");
            }else{
                result.setResult(purifyCategoryList);
            }
        }catch(Exception e){
            log.error("find categories failed , seriesId={} error code={}", seriesId, e);
            result.setError("purify.category.find.failed");
        }

        return result;
    }

    /**
     * 根据系列编号查询该系列的最深的组件层次
     * @param seriesId  系列编号
     * @return  Integer
     * 返回最深的组件层次编号+1(为找到返回-1)
     */
    private Integer findMaxStage(Long seriesId){
        Integer maxStage;

        try{
            PurifyCategory purifyCategory = purifyCategoryDao.findMaxStage(seriesId);
            maxStage = purifyCategory == null ? 1 : purifyCategory.getStage()+1;
        }catch(Exception e){
            log.error("find max stage failed, seriesId={} error code={}", seriesId, e);
            maxStage = -1;
        }

        return maxStage;
    }
}
