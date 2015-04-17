package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.rrs.brand.model.*;
import com.rrs.brand.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhum01 on 2014/7/27.
 * 品牌馆首页数据运营人员维护后台console
 */
@Slf4j
@Controller
@RequestMapping("/api/brandCon")
public class BrandsClubConController {

    @Autowired
    private BrandClubService brandClubService;

    @Autowired
    private BrandClubTypeService brandClubTypeService;

    @Autowired
    private BrandClubProductService brandClubProductService;

    @Autowired
    private BrandClubProductTypeService brandClubProductTypeService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BrandRlService brandRlService;

    /**
     * 查询品牌馆信息
     * **/
    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BrandClubVo> query(){
        Response<List<BrandClubVo>> result = brandClubService.findAllBy(null,null,1);
        if(!result.isSuccess()) {
            log.error("find BrandClub failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 查询品牌馆类型
     * **/
    @RequestMapping(value = "/queryBrandType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BrandClubType> queryBrandType(){
        Response<List<BrandClubType>> result = brandClubTypeService.findAllBy();
        if(!result.isSuccess()) {
            log.error("find queryBrandType failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 更新品牌馆信息
     * @param id
     * @param brandType
     * @param brandMainImg
     * @param brandClub
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String update(@RequestParam("id") Long id,
                         @RequestParam("brandType") Long brandType,
                         @RequestParam("brandMainImg") String brandMainImg,
                         BrandClub brandClub) {
        brandClub.setId(id);
        brandClub.setBrandTypeId(brandType);
        brandClub.setBrandMainImg(brandMainImg);
        Response<Boolean> result = brandClubService.updateBrandClub(brandClub);

        if (result.isSuccess()) {
            return "ok";
        }
        log.error("failed to update {},error code:{}", brandClub, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 添加或保存品牌馆信息
     * @param id
     * @param brandClupId
     * @param productType
     * @param price
     * @param oriprice
     * @param productImages
     * @param brandClubproduct
     * @return
     */
    @RequestMapping(value = "/saveorupdate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveorupdate(@RequestParam("id") Long id,
                               @RequestParam("brandClupId") Long brandClupId,
                               @RequestParam("productType") Long productType,
                               @RequestParam("price") Long price,
                               @RequestParam("oriprice") Long oriprice,
                               @RequestParam("productImages") String productImages,
                               @RequestParam("productId") Long productId,
                               BrandClubProduct brandClubproduct) {

        Response<Item> item = itemService.findById(productId);
        if(item.isSuccess()){
            brandClubproduct.setId(id);
            brandClubproduct.setBrandClupId(brandClupId);
            brandClubproduct.setProductType(productType);
            brandClubproduct.setProductId(productId);
            if(item.getResult().getPrice()!=null){
                brandClubproduct.setPrice(item.getResult().getPrice().longValue());
            }
            if(item.getResult().getOriginPrice()!=null){
                brandClubproduct.setOriPrice(item.getResult().getOriginPrice().longValue());
            }
            if(productImages!=null && !productImages.equals("")){
                brandClubproduct.setProductImage(productImages);
            }else{
                brandClubproduct.setProductImage(item.getResult().getMainImage());
            }

        }

        Response<Boolean> result = null;
        if(brandClubproduct.getId()!=null){//修改 保存
            result = brandClubProductService.updateBrandClubProduct(brandClubproduct);
        }else{//新增 保存
            result = brandClubProductService.saveBrandClubProduct(brandClubproduct);
        }
        if (result.isSuccess()) {
            return "ok";
        }
        log.error("failed to update {},error code:{}", brandClubproduct, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 加载品牌馆信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BrandClub queryById(@RequestParam("id") int id){
        BrandClub result = brandClubService.findById(id);
        return result;
    }

    /**
     * 更改二级域名
     * @param id
     * @param brandEnName
     * @param http2
     * @param brandTradeMark
     * @return
     */
    @RequestMapping(value = "/updateBrandClubHttp2", method = RequestMethod.GET)
    public String updateBrandClubHttp2(@RequestParam("id") Long id,@RequestParam("brandEnName") String brandEnName,@RequestParam("http2") String http2,@RequestParam("brandTradeMark") String brandTradeMark){
        BrandClub brandClub=new BrandClub();
        brandClub.setId(id);
        brandClub.setBrandEnName(brandEnName);
        brandClub.setHttp2(http2);
        brandClub.setBrandTradeMark(brandTradeMark);
        brandClubService.updateBrandClubHttp2(brandClub);
        return "redirect:/operations/brandhttp2";
    }

    /**
     * 根据登录用户获取品牌信息
     * @return
     */
    @RequestMapping(value = "/getBrandByUser", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BrandClub getBrandByUser(){
        BaseUser baseUser=UserUtil.getCurrentUser();
        return this.brandClubService.findByUser(baseUser.getId().intValue());
    }

    /**
     * 查询产品类型信息
     * **/
    @RequestMapping(value = "/queryBrandProductType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BrandClubProductType> queryBrandProductType(){
        Response<List<BrandClubProductType>> result = brandClubProductTypeService.findAllBy();
        if(!result.isSuccess()) {
            log.error("find queryBrandType failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 查询品牌馆对应的产品信息
     * **/
    @RequestMapping(value = "/queryBrandProductBy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BrandClubProduct> queryBrandProductBy(@RequestParam("brandClubId") Integer brandClubId){
        Response<List<BrandClubProduct>> result = brandClubProductService.findByBrandId(brandClubId);
        if(!result.isSuccess()) {
            log.error("find queryBrandType failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

//        List<Long> productIds = new ArrayList<Long>();
//        for(int i=0;i<result.getResult().size();i++){
//            productIds.add(result.getResult().get(i).getProductId());
//        }
//        Response<List<Item>> items = itemService.findByIds(productIds);
//        if(!items.isSuccess()){
//            log.error("find itemService.findByIds failed, cause:{}", items.getError());
//            throw new JsonResponseException(500, messageSources.get(items.getError()));
//        }
        return result.getResult();
    }

    /**
     * 删除单个品牌馆对应的产品信息
     * **/
    @RequestMapping(value = "/deleteProductBy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean deleteProductBy(@RequestParam("productId") Long productId,@RequestParam("brandClubId") Long brandClubId,BrandClubProduct brandClubProduct){
//        brandClubProduct.setBrandClupId(Long.valueOf(id));
        brandClubProduct.setBrandClupId(brandClubId);
        brandClubProduct.setProductId(productId);
        Response<Boolean> result = brandClubProductService.deleteBrandClubProduct(brandClubProduct);
        if(!result.isSuccess()) {
            log.error("find queryBrandType failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 更具ID查询对应产品信息
     * **/
    @RequestMapping(value = "/queryItemById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Item queryItemById(@RequestParam("productId") Integer productId){
        Response<Item> result = itemService.findById(Long.valueOf(productId));
        if(!result.isSuccess()) {
            log.error("find queryBrandType failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        BaseUser baseUser = UserUtil.getCurrentUser();
        Response result2 = brandClubProductService.vaildateBrand(baseUser,Long.valueOf(productId));
        if(!result2.isSuccess()){
            result.setSuccess(false);
            log.error("find product error about is exist or this product is not belongs the brand");
            throw new JsonResponseException(500,messageSources.get("find product error about is exist or this product is not belongs the brand"));
        }
        return result.getResult();
    }

    /**
     * 查看当前品牌商用户登录之后查看对应店铺的item信息
     * **/
    @RequestMapping(value = "/queryItemsBy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
     public List<Item> queryItemsBy(){
        List<Item> allItemList = new ArrayList<Item>();
        BaseUser baseUser = UserUtil.getCurrentUser();//当前登录用户
        if(baseUser!=null){
            BrandClub brandClub = brandClubService.findByUser(baseUser.getId().intValue());
            if(brandClub!=null){
                Response<List<BrandRlView>> resultList = brandRlService.findRl(brandClub.getId().intValue());
                if(resultList.isSuccess()){
                    Iterator<BrandRlView> its = resultList.getResult().iterator();
                    while(its.hasNext()){//获取shopId
                        BrandRlView view = its.next();
                        Long shopId = Long.valueOf(view.getShopId());
                        Response<List<Item>> result = itemService.findByShopId(shopId);
                        if(result.isSuccess()){
                            allItemList.addAll(result.getResult());
                        }
                    }
                }
            }
        }
        return allItemList;
    }

}
