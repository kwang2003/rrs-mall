package com.aixforce.rrs.purify.manager;

import com.aixforce.rrs.purify.dao.PurifyAssemblyDao;
import com.aixforce.rrs.purify.dao.PurifyCategoryDao;
import com.aixforce.rrs.purify.dao.PurifyRelationDao;
import com.aixforce.rrs.purify.dao.PurifySeriesDao;
import com.aixforce.rrs.purify.model.PurifyAssembly;
import com.aixforce.rrs.purify.model.PurifyCategory;
import com.aixforce.rrs.purify.model.PurifyRelation;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Desc:定制的一些处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-11.
 */
@Slf4j
@Component
public class PurifyManager {
    @Autowired
    private PurifySeriesDao purifySeriesDao;

    @Autowired
    private PurifyCategoryDao purifyCategoryDao;

    @Autowired
    private PurifyAssemblyDao purifyAssemblyDao;

    @Autowired
    private PurifyRelationDao purifyRelationDao;

    /**
     * 创建组件的同时需要绑定组件之间的关系
     * @param parentId          上级组件编号(可以为null:表示没有关联，不需要创建关系)
     * @param purifyAssembly    组件对象
     * @param productId         产品编号（可以为null：表示为绑定到最终的商品）
     * @return Boolean
     * 返回创建是否成功(添加事务管理)
     */
    @Transactional
    public Boolean createAssemblyAndRelation(Long parentId, PurifyAssembly purifyAssembly, Long productId){
        //创建组件对象&返回组件编号
        Long childId = purifyAssemblyDao.create(purifyAssembly);
        log.debug("succeed to create assembly");

        if(parentId != null){
            //组件创建成功-》绑定关系
            PurifyRelation purifyRelation = new PurifyRelation();
            purifyRelation.setAssemblyParent(parentId);
            purifyRelation.setAssemblyChild(childId);
            purifyRelation.setProductId(productId == null ? 0 : productId);

            purifyRelationDao.create(purifyRelation);
            log.debug("succeed to create relation");
        }

        return true;
    }

    /**
     * 通过系列编号删除系列&系列下的所有关联类目&组件(以及该类目下的组件与其他组件的关联关系)
     * @param seriesId  系列编号
     * @return Boolean
     * 返回删除结果
     */
    @Transactional
    public Boolean deleteSeries(Long seriesId){

        log.debug("deleteSeries transactional start");
        //删除系列
        purifySeriesDao.delete(seriesId);

        //获取系列下的全部类目
        List<PurifyCategory> categoryList = purifyCategoryDao.findBySeriesId(seriesId);

        //获取类目编号集合
        List<Long> categoryIds = Lists.transform(categoryList , new Function<PurifyCategory , Long>(){
            @Override
            public Long apply(PurifyCategory input) {
                return input.getId();
            }
        });

        //通过系列编号删除旗下的全部类目
        purifyCategoryDao.deleteBySeriesIds(new Long[]{seriesId});

        //通过类目编号集合获取全部类目下的所有组件对象
        purifyAssemblyDao.deleteByCategoryIds((Long[])categoryIds.toArray());

        //获取组件编号
        List<PurifyAssembly> purifyAssemblies = purifyAssemblyDao.findByCategoryIds((Long[])categoryIds.toArray());

        //通过组件编号集合删除对应的关系对象
        purifyRelationDao.deleteByAssemblyIds((Long[])Lists.transform(purifyAssemblies , new Function<PurifyAssembly , Long>(){
            @Override
            public Long apply(PurifyAssembly input) {
                return input.getId();
            }
        }).toArray());

        log.debug("deleteSeries transactional end");

        return true;
    }

    /**
     * 通过类目编号删除(以及该类目下的组件与其他组件的关联关系)
     * @param categoryId  类目编号
     * @return Boolean
     * 返回删除结果
     */
    @Transactional
    public Boolean deleteCategory(Long categoryId){
        log.debug("deleteCategory transactional start");
        //删除类目
        purifyCategoryDao.delete(categoryId);

        //获取组件编号
        List<PurifyAssembly> purifyAssemblies = purifyAssemblyDao.findByCategoryIds(new Long[]{categoryId});

        //删除组件
        purifyAssemblyDao.deleteByCategoryIds(new Long[]{categoryId});

        //通过组件编号集合删除对应的关系对象
        purifyRelationDao.deleteByAssemblyIds((Long[])Lists.transform(purifyAssemblies , new Function<PurifyAssembly , Long>(){
            @Override
            public Long apply(PurifyAssembly input) {
                return input.getId();
            }
        }).toArray());

        log.debug("deleteCategory transactional end");

        return true;
    }

    /**
     * 通过组件编号删除
     * @param assemblyId  组件编号
     * @return Boolean
     * 返回删除结果
     */
    @Transactional
    public Boolean deleteAssembly(Long assemblyId){
        log.debug("deleteCategory transactional start");

        //删除组件
        purifyAssemblyDao.delete(assemblyId);

        //通过组件编号集合删除对应的关系对象
        purifyRelationDao.deleteByAssemblyIds(new Long[]{assemblyId});

        log.debug("deleteCategory transactional end");

        return true;
    }
}
