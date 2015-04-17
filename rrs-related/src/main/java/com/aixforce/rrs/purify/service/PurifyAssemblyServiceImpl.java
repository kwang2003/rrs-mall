package com.aixforce.rrs.purify.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.dao.PurifyAssemblyDao;
import com.aixforce.rrs.purify.manager.PurifyManager;
import com.aixforce.rrs.purify.model.PurifyAssembly;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:组件实体对象信息处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
@Slf4j
@Service
public class PurifyAssemblyServiceImpl implements PurifyAssemblyService {
    @Autowired
    private PurifyAssemblyDao purifyAssemblyDao;

    @Autowired
    private PurifyManager purifyManager;

    /*
        组件实体创建
        // 字符串长度的验证后期一起处理
     */
    @Override
    public Response<Boolean> createAssembly(Long parentId, PurifyAssembly purifyAssembly, Long productId) {
        Response<Boolean> result = new Response<Boolean>();

        //组件名称(这个组件重名问题不加处理，because:同名，价格不同 or 同名价格相同->用于关联不同的层次)
        if(Strings.isNullOrEmpty(purifyAssembly.getAssemblyName())){
            log.error("create purify assembly needs assembly name");
            result.setError("purify.assembly.name.null");
            return result;
        }

        //组件详情
        if(Strings.isNullOrEmpty(purifyAssembly.getAssemblyIntroduce())){
            log.error("create purify assembly needs assembly introduce");
            result.setError("purify.assembly.introduce.null");
            return result;
        }

        //组件图片
        if(Strings.isNullOrEmpty(purifyAssembly.getAssemblyImage())){
            log.error("create purify assembly needs assembly image");
            result.setError("purify.assembly.image.null");
            return result;
        }

        //组件价格
        if(purifyAssembly.getAssemblyTotal() == null){
            log.error("create purify assembly needs assembly total");
            result.setError("purify.assembly.total.null");
            return result;
        }

        //组件类目编号
        if(purifyAssembly.getCategoryId() == null){
            log.error("create purify assembly needs categoryId");
            result.setError("purify.assembly.categoryId.null");
            return result;
        }

        try{
            //创建组件&绑定关系
            result.setResult(purifyManager.createAssemblyAndRelation(parentId, purifyAssembly, productId));
        }catch(Exception e){
            log.error("create purify assembly failed, error code={}", e);
            result.setError("purify.assembly.create.failed");
        }

        return result;
    }

    /*
        更新组件实体对象
     */
    @Override
    public Response<Boolean> updateAssembly(PurifyAssembly purifyAssembly) {
        Response<Boolean> result = new Response<Boolean>();

        if(purifyAssembly.getId() == null){
            log.error("update purify assembly needs assemblyId");
            result.setError("purify.assembly.assemblyId.null");
            return result;
        }

        try{
            result.setResult(purifyAssemblyDao.update(purifyAssembly));
        }catch(Exception e){
            log.error("update purify assembly failed , assemblyId={} error code={}" , purifyAssembly.getId() , e);
            result.setError("purify.assembly.update.failed");
        }

        return result;
    }

    /*
        删除组件
     */
    @Override
    public Response<Boolean> deleteAssembly(Long assemblyId){
        Response<Boolean> result = new Response<Boolean>();

        if(assemblyId == null){
            log.error("delete purify assembly need assemblyId");
            result.setError("purify.assembly.assemblyId.null");
            return result;
        }

        try{
            purifyManager.deleteAssembly(assemblyId);
            result.setResult(true);
        }catch(Exception e){
            log.error("delete purify assembly failed, assemblyId={} error code={}", assemblyId, e);
            result.setError("purify.assembly.delete.failed");
        }

        return result;
    }

    /*
        通过编号查询组件实体信息
     */
    @Override
    public Response<PurifyAssembly> findById(Long assemblyId) {
        Response<PurifyAssembly> result = new Response<PurifyAssembly>();

        if(assemblyId == null){
            log.error("find purify assembly needs assemblyId");
            result.setError("purify.assembly.assemblyId.null");
            return result;
        }

        try{
            result.setResult(purifyAssemblyDao.findById(assemblyId));
        }catch(Exception e){
            log.error("find purify assembly failed, assemblyId={} error code={}", assemblyId, e);
            result.setError("purify.assembly.find.failed");
        }

        return result;
    }

    /*
        通过类目编号查询该类目编号下的全部组件实体
     */
    @Override
    public Response<List<PurifyAssembly>> findByCategory(Long categoryId) {
        Response<List<PurifyAssembly>> result = new Response<List<PurifyAssembly>>();

        if(categoryId == null){
            log.error("find assemblies needs categoryId");
            result.setError("purify.assembly.categoryId.null");
            return result;
        }

        try{
            List<PurifyAssembly> purifyAssemblies = purifyAssemblyDao.findByCategory(categoryId);
            if(purifyAssemblies == null){
                result.setError("purify.assembly.find.failed");
            }else{
                result.setResult(purifyAssemblies);
            }
        }catch(Exception e){
            log.error("find assembly by category failed, categoryId={} error code={}", categoryId, e);
            result.setError("purify.assembly.find.failed");
        }

        return result;
    }

    /*
        通过上级组件实体编号查询对应的下级实体编号列表
     */
    @Override
    public Response<List<PurifyAssembly>> findByAssembly(Long parentId) {
        Response<List<PurifyAssembly>> result = new Response<List<PurifyAssembly>>();

        if(parentId == null){
            log.error("find assemblies needs assembly parentId");
            result.setError("purify.assembly.parentId.null");
            return result;
        }

        try{
            List<PurifyAssembly> purifyAssemblies = purifyAssemblyDao.findByAssembly(parentId);
            if(purifyAssemblies == null){
                result.setError("purify.assembly.find.failed");
            }else{
                result.setResult(purifyAssemblies);
            }
        }catch(Exception e){
            log.error("find assembly by assembly parentId failed, parentId={} error code={}", parentId, e);
            result.setError("purify.assembly.find.failed");
        }

        return result;
    }
}
