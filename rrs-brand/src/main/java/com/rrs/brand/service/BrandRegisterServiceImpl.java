package com.rrs.brand.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.rrs.brand.dao.BrandClubDao;
import com.rrs.brand.dao.BrandUserDao;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandUser;
import com.rrs.brand.model.RrsBrand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


/**
 * Created by temp on 2014/7/10.
 */
@Service
public class BrandRegisterServiceImpl implements BrandRegisterService {

    private final static HashFunction md5 = Hashing.md5();
    private final static Joiner joiner = Joiner.on('@').skipNulls();
    private final static HashFunction sha512 = Hashing.sha512();
    private final static Splitter splitter = Splitter.on('@').trimResults();

    @Autowired
    private BrandUserDao brandUserDao;

    @Autowired
    private BrandClubDao brandClubDao;

    @Override
    public Response<BrandUser> check(BrandUser brandUser) {
        BrandUser result = brandUserDao.searchBrandUser(brandUser);
        Response<BrandUser> response = new Response<BrandUser>();
        if (result == null) {
            response.setResult(null);
            response.setError("该用户名不存在！");
        } else {
            // 标识密码是否匹配
            boolean flag = passwordMatch(brandUser.getResourcePassword(), result.getPassWord());
            if (flag) {
                response.setSuccess(true);
                response.setResult(result);
            } else {
                response.setSuccess(false);
                response.setResult(null);
                response.setError("用户名和密码不匹配！");
            }
        }
        return response;
    }


    @Override
    public Response<Boolean> insertBrand(BrandUser brandUser) {
        Response<Boolean> result = new Response<Boolean>();
        String tempPwd=brandUser.getPassWord();
        brandUser.setPassWord(encryptPassword(tempPwd));
        result.setResult(brandUserDao.insertBrandUser(brandUser));
        return result;
    }

    @Override
    public Response<Boolean> insertBrandProfiles(BrandClub brandUserProfiles) {
        Response<Boolean> result = new Response<Boolean>();
        result.setResult(brandUserDao.insertBrandUserProfiles(brandUserProfiles));
        return result;
    }

    @Override
    public int VertifyProfiles(BrandClub brandUserProfiles) {
        return brandUserDao.insertBrandVerity(brandUserProfiles);
    }

    @Override
    public Response<List<BrandClub>> showBrandSeller(String sellerName,String brandSearchName, int pinpai, int status) {
        Response<List<BrandClub>> result = new Response<List<BrandClub>>();
        if(brandUserDao.showBrandUser(sellerName,brandSearchName,pinpai,status).size()>0) {
            result.setResult(brandUserDao.showBrandUser(sellerName,brandSearchName,pinpai,status));
            result.setSuccess(true);
        }else{
            result.setSuccess(false);
            result.setError("查询品牌商列表失败!");
        }
        return result;
    }

    @Override
    public boolean approSucc(BrandClub brandUserProfiles,RrsBrand brand) {
      return brandUserDao.updateSucc(brandUserProfiles,brand);
    }

    @Override
    public void approFail(BrandClub brandUserProfiles) {
        brandUserDao.updateFail(brandUserProfiles);
    }

    @Override
    public void approFrozen(BrandClub brandUserProfiles) {
        brandUserDao.updateFrozen(brandUserProfiles);
    }
    @Override
    public void approUnFrozen(BrandClub brandUserProfiles) {
        brandUserDao.updateUnFrozen(brandUserProfiles);
    }

    @Override
    public void insertFee(BrandClub brandClub) {
    brandUserDao.insertFee(brandClub);
    }

    @Override
    public boolean isExistBrand(RrsBrand brand) {
        if(brandUserDao.brandIsExist(brand)==0){
            return true;
        }else{
        return false;
        }
    }


    @Override
    public int vertifyFee(BrandClub brandClub) {
      return  brandUserDao.feeVerity(brandClub);
    }

    @Override
    public void updateFee(BrandClub brandClub) {
        brandUserDao.updateFee(brandClub);
    }

    @Override
    public Response<List<BrandClub>> findKeyByUser(@ParamInfo("baseUser") BaseUser baseUser) {
         Response<List<BrandClub>> result = new  Response<List<BrandClub>>();
        long userId = baseUser.getId();
        result.setResult(brandClubDao.searchRl(userId));
        result.setSuccess(true);
        return result;
    }

    @Override
    public BrandClub vertifyBrand(BrandClub brandClub) {
        return brandUserDao.verifyBrand(brandClub);
    }

    @Override
    public Response<BrandClub> searchReason(BaseUser baseUser) {
       BrandClub brandClub = new BrandClub();
       brandClub.setUserId(baseUser.getId());
       BrandClub result =  brandUserDao.verifyBrand(brandClub);
       if("0".equals(result.getStatus())){
           result.setApproReason("正在审核中,请耐心等待");
       }else if("1".equals(result.getStatus())){
           result.setApproReason("品牌商已审核通过。不能重复注册");
       }else if("2".equals(result.getStatus())){
           String tempResult= result.getApproReason();
           result.setApproReason("审核失败。原因："+tempResult);
       }
        Response<BrandClub> resultBc = new Response<BrandClub>();
        resultBc.setResult(result);
        return resultBc;
    }

    @Override
    public Response<BrandClub> findKeyForUpdate(@ParamInfo("baseUser") BaseUser baseUser) {
        Response<BrandClub> result = new Response<BrandClub>();
        BrandClub brandClub = brandUserDao.updateBrandInfo(baseUser.getId());
        if(brandClub==null){
        result.setSuccess(false);
        result.setResult(null);
        }else{
            result.setSuccess(true);
            result.setResult(brandClub);
        }
        return result;
    }

    @Override
    public void updateBrandUserInfos(BrandClub brandClub) {
        brandUserDao.updateBrandInfos(brandClub);
    }


    /**
     * 校验 加盐密码
     * @param password
     * @param encryptedPassword
     * @return
     */
    private boolean passwordMatch(String password, String encryptedPassword) {
        Iterable<String> parts = splitter.split(encryptedPassword);
        String salt = Iterables.get(parts, 0);
        String realPassword = Iterables.get(parts, 1);
        return Objects.equal(sha512.hashUnencodedChars(password + salt).toString().substring(0, 20), realPassword);
    }

    /**
     * 生成加盐密码
     * @param password
     * @return
     */
    public  String encryptPassword(String password) {
        String salt = md5.newHasher().putUnencodedChars(UUID.randomUUID().toString()).putLong(System.currentTimeMillis()).hash()
                .toString().substring(0, 4);
        String realPassword = sha512.hashUnencodedChars(password + salt).toString().substring(0, 20);
        return joiner.join(salt, realPassword);
    }
//    public static void main(String args[]){
//        BrandSellerServiceImpl ac = new BrandSellerServiceImpl();
//        System.out.println(ac.encryptPassword("12345"));
//        System.out.println(ac.passwordMatch("12345","0bcc@fa9ff69b1b052e16d040"));
//    }

}
