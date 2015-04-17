package com.aixforce.rrs.purify.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.dao.PurifyRelationDao;
import com.aixforce.rrs.purify.model.PurifyRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:净水组件上下级关系处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
@Slf4j
@Service
public class PurifyRelationServiceImpl implements PurifyRelationService {
    @Autowired
    private PurifyRelationDao purifyRelationDao;

    /*
        创建组件上下级关系信息
     */
    @Override
    public Response<Boolean> createRelation(PurifyRelation purifyRelation) {
        Response<Boolean> result = new Response<Boolean>();

        //关联关系是否已存在
        Response<PurifyRelation> existRelation = findRelation(purifyRelation.getAssemblyParent() , purifyRelation.getAssemblyChild());
        if(existRelation.isSuccess()){
            //该关联关系已存在
            if(existRelation.getResult() != null){
                result.setError("purify.relation.existed");
            }
        }else{
            result.setError(existRelation.getError());
            return result;
        }

        //判断商品是否已存在
        if(purifyRelation.getProductId() == null){
            //未关联商品
            purifyRelation.setProductId(0l);
        }else if(!existProduct(purifyRelation.getProductId())){
            log.error("can't find product by productId={}", purifyRelation.getProductId());
            result.setError("purify.relation.product.null");
            return result;
        }

        try{
            Long relationId = purifyRelationDao.create(purifyRelation);
            if(relationId == null){
                result.setError("purify.relation.create.failed");
            }else{
                result.setResult(true);
            }
        }catch(Exception e){
            log.error("create purify relation failed , error code={}" , e);
            result.setError("purify.relation.create.failed");
        }

        return result;
    }

    /*
        更新组件关系
     */
    @Override
    public Response<Boolean> updateRelation(PurifyRelation purifyRelation) {
        Response<Boolean> result = new Response<Boolean>();

        if(purifyRelation.getId() == null){
            log.error("update purify relation need relationId");
            result.setError("purify.relation.update.failed");
            return result;
        }

        try{
            result.setResult(purifyRelationDao.update(purifyRelation));
        }catch(Exception e){
            log.error("update purify relation failed , error code={}" , e);
            result.setError("purify.relation.create.failed");
        }

        return result;
    }

    /*
        通过关系编号查询关系
     */
    @Override
    public Response<PurifyRelation> findById(Long relationId) {
        Response<PurifyRelation> result = new Response<PurifyRelation>();

        if(relationId == null){
            log.error("find purify relation needs relationId");
            result.setError("purify.relation.relationId.null");
            return result;
        }

        try{
            result.setResult(purifyRelationDao.findById(relationId));
        }catch(Exception e){
            log.error("find purify relation failed , relationId={} error code={}", relationId, e);
            result.setError("purify.relation.find.failed");
        }

        return result;
    }

    /*
        通过上级组件&下级组件查询关系对象
     */
    @Override
    public Response<PurifyRelation> findRelation(Long assemblyParent, Long assemblyChild) {
        Response<PurifyRelation> result = new Response<PurifyRelation>();

        if(assemblyParent == null){
            log.error("find assembly relation needs parentId");
            result.setError("purify.relation.parentId.null");
            return result;
        }

        if(assemblyChild == null){
            log.error("find assembly relation needs childId");
            result.setError("purify.relation.childId.null");
            return result;
        }

        try{
            result.setResult(purifyRelationDao.findRelation(assemblyParent , assemblyChild));
        }catch(Exception e){
            log.error("find purify relation failed, assembly parentId={} childId={} error code={}" , assemblyParent, assemblyChild, e);
            result.setError("purify.relation.find.failed");
        }
        return result;
    }

    /**
     * 通过产品编号查询该产品是否存在
     * @param productId 产品编号
     * @return  Boolean
     * 返回判断结果
     * //todo 这个后期处理现在还不明确产品是调用现有的items还是另外创建一张新表
     */
    private Boolean existProduct(Long productId){
        return true;
    }
}
