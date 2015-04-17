package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.dao.UserTradeInfoDao;
import com.aixforce.trade.manager.TradeInfoManager;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.Address;
import com.aixforce.user.service.AddressService;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-14
 */
@Service
public class UserTradeInfoServiceImpl implements UserTradeInfoService {

    private final static Logger log = LoggerFactory.getLogger(UserTradeInfoService.class);

    @Autowired
    private UserTradeInfoDao userTradeInfoDao;

    @Autowired
    private TradeInfoManager tradeInfoManager;

    @Autowired
    private AddressService addressService;

    @Override
    public Response<Long> create(UserTradeInfo userTradeInfo) {
        Response<Long> result = new Response<Long>();
        try {
            if(!Strings.isNullOrEmpty(userTradeInfo.getPhone())) {
                userTradeInfo.setPhone(userTradeInfo.getPhone().trim());
            }
            String province = addressService.findById(userTradeInfo.getProvinceCode()).getResult().getName();
            String city = addressService.findById(userTradeInfo.getCityCode()).getResult().getName();
            String district = addressService.findById(userTradeInfo.getDistrictCode()).getResult().getName();
            userTradeInfo.setProvince(province);
            userTradeInfo.setCity(city);
            userTradeInfo.setDistrict(district);
            Long id = tradeInfoManager.create(userTradeInfo);
            result.setResult(id);
            return result;
        } catch (Exception e) {
            log.error("failed to create {},cause:{}", userTradeInfo, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.create.failed");
            return result;
        }
    }

    @Override
    public Response<List<UserTradeInfo>> findTradeInfosByUserId(Long userId) {
        Response<List<UserTradeInfo>> result = new Response<List<UserTradeInfo>>();
        if (userId == null) {
            log.error("userId can not be null");
            result.setError("userid.not.null.fail");
            return result;
        }
        try {
            List<UserTradeInfo> userTradeInfos = userTradeInfoDao.findValidByUserId(userId);
            result.setResult(userTradeInfos);
            return result;
        } catch (Exception e) {
            log.error("failed to find userTradeInfos for user(id={}),cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.not.found");
            return result;
        }
    }

    @Override
    public Response<List<UserTradeInfo>> findTradeInfosByUser(BaseUser baseUser) {
        return findTradeInfosByUserId(baseUser.getId());
    }

    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        if (id == null) {
            log.error("userTradeInfo's id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            userTradeInfoDao.delete(id);
            log.debug("delete userTradeInfo {}", id);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete userTradeInfo where id={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.delete.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> invalidate(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        if (id == null) {
            log.error("userTradeInfo's id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            userTradeInfoDao.invalidate(id);
            log.debug("invalidate userTradeInfo {}", id);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to invalidate userTradeInfo where id={},cause:{}", id,
                    Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.invalid.failed");
            return result;
        }
    }

    @Override
    public Response<UserTradeInfo> findById(Long id) {
        Response<UserTradeInfo> result = new Response<UserTradeInfo>();
        if (id == null) {
            log.error("userTradeInfo's id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            UserTradeInfo userTradeInfo = userTradeInfoDao.findById(id);
            if (userTradeInfo == null) {
                log.error("UserTradeInfo(id={}) not found", id);
                result.setError("user.trade.info.not.found");
                return result;
            }
            result.setResult(userTradeInfo);
            return result;
        } catch (Exception e) {
            log.error("failed to find userTradeInfo where id={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.not.found");
            return result;
        }
    }



    @Override
    public Response<Long> update(UserTradeInfo userTradeInfo, Long userId) {
        Response<Long> result = new Response<Long>();

        if (userTradeInfo.getId() == null) {
            log.error("userTradeInfo's id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        UserTradeInfo exist = userTradeInfoDao.findById(userTradeInfo.getId());
        if (exist == null) {
            log.error("UserTradeInfo(id={}) not found", userTradeInfo.getId());
            result.setError("user.trade.info.not.found");
            return result;
        }
        if (!Objects.equal(exist.getUserId(), userId)) {
            log.error("user don't have the right to update");
            result.setError("userTradeInfo.update.failed");
            return result;
        }
        try {
            if(!Strings.isNullOrEmpty(userTradeInfo.getPhone())) {
                userTradeInfo.setPhone(userTradeInfo.getPhone().trim());
            }
            Response<Address> provinceR = addressService.findById(userTradeInfo.getProvinceCode());
            if(!provinceR.isSuccess()) {
                log.error("fail to find address by id{},error code:{}",
                        userTradeInfo.getProvinceCode(), provinceR.getError());
                result.setError(provinceR.getError());
                return result;
            }
            String province = provinceR.getResult().getName();
            Response<Address> cityR = addressService.findById(userTradeInfo.getCityCode());
            if(!cityR.isSuccess()) {
                log.error("fail to find address by id{},error code:{}",
                        userTradeInfo.getCityCode(), cityR.getError());
                result.setError(cityR.getError());
                return result;
            }
            String city = cityR.getResult().getName();
            Response<Address> districtR = addressService.findById(userTradeInfo.getDistrictCode());
            if(!districtR.isSuccess()) {
                log.error("fail to find address by id{},error code:{}",
                        userTradeInfo.getDistrictCode(), districtR.getError());
                result.setError(districtR.getError());
                return result;
            }
            String district = districtR.getResult().getName();
            userTradeInfo.setProvince(province);
            userTradeInfo.setCity(city);
            userTradeInfo.setDistrict(district);
            userTradeInfo.setUserId(userId);
            Long newId = tradeInfoManager.update(userTradeInfo);
            log.debug("update userTradeInfo {}", userTradeInfo);
            result.setResult(newId);
            return result;
        } catch (Exception e) {
            log.error("failed to update {},cause:{}", userTradeInfo, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.update.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> makeDefault(Long userId, Long id) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("userId can not be null");
            result.setError("userid.not.null.fail");
            return result;
        }
        if (id == null) {
            log.error("userTradeInfo's id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            boolean success = tradeInfoManager.makeDefault(userId, id);
            result.setResult(success);
            return result;
        } catch (Exception e) {
            log.error("failed to make userTradeInfo(id={}) as default,cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.makeDefault.failed");
            return result;
        }
    }


    @Override
    public Response<UserTradeInfo> findDefault(Long userId) {

        Response<UserTradeInfo> result = new Response<UserTradeInfo>();
        if (userId == null) {
            log.error("userTradeInfo's userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            UserTradeInfo userTradeInfo = userTradeInfoDao.findDefault(userId);
            if (userTradeInfo == null) {
                log.error("UserTradeInfo default by user(id={}) not found", userId);
                result.setError("user.default.trade.info.not.found");
                return result;
            }
            result.setResult(userTradeInfo);
            return result;
        } catch (Exception e) {
            log.error("failed to find userTradeInfo default where user id={},cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.not.found");
            return result;
        }
    }

    @Override
    public Response<List<UserTradeInfo>> findTradeInfoByUserAndDistrict(Long userId, Integer districtId) {

        Response<List<UserTradeInfo>> result = new Response<List<UserTradeInfo>>();
        if (userId == null) {
            log.error("userTradeInfo's userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        if (districtId == null) {
            log.error("userTradeInfo's districtId can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<UserTradeInfo> userTradeInfoList = userTradeInfoDao.findTradeInfoByUserAndDistrict(userId,districtId);
            result.setResult(userTradeInfoList);
            return result;
        } catch (Exception e) {
            log.error("failed to find userTradeInfo default where user id={},cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("userTradeInfo.not.found");
            return result;
        }
    }
}
