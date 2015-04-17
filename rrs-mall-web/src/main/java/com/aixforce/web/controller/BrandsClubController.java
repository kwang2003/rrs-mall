package com.aixforce.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.alibaba.dubbo.common.utils.StringUtils;
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
import java.util.List;
import java.util.TreeMap;

/**
 * Created by zhum01 on 2014/7/25.
 */

@Slf4j
@Controller
@RequestMapping("/api/brands")
public class BrandsClubController {

    @Autowired
    private BrandClubService brandClubService;

    @Autowired
    private BrandClubTypeService brandClubTypeService;

    @Autowired
    private BrandClubProductService brandClubProductService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private BrandClubSlideService brandClubSlideService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private BrandRlService brandRlService;

    @Autowired
    private ItemService itemService;

    /**
     * 查询轮播图信息
     * **/
    @RequestMapping(value = "/querySlide", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BrandClubSd> query(@RequestParam("imageType") Long imageType){
        Response<List<BrandClubSd>> result = brandClubSlideService.findAllByIdAndType(-1,imageType);
        if(!result.isSuccess()) {
            log.error("find BrandClub failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TreeMap query(@RequestParam("status") Integer status){
        Response<List<BrandClubVo>> result = brandClubService.findAllBy(null,-1,status);
        List<BrandClubVo> showList = result.getResult();
        List<BrandClubVo> resultList = null;
        int slength = showList.size();
        TreeMap<String, List<BrandClubVo>> showMap = new TreeMap<String, List<BrandClubVo>>();
        for (int i = 0; i < slength; i++) {
            BrandClubVo bshow = showList.get(i);
            String key = bshow.getBrandTypeId()+"-"+bshow.getBrandTypeName();
            if(showMap.containsKey(key)){
                resultList = showMap.get(key);
                resultList.add(bshow);
            }else{
                resultList = new ArrayList<BrandClubVo>();
                resultList.add(bshow);
                showMap.put(key, resultList);
            }
        }
        Response<TreeMap> resultMap = new Response<TreeMap>();
        resultMap.setResult(showMap);
        if(!result.isSuccess()) {
            log.error("find BrandClub failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return resultMap.getResult();
    }

    @RequestMapping(value = "/redirectShops", method = RequestMethod.GET)
    public String redirectShops(@RequestParam("sellerId") Integer sellerId){
        Integer ownerId = 0;
        BrandClub brandClub = brandClubService.findByUser(sellerId.intValue());
        BrandsClubKey brandsClubKey = null;
        if(brandClub!=null){
            List<BrandsClubKey> brandclubkeys = brandRlService.findbrandKeyByBrandId(brandClub.getId());
            if(brandclubkeys!=null && brandclubkeys.size()>0){
                brandsClubKey = brandclubkeys.get(0);
            }
        }

        if(brandsClubKey!=null){
            int shopId = brandsClubKey.getShopId();
            Response<Shop> shopResponse = shopService.findById(Long.valueOf(shopId));
            if(shopResponse.isSuccess()){
                return "redirect:/shops/"+shopResponse.getResult().getUserId();
            }
        }
        return "redirect:/";
    }

//    /**
//     * 前台查询品牌馆信息
//     *
//     * **/
//    @RequestMapping(value = "/queryby", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public List<BrandClub> queryby(@RequestParam("brandName") String brandName){
//        HashMap<Object,Object> paramMap = new HashMap<Object, Object>();
//        paramMap.put("brandName",brandName);
//        Response<List<BrandClub>> result = brandClubService.findBrandClubBy(paramMap);
//        if(!result.isSuccess()) {
//            log.error("find BrandClub failed, cause:{}", result.getError());
//            throw new JsonResponseException(500, messageSources.get(result.getError()));
//        }
//        return result.getResult();
//    }

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
     * 查询品牌馆对于的产品信息
     * **/
    @RequestMapping(value = "/queryBrandProduct", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TreeMap queryBrandProduct(@RequestParam("brandId") Integer brandId){
        Response<TreeMap> resultMap = new Response<TreeMap>();
        resultMap = queryProductByBrandId(brandId);
        return resultMap.getResult();
    }
    @RequestMapping(value = "/findAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Addresses>> findAddress(@RequestParam("provinceId") Integer provinceId){
      Response<List<Addresses>> result = brandClubService.findAddress(provinceId);
        return result;
    }
    @RequestMapping(value = "/findProvince", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Addresses>> findProvince(){
        Response<List<Addresses>> result = brandClubService.findProvince();
        return result;
    }

    @RequestMapping(value = "/findMall", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<ExperinceMall>> findMall(@RequestParam("provinceId") Integer provinceId,@RequestParam("cityId") Integer cityId){
        Response<List<ExperinceMall>> result = brandClubService.findMall(provinceId,cityId);
        return result;
    }
    @RequestMapping(value = "/insertMall", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Integer> insertMall(@RequestParam("mallId") Integer mallId){
        BaseUser baseUser = UserUtil.getCurrentUser();
        Response<Integer> result = brandClubService.insertMall(mallId,baseUser.getId());
        return result;
    }
    @RequestMapping(value = "/exitMall", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Integer> exitMall(){
        BaseUser baseUser = UserUtil.getCurrentUser();
        brandClubService.exitMall(baseUser.getId());
        Response<Integer> result = new Response<Integer>();
        result.setResult(200);
        return result;
    }
    @RequestMapping(value = "/findStatus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Integer> findStatus(){
        BaseUser baseUser = UserUtil.getCurrentUser();
        Response<Integer> result =brandClubService.findStatus(baseUser.getId());
//        Response<Integer> result = new Response<Integer>();
//        result.setResult(200);
        return result;
    }



    /**
     * 根据brandId 查询 对应的品牌馆信息
     * **/
    @RequestMapping(value = "/queryBrandById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BrandClub queryBrandById(@RequestParam("brandId") Long brandId){
        Response<BrandClub> result = brandClubService.queryBrandById(brandId);
        if(!result.isSuccess()) {
            log.error("find queryBrandType failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }
    /**
     * 根据SellerId查询对应店铺所属的品牌产品信息
     * @param sellerId
     * @return
     */
    @RequestMapping(value = "/queryBysellerId", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TreeMap queryBysellerId(@RequestParam("sellerId") Long sellerId){
        //查询当前sellerId 对应的 shopId
        Response<Shop> shop = shopService.findByUserId(sellerId);
        if(shop.isSuccess()){
            //根据shopId查看是否存在认领的品牌馆信息 如果存在多个选择其中一个
            Long shopId = shop.getResult().getId();
            List<BrandsClubKey> resultList = brandRlService.findbrandKeyByShopId(Integer.valueOf(shopId + ""));
            int brandId = 0;
            if(resultList!=null && resultList.size()>0){
                BrandsClubKey brandsClubKey = resultList.get(0);
                brandId = brandsClubKey.getBrandClubId();
                return queryProductByBrandId(brandId).getResult();
            }else{
                return null;
            }
        }
        return null;
    }




    public Response<TreeMap> queryProductByBrandId(int brandId){
        Response<TreeMap> resultMap = new Response<TreeMap>();
        if(!StringUtils.isEmpty(String.valueOf(brandId))){
            Response<List<BrandClubProduct>> result = brandClubProductService.findByBrandId(brandId);
            List<BrandClubProduct> showList = result.getResult();
            List<Item> resultList = null;
            int slength = showList.size();
            TreeMap<String, List<Item>> showMap = new TreeMap<String, List<Item>>();

            for (int i = 0; i < slength; i++) {
                BrandClubProduct bshow = showList.get(i);
                Response<Item> itemObjs = itemService.findById(bshow.getProductId());
                Item itemObj = null;
                if(itemObjs.isSuccess()){
                    itemObj = itemObjs.getResult();
                    if(bshow.getProductImage()!=null && !bshow.getProductImage().equals("")){
                        itemObj.setMainImage(bshow.getProductImage());
                    }
                String key = bshow.getBrandClupId()+"-"+bshow.getProductType();
                if(showMap.containsKey(key)){
                    resultList = showMap.get(key);
                    resultList.add(itemObj);
                }else{
                    resultList = new ArrayList<Item>();
                    resultList.add(itemObj);
                    showMap.put(key, resultList);
                }
                }
            }
            resultMap.setResult(showMap);
            if(!result.isSuccess()) {
                log.error("find BrandClub failed, cause:{}", result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
        }
        return resultMap;
    }

    /**
     * 根据SellerId查询对应店铺所属的品牌的轮播信息
     * @param sellerId
     * @return
     */
    @RequestMapping(value = "/querySlideBysellerId", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public  List<BrandClubSd> querySlideBysellerId(@RequestParam("sellerId") Long sellerId,@RequestParam("imageType") Long imageType){
        //查询当前sellerId 对应的 shopId
        Response<List<BrandClubSd>> resultSlide = new  Response<List<BrandClubSd>>();
        Response<Shop> shop = shopService.findByUserId(sellerId);
        if(shop.isSuccess()){
            //根据shopId查看是否存在认领的品牌馆信息 如果存在多个选择其中一个
            Long shopId = shop.getResult().getId();
            List<BrandsClubKey> resultList = brandRlService.findbrandKeyByShopId(Integer.valueOf(shopId + ""));
            int brandId = 0;
            if(resultList!=null && resultList.size()>0){
                BrandsClubKey brandsClubKey = resultList.get(0);
                brandId = brandsClubKey.getBrandClubId();
                resultSlide =  brandClubSlideService.findAllByIdAndType(brandId,imageType);
                if(resultSlide.isSuccess()){
                    return resultSlide.getResult();
                }
            }else{
                return null;
            }
        }
        return null;
    }

}


