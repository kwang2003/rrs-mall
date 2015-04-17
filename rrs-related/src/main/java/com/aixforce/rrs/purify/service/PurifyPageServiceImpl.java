package com.aixforce.rrs.purify.service;

import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.purify.dao.PurifyAssemblyDao;
import com.aixforce.rrs.purify.dao.PurifyCategoryDao;
import com.aixforce.rrs.purify.dto.PurifyPageDto;
import com.aixforce.rrs.purify.dto.PurifyProduct;
import com.aixforce.rrs.purify.model.PurifyAssembly;
import com.aixforce.rrs.purify.model.PurifyCategory;
import com.aixforce.rrs.purify.model.PurifyRelation;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-14.
 */
@Service
@Slf4j
public class PurifyPageServiceImpl implements PurifyPageService {
    @Autowired
    private PurifyAssemblyService purifyAssemblyService;

    @Autowired
    private PurifyRelationService purifyRelationService;

    @Autowired
    private PurifyCategoryDao purifyCategoryDao;

    @Autowired
    private PurifyAssemblyDao purifyAssemblyDao;

    @Autowired
    private ItemService itemService;

    /*
        查询页面信息
     */
    public Response<PurifyPageDto> findPurifyPageInfo(Long seriesId, Long[] assemblyIds){
        Response<PurifyPageDto> result = new Response<PurifyPageDto>();

        if(seriesId == null){
            log.error("find purify category need seriesId");
            result.setError("purify.series.seriesId.null");
            return result;
        }

        try{
            //装载类目信息
            PurifyPageDto purifyPageDto = new PurifyPageDto();

            //未选取组件时返回默认的类目以及类目下的组件（默认获取）
            if(assemblyIds == null){
                //得到默认的类目对象
                PurifyCategory purifyCategory = purifyCategoryDao.findDefaultBySeriesId(seriesId);
                purifyPageDto.setPurifyCategoryList(Lists.newArrayList(purifyCategory));

                //获取新类目下的组件
                Response<List<PurifyAssembly>> response = purifyAssemblyService.findByCategory(purifyCategory.getId());
                if(response.isSuccess()){
                    // 需要将每个组件的价格范围查询出来(这个代价太大，先不考虑)
                    purifyPageDto.setPurifyAssemblyList(response.getResult());
                    result.setResult(purifyPageDto);

                    return result;
                }else{
                    result.setError(response.getError());
                    return result;
                }
            }else{
                //通过组件编号查询页面信息
                return findProductInfo(assemblyIds);
            }
        }catch(Exception e){
            log.error("find purify page information failed, assemblyIds={} error code={}", assemblyIds, e);
            result.setError("purify.page.find.failed");
        }

        return result;
    }

    /**
     * 通过组件编号查询页面信息
     * @param assemblyIds   组件列表
     * @return PurifyPageDto
     * 返回一个封装好的页面数据
     */
    private Response<PurifyPageDto> findProductInfo(Long[] assemblyIds){
        Response<PurifyPageDto> result = new Response<PurifyPageDto>();
        PurifyPageDto purifyPageDto = new PurifyPageDto();

        //当前的组件编号 (跳转的当前组件编号)
        Long childId = assemblyIds[assemblyIds.length-1];
        try{
            //上一级的组件编号（指跳转时的组件编号）->存在需要判断是否已经达到末尾有货物
            if(assemblyIds.length-2 >= 0){
                Long parentId = assemblyIds[assemblyIds.length-2];

                //是否已经达到最末尾(是指找到了货物)
                Response<PurifyRelation> relationResponse = purifyRelationService.findRelation(parentId , childId);

                if(relationResponse.isSuccess()){
                    if(relationResponse.getResult() == null){
                        log.error("can't find purify relation by parentId={} childId={}", parentId, childId);
                        result.setError("purify.relation.find.failed");
                        return result;
                    }else{
                        //获取商品信息注入到PurifyPageDto对象中
                        if(relationResponse.getResult().getProductId() != 0){
                            //获取商品信息
                            Response<Item> productRes = itemService.findById(relationResponse.getResult().getProductId());
                            if(!productRes.isSuccess()){
                                log.error("can't find item by productId={}, error code={}", relationResponse.getResult().getProductId(), productRes.getError());
                                result.setError(productRes.getError());
                                return result;
                            }

                            //获取组件信息
                            List<PurifyAssembly> purifyAssemblies = purifyAssemblyDao.findByAssemblyIds(assemblyIds);

                            PurifyProduct purifyProduct = transformItem(productRes.getResult());
                            purifyProduct.setPurifyAssemblyList(purifyAssemblies);
                            purifyPageDto.setPurifyProduct(purifyProduct);
                        }
                    }
                }else{
                    result.setError(relationResponse.getError());
                    return result;
                }
            }

            //当前的组件编号获取下家组件信息
            Response<List<PurifyAssembly>> response = purifyAssemblyService.findByAssembly(childId);
            if(response.isSuccess()){
                purifyPageDto.setPurifyAssemblyList(response.getResult());
            }else{
                result.setError(response.getError());
                return result;
            }

            //获取以前的类目对象信息
            List<PurifyCategory> purifyCategoryList = purifyCategoryDao.findByAssemblyIds(assemblyIds);

            //获取当前的类目对象
            purifyCategoryList.addAll(purifyCategoryDao.findByAssemblyIds(new Long[]{response.getResult().isEmpty() ? null : response.getResult().get(0).getId()}));
            purifyPageDto.setPurifyCategoryList(purifyCategoryList);

            result.setResult(purifyPageDto);

        }catch(Exception e){
            log.error("can't find any product or assembly , assemblyIds={} error code={}", assemblyIds, e);
            result.setError("purify.assembly.find.failed");
        }

        return result;
    }

    /**
     * 将item数据装载到PurifyProduct中
     * @param item  商品信息
     * @return  PurifyProduct
     * 返回商品详细信息
     */
    private PurifyProduct transformItem(Item item){
        PurifyProduct purifyProduct = new PurifyProduct();

        purifyProduct.setId(item.getId());
        purifyProduct.setSpuId(item.getSpuId());
        purifyProduct.setUserId(item.getUserId());
        purifyProduct.setShopId(item.getShopId());
        purifyProduct.setBrandId(item.getBrandId());
        purifyProduct.setName(item.getName());
        purifyProduct.setMainImage(item.getMainImage());
        purifyProduct.setTradeType(item.getTradeType());
        purifyProduct.setStatus(item.getStatus());
        purifyProduct.setFreightModelId(item.getFreightModelId());
        purifyProduct.setQuantity(item.getQuantity());
        purifyProduct.setSoldQuantity(item.getSoldQuantity());
        purifyProduct.setPrice(item.getPrice());
        purifyProduct.setOriginPrice(item.getOriginPrice());
        purifyProduct.setRegion(item.getRegion());
        purifyProduct.setOnShelfAt(item.getOnShelfAt());
        purifyProduct.setCreatedAt(item.getCreatedAt());
        purifyProduct.setUpdatedAt(item.getUpdatedAt());

        return purifyProduct;
    }
}
