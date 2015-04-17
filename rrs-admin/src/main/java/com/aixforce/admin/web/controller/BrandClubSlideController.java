package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.rrs.brand.model.BrandClubSd;
import com.rrs.brand.service.BrandClubService;
import com.rrs.brand.service.BrandClubSlideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by zhum01 on 2014/8/5.
 */

@Slf4j
@Controller
@RequestMapping("/api/brandslide")
public class BrandClubSlideController {

    @Autowired
    private BrandClubSlideService brandClubSlideService;

    @Autowired
    private BrandClubService brandClubService;

    @Autowired
    private MessageSources messageSources;
    /**
     * 查询轮播图信息
     * **/
    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BrandClubSd> query(@RequestParam("brandId") int brandId,@RequestParam("imageType") Long imageType){
        Long userId = UserUtil.getUserId();
        Response<List<BrandClubSd>> result = brandClubSlideService.findAllByIdAndType(brandId,imageType);
        if(!result.isSuccess()) {
            log.error("find BrandClub failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 修改轮播图信息
     * **/
    @RequestMapping(value = "/update", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String update(@RequestParam("id") Long id,
            @RequestParam("httpUrl") String httpUrl,
            BrandClubSd brandClubSlide ){
        brandClubSlide.setId(id);
        brandClubSlide.setHttpUrl(httpUrl);
        Response<Boolean> result = brandClubSlideService.updateBrandClubSlide(brandClubSlide);
        if (result.isSuccess()) {
            return "ok";
        }
        log.error("failed to update {},error code:{}", brandClubSlide, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 删除轮播图信息
     * **/
    @RequestMapping(value = "/delete", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@RequestParam("id") Long id,BrandClubSd brandClubSlide){
        brandClubSlide.setId(id);
        Response<Boolean> result = brandClubSlideService.deleteBrandClubSlide(brandClubSlide);
        if(!result.isSuccess()) {
            log.error("find BrandClub failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 保存轮播图信息
     * **/
    @RequestMapping(value = "/insert", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String insert(@RequestParam("id") Long id,
                         @RequestParam("httpUrl") String httpUrl,
                         @RequestParam("mainImage") String mainImage,
                         @RequestParam("imageType") Long imageType,
                         @RequestParam("brandId") Long brandId,
                         BrandClubSd brandClubSlide){

        Response<List<BrandClubSd>> clubslideRe = brandClubSlideService.findAllByIdAndType(brandId.intValue(),imageType);
        List<BrandClubSd> resultList = clubslideRe.getResult();
        if(resultList!=null && resultList.size()>0){
            return "have";
        }else{
            brandClubSlide.setId(id);
            brandClubSlide.setImageType(imageType);
            brandClubSlide.setMainImage(mainImage);
            brandClubSlide.setHttpUrl(httpUrl);
            brandClubSlide.setBrandId(brandId);
            Response<Boolean> result = brandClubSlideService.saveBrandClubSlide(brandClubSlide);
            if(!result.isSuccess()) {
                log.error("find BrandClub failed, cause:{}", result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
        }

        return "ok";
    }
    
    /**
     * 保存轮播图信息
     * **/
    @RequestMapping(value = "/insertBanner", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String insertBanner(@RequestParam("id") Long id,
                         @RequestParam("httpUrl") String httpUrl,
                         @RequestParam("mainBannerImage") String mainBannerImage,
                         @RequestParam("imageType") Long imageType,
                         @RequestParam("brandIdBanner") Long brandIdBanner,
                         BrandClubSd brandClubSlide){

       /* Response<List<BrandClubSd>> clubslideRe = brandClubSlideService.findAllByIdAndType(brandId.intValue(),imageType);
        List<BrandClubSd> resultList = clubslideRe.getResult();
        if(resultList!=null && resultList.size()>0){
            return "have";
        }else{*/
            brandClubSlide.setId(id);
            brandClubSlide.setImageType(imageType);
            brandClubSlide.setMainImage(mainBannerImage);
            brandClubSlide.setHttpUrl(httpUrl);
            brandClubSlide.setBrandId(brandIdBanner);
            Response<Boolean> result = brandClubSlideService.saveBrandClubSlide(brandClubSlide);
            if(!result.isSuccess()) {
                log.error("find BrandClub failed, cause:{}", result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
        //}

        return "ok";
    }
}
