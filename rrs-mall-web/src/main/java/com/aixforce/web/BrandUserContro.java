package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.controller.api.Images;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandUser;
import com.rrs.brand.service.BrandClubService;
import com.rrs.brand.service.BrandRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by temp on 2014/7/10.
 */
@Controller
@RequestMapping("/api/brandUser")
public class BrandUserContro {
    @Autowired
    private BrandRegisterService brandSellerService;
    @Autowired
    private CommonConstants commonConstants;
    @Autowired
    private Images images;
    @Autowired
    private BrandClubService brandClubService;
    @Autowired
    private BrandRegisterService brandRegisterService;

    @RequestMapping(value="/vertify",method = RequestMethod.GET)
    public String vertify( HttpServletRequest request, HttpServletResponse response){
       if( request.getSession().getAttribute(CommonConstants.SESSION_USER_ID)==null||"".equals(request.getSession().getAttribute(CommonConstants.SESSION_USER_ID).toString())){
           return "redirect:/login";
       }else{
           return "redirect:/sssss";
       }
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(@RequestParam("name") String username,
                                       @RequestParam("password") String password) {
                BrandUser brandUser = new BrandUser();
                brandUser.setUserName(username);
                brandUser.setPassWord(password);
                brandSellerService.insertBrand(brandUser);

             return "redirect:/api/brandUser/verifyProfile";
    }
    @RequestMapping(value = "/verifyProfile", method = RequestMethod.GET)
    public String vertifyBrand(HttpServletRequest request,HttpServletResponse response){
        BaseUser baseUser = UserUtil.getCurrentUser();
       if(baseUser==null){
           return "redirect:/login";
       }else {
           BrandClub bc = new BrandClub();
            bc.setUserId(baseUser.getId());
           BrandClub brand = brandRegisterService.vertifyBrand(bc);
           if(brand==null){
               return "redirect:/brand/brandregist";
           }
           return "redirect:/brand/brand_infos";
       }
    }
    @RequestMapping(value = "/verifyInfos", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<BrandClub>  vertifyInfos(){
        BaseUser baseUser = UserUtil.getCurrentUser();
        return brandRegisterService.searchReason(baseUser);
    }

    @RequestMapping(value = "/registerProfile", method = RequestMethod.POST)
    @ResponseBody
    public   Response<String>  registerProfiles(HttpServletRequest request, HttpServletResponse response) {
        String brandName = "null".equals(request.getParameter("brandName"))==true?"":request.getParameter("brandName");
        String brandAppNo = "null".equals(request.getParameter("brandAppNo"))==true?"":request.getParameter("brandAppNo");
        String brandNameEn = "null".equals(request.getParameter("brandNameEn"))==true?"":request.getParameter("brandNameEn");
        String brandDesc = "null".equals(request.getParameter("brandDesc"))==true?"":request.getParameter("brandDesc");
        String brandQualify = "null".equals(request.getParameter("brandQualify"))==true?"":request.getParameter("brandQualify");
        String brandLogo = "null".equals(request.getParameter("brandLogo"))==true?"":request.getParameter("brandLogo");
        String brandTradeMark = "null".equals(request.getParameter("brandTradeMark"))==true?"":request.getParameter("brandTradeMark");
        String brandAuthor = "null".equals(request.getParameter("brandAuthor"))==true?"":request.getParameter("brandAuthor");

        BrandClub brandUserProfiles = new BrandClub();
//        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//        Map<String, MultipartFile> files = multipartRequest.getFileMap();
//        Iterator<String> fileNames = multipartRequest.getFileNames();
//        while(fileNames.hasNext()){
//            String filename = fileNames.next();
//            CommonsMultipartFile commonmuMultipartFile = (CommonsMultipartFile) files.get(filename);
//            if(!"".equals(commonmuMultipartFile.getOriginalFilename())&&!"null".equals(commonmuMultipartFile.getOriginalFilename())){
//                String tarFile = "d:/" + commonmuMultipartFile.getOriginalFilename();
//                try {
//                    FileCopyUtils.copy(commonmuMultipartFile.getBytes(), new File(tarFile));
//                    if (filename.equals("brandLogo")) {
//                        brandUserProfiles.setBrandLogo(tarFile);
//                    } else if (filename.equals("brandTradeMark")) {
//                        brandUserProfiles.setBrandTradeMark(tarFile);
//                    } else if (filename.equals("brandAuthor")) {
//                        brandUserProfiles.setBrandAuthor(tarFile);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }


        brandUserProfiles.setBrandName(brandName);
        brandUserProfiles.setUserId(Long.parseLong(request.getSession().getAttribute(CommonConstants.SESSION_USER_ID).toString()));
        brandUserProfiles.setBrandAppNo(brandAppNo);
        brandUserProfiles.setBrandDesc(brandDesc);
        brandUserProfiles.setBrandQualify(brandQualify);
        brandUserProfiles.setBrandEnName(brandNameEn);
        brandUserProfiles.setBrandLogo(brandLogo);
        brandUserProfiles.setBrandTradeMark(brandTradeMark);
        brandUserProfiles.setBrandAuthor(brandAuthor);
        int temp = brandSellerService.VertifyProfiles(brandUserProfiles);
        Response<String> result2=new Response<String>();
        if(temp==0) {
            Response<Boolean> result = brandSellerService.insertBrandProfiles(brandUserProfiles);
            if (result.getResult() == true) {
                result2.setResult("200");
            } else {
                result2.setResult("700");
            }
        }else{
            result2.setResult("500");
        }
    return result2;
    }

    @RequestMapping(value = "/shopAccess", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public String shopAccessAction(HttpServletRequest request,
                                   HttpServletResponse response){
        long brandId = Long.parseLong(request.getParameter("clubId").toString());
        brandClubService.updateShopClubKey(brandId);
        return "redirect:/seller/shop_brand_vertify";
    }
    @RequestMapping(value = "/updateBrandUserInfos", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> updateBrandUserInfos(HttpServletRequest request,HttpServletResponse response){
        String brandName = "null".equals(request.getParameter("brandName"))==true?"":request.getParameter("brandName");
        String brandAppNo = "null".equals(request.getParameter("brandAppNo"))==true?"":request.getParameter("brandAppNo");
        String brandNameEn = "null".equals(request.getParameter("brandNameEn"))==true?"":request.getParameter("brandNameEn");
        String brandDesc = "null".equals(request.getParameter("brandDesc"))==true?"":request.getParameter("brandDesc");
        String brandQualify = "null".equals(request.getParameter("brandQualify"))==true?"":request.getParameter("brandQualify");
        String brandLogo = "null".equals(request.getParameter("brandLogo"))==true?"":request.getParameter("brandLogo");
        String brandTradeMark = "null".equals(request.getParameter("brandTradeMark"))==true?"":request.getParameter("brandTradeMark");
        String brandAuthor = "null".equals(request.getParameter("brandAuthor"))==true?"":request.getParameter("brandAuthor");
        BrandClub brandUserProfiles = new BrandClub();
        brandUserProfiles.setBrandName(brandName);
        brandUserProfiles.setUserId(Long.parseLong(request.getSession().getAttribute(CommonConstants.SESSION_USER_ID).toString()));
        brandUserProfiles.setBrandAppNo(brandAppNo);
        brandUserProfiles.setBrandDesc(brandDesc);
        brandUserProfiles.setBrandQualify(brandQualify);
        brandUserProfiles.setBrandEnName(brandNameEn);
        brandUserProfiles.setBrandLogo(brandLogo);
        brandUserProfiles.setBrandTradeMark(brandTradeMark);
        brandUserProfiles.setBrandAuthor(brandAuthor);
        brandRegisterService.updateBrandUserInfos(brandUserProfiles);
        Response<String> result = new Response<String>();
        result.setSuccess(true);
        return result;
    }

}
