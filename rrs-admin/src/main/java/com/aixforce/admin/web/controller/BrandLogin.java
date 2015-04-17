package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.user.base.UserUtil;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandUser;
import com.rrs.brand.model.RrsBrand;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yea01 on 2014/7/26.
 */
@Controller
@RequestMapping("/api/brandUser")
public class BrandLogin {
    @Autowired
    private BrandRegisterService brandRegisterService;
    @Autowired
    private BrandClubService brandClubService;



    /**
     * 品牌馆系统登录
     * @param loginId
     * @param password
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<BrandUser> login(@RequestParam("loginId") String loginId,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
    HttpServletResponse response){
        BrandUser brandUser = new BrandUser();
        brandUser.setUserName(loginId);
        brandUser.setResourcePassword(password);
        Response<BrandUser> result = brandRegisterService.check(brandUser);
        if(result.isSuccess()) {
            request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, result.getResult().getUserId());
        }
        return result;
    }
    @RequestMapping(value = "/qianRu", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public String qianRu(@RequestParam("busId") String busId,@RequestParam("qianRuShop") String shopId,@RequestParam("experIds") String experId,HttpServletRequest request,HttpServletResponse response){
        if(busId==""||busId=="null"){
            return "redirect:/operations/experSeller";
        }
      Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("busId",busId);
        map.put("shopId",shopId);
        map.put("experId",experId);
        brandClubService.insertQr(map);
        return "redirect:/operations/experSeller";

    }
    @RequestMapping(value = "/qianChu", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public String qianChu(@RequestParam("busChuId") String busId,@RequestParam("qianChuShop") String shopId,@RequestParam("experChuIds") String experId,HttpServletRequest request,HttpServletResponse response){
        if(busId==""||busId=="null"){
            return "redirect:/operations/experSeller";
        }
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("busId",busId);
        map.put("shopId",shopId);
        map.put("experId",experId);
        brandClubService.insertQc(map);
        return "redirect:/operations/experSeller";

    }


    /**
     * 品牌馆系统登出
     * @return
     */
    @RequestMapping(value = "/logoff")
    public String logoff(){
        UserUtil.removeUser();
        return "redirect:/brand/login";
    }

    /**
     * 品牌馆用户审核成功
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/success", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> approSucc(HttpServletRequest request,
                              HttpServletResponse response){
        long brandId = Long.parseLong(request.getParameter("brandId").toString());
        String brandName = request.getParameter("brandName").toString();
        Long userId = Long.parseLong(request.getParameter("userId"));
        BrandClub brand = new BrandClub();
        brand.setId(brandId);
        brand.setBrandName(brandName);
        brand.setUserId(userId);
        RrsBrand brand2 =new RrsBrand();
        brand2.setBrandName(brandName);
        Response<String> result2 = new Response<String>();
        //判断品牌已被入驻
        boolean flag2=brandRegisterService.isExistBrand(brand2);
        if(flag2==false){
            result2.setResult("503");
        }else {
            boolean flag = brandRegisterService.approSucc(brand, brand2);
            if (flag == false) {
                result2.setResult("500");
            } else {
                result2.setResult("200");
            }
        }
        return result2;
    }

    /**
     * 品牌馆审核失败
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/failer", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public String approFail(HttpServletRequest request,HttpServletResponse response){
        long brandId = Long.parseLong(request.getParameter("brandId").toString());
        String reason = request.getParameter("failReason").toString();
        BrandClub brand = new BrandClub();
        brand.setId(brandId);
        brand.setApproReason(reason);
        brandRegisterService.approFail(brand);

        return "redirect:/operations/brand";
    }

    /**
     * 品牌馆用户登录冻结
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/frozen", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public String approFrozen(HttpServletRequest request,HttpServletResponse response){
        long brandId = Long.parseLong(request.getParameter("brandId").toString());
        BrandClub brand = new BrandClub();
        brand.setId(brandId);
        brandRegisterService.approFrozen(brand);

        return "redirect:/operations/brand";
    }

    /**
     * 品牌馆用户解冻
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/unfrozen", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public String approUnFrozen(HttpServletRequest request,HttpServletResponse response){
        long brandId = Long.parseLong(request.getParameter("brandId").toString());
        BrandClub brand = new BrandClub();
        brand.setId(brandId);
        brandRegisterService.approUnFrozen(brand);

        return "redirect:/operations/brand";
    }

    /**
     * 插入保证金
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/InsertFee", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public String approFee(HttpServletRequest request,HttpServletResponse response){
        long userId = Long.parseLong(request.getParameter("brandSellId"));
        String userName=request.getParameter("brandSellName");
        double baozhengFee=0;
        if(request.getParameter("baoZhengFee")!=null&&!("").equals(request.getParameter("baoZhengFee"))&&!("null").equals(request.getParameter("baoZhengFee"))) {
             baozhengFee =  (Double.parseDouble(request.getParameter("baoZhengFee"))*100);
        }
        double jishuFee=0;
        if(request.getParameter("jiShuFee")!=null&&!("").equals(request.getParameter("jiShuFee"))&&!("null").equals(request.getParameter("jiShuFee"))) {
            jishuFee = (Double.parseDouble(request.getParameter("jiShuFee"))*100);
        }
        BrandClub brand = new BrandClub();
        brand.setUserId(userId);
        brand.setUserName(userName);
        brand.setBaozhengFee(baozhengFee);
        brand.setJishuFee(jishuFee);
        if(brandRegisterService.vertifyFee(brand)>0) {
            brandRegisterService.updateFee(brand);

        }else{
            brandRegisterService.insertFee(brand);
        }

        return "redirect:/operations/brand";
    }
    /**
     * 修改品牌信息
     */
    @RequestMapping(value = "/UpdateBrandInfos", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateBrandInfos(HttpServletRequest request,HttpServletResponse response){
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
        brandClubService.updateBrandInfos(brandUserProfiles);
        return "redirect:/operations/brandInfos";
    }


}
