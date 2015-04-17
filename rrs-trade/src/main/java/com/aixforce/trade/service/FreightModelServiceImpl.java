package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.dao.FreightModelDao;
import com.aixforce.trade.dao.LogisticsSpecialDao;
import com.aixforce.trade.dto.FreightModelDto;
import com.aixforce.trade.manager.FreightModelManager;
import com.aixforce.trade.model.FreightModel;
import com.aixforce.trade.model.LogisticsSpecial;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Desc:运费模板信息处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-22.
 */
@Slf4j
@Service
public class FreightModelServiceImpl implements FreightModelService {

    private final FreightModelManager freightModelManager;

    private final FreightModelDao freightModelDao;

    private final LogisticsSpecialDao logisticsSpecialDao;

    //系统默认的运费模版信息
    private final FreightModel defaultModel;

    @Autowired
    public FreightModelServiceImpl(FreightModelManager freightModelManager , FreightModelDao freightModelDao,
                                   LogisticsSpecialDao logisticsSpecialDao){
        this.freightModelManager = freightModelManager;
        this.freightModelDao = freightModelDao;
        this.logisticsSpecialDao = logisticsSpecialDao;
        this.defaultModel = createDefaultModel();
    }

    @Override
    public Response<Boolean> createModel(FreightModelDto freightModelDto) {
        Response<Boolean> result = new Response<Boolean>();

        //模板名称是否已存在
        Response<Boolean> existRes = existModel(freightModelDto.getSellerId() , freightModelDto.getModelName(), freightModelDto.getId());
        if(!existRes.isSuccess()){
            result.setError(existRes.getError());
            return result;
        }else{
            if(existRes.getResult()){
               //模板名称已存在
                log.error("freight model name existed.");
                result.setError("freight.model.name.existed");
                return result;
            }
        }

        //是否是卖家承担运费
        if(FreightModel.CostWay.from(freightModelDto.getCostWay()) == null){
            log.error("create freight model needs costWay.");
            result.setError("freight.model.no.costWay");
            return result;
        }

        try{
            //运费模板默认是启用的
            freightModelDto.setStatus(FreightModel.Status.ENABLED.value());

            if(Objects.equal(FreightModel.CostWay.from(freightModelDto.getCostWay()) , FreightModel.CostWay.BEAR_SELLER)){
                //卖家承担
                freightModelManager.createModel(freightModelDto);
            }else{
                //验证所有数据是否合法,只有非包邮模板才需要验证
                if(!validateFreightNum(freightModelDto)) {
                    log.error("freight model {} params illegal", freightModelDto);
                    result.setError("create.freight.param.illegal");
                    return result;
                }

                freightModelManager.createModel(freightModelDto);
            }
            result.setSuccess(true);
        }catch(Exception e){
            log.error("create freight model failed , error code={}", e);
            result.setError("freight.model.create.failed");
        }

        return result;
    }

    private boolean validateFreightNum(FreightModelDto freightModelDto) {

        //验证firstAmount， addAmount
        if(freightModelDto.getFirstAmount() == null) {
            return false;
        }
        if(freightModelDto.getFirstAmount() < 0) {
            return false;
        }
        if(freightModelDto.getAddAmount() == null) {
            return false;
        }
        if(freightModelDto.getAddAmount() <= 0) {
            return false;
        }

        //验证默认运费价格,不能小于0
        if(freightModelDto.getFirstFee() == null) {
            return false;
        }
        if(freightModelDto.getFirstFee() < 0) {
            return false;
        }
        if(freightModelDto.getAddFee() == null) {
            return false;
        }
        if(freightModelDto.getAddFee() < 0) {
            return false;
        }

        if(freightModelDto.getLogisticsSpecialList() != null && !freightModelDto.getLogisticsSpecialList().isEmpty()) {
            for(LogisticsSpecial logisticsSpecial : freightModelDto.getLogisticsSpecialList()) {
                if(logisticsSpecial.getFirstAmount() == null) {
                    return false;
                }
                if(logisticsSpecial.getFirstAmount() < 0) {
                    return false;
                }
                if(logisticsSpecial.getAddAmount() == null) {
                    return false;
                }
                if(logisticsSpecial.getAddAmount() <= 0) {
                    return false;
                }
                //验证特殊区域价格,不能小于0
                if(logisticsSpecial.getFirstFee() == null) {
                    return false;
                }
                if(logisticsSpecial.getFirstFee() < 0) {
                    return false;
                }
                if(logisticsSpecial.getAddFee()  == null) {
                    return false;
                }
                if(logisticsSpecial.getAddFee() < 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Response<Boolean> existModel(Long sellerId, String modelName, Long modelId) {
        Response<Boolean> result = new Response<Boolean>();

        //模板名称
        if(Strings.isNullOrEmpty(modelName)){
            log.error("check freight model needs model name.");
            result.setError("freight.model.name.null");
            return result;
        }

        //商家编号
        if(sellerId == null){
            log.error("check freight model needs sellerId.");
            result.setError("freight.model.sellerId.null");
            return result;
        }

        try{
            result.setResult(freightModelDao.existModel(sellerId, modelName, modelId) != null);
        }catch(Exception e){
            log.error("find freight model failed, sellerId={} , modelName={}, error code={}" , sellerId, modelName, e);
            result.setError("freight.model.find.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> updateModel(FreightModelDto freightModelDto, Long userId) {
        Response<Boolean> result = new Response<Boolean>();

        //是否是卖家承担运费
        if(FreightModel.CostWay.from(freightModelDto.getCostWay()) == null){
            log.error("update freight model needs costWay.");
            result.setError("freight.model.no.costWay");
            return result;
        }

        //模板名称是否已存在
        Response<Boolean> existRes = existModel(userId , freightModelDto.getModelName(), freightModelDto.getId());
        if(!existRes.isSuccess()){
            result.setError(existRes.getError());
            return result;
        }else{
            if(existRes.getResult()){
                //模板名称已存在
                log.error("freight model name existed.");
                result.setError("freight.model.name.existed");
                return result;
            }
        }

        try{

            FreightModel existModel = freightModelDao.findById(freightModelDto.getId());
            if(existModel == null) {
                log.error("fail to find freight model by id={}", freightModelDto.getId());
                result.setError("freight.model.not.found");
                return result;
            }
            if(!Objects.equal(userId, existModel.getSellerId())) {
                log.error("authorize fail, current user id is {}, model seller id is {}", userId, existModel.getSellerId());
                result.setError("authorize.fail");
                return result;
            }
            if(Objects.equal(FreightModel.CostWay.from(freightModelDto.getCostWay()) , FreightModel.CostWay.BEAR_SELLER)){
                //卖家承担
                freightModelManager.updateModel(freightModelDto);
            }else{
                //验证所有价格是否合法
                if(!validateFreightNum(freightModelDto)) {
                    log.error("freight model {} params illegal", freightModelDto);
                    result.setError("update.freight.param.illegal");
                    return result;
                }

                freightModelManager.updateModel(freightModelDto);
            }
            result.setSuccess(true);
        }catch(Exception e){
            log.error("update freight model failed , error code={}", e);
            result.setError("freight.model.update.failed");
        }

        return result;
    }

    @Override
    public Response<Boolean> deleteModel(Long freightModelId, Long userId) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            FreightModel exist = freightModelDao.findById(freightModelId);

            if(exist == null) {
                log.error("fail to find freight model by id={}", freightModelId);
                result.setError("freight.model.not.found");
                return result;
            }

            //如果不是当前商家的运费模板，不允许删除
            if(!Objects.equal(exist.getSellerId(), userId)) {
                log.error("freight model id={}, sellerId is {}, current user id is {}, authorize fail",
                        freightModelId, exist.getSellerId(), userId);
                result.setError("authorize.fail");
                return result;
            }

            //逻辑删除运费模板，已经发布的商品还是按原有的运费模板算运费
            FreightModel toUpdate = new FreightModel();
            toUpdate.setId(exist.getId());
            toUpdate.setStatus(FreightModel.Status.DISABLED.value());
            freightModelDao.update(toUpdate);

            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to logic delete model id={}, current userId={}, cause:{}",
                    freightModelId, userId, Throwables.getStackTraceAsString(e));
            result.setError("freight.model.logic.delete.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> deleteLogisticsSpecial(Long specialId, Long userId) {
        Response<Boolean> result = new Response<Boolean>();

        if(specialId == null){
            log.error("delete logistics special info need specialId");
            result.setError("freight.model.specialId.null");
            return result;
        }

        try{
            LogisticsSpecial exist = logisticsSpecialDao.findById(specialId);
            if(exist == null) {
                log.error("fail to find LogisticsSpecial by id={}", specialId);
                result.setError("logisticsSpecial.not.found");
                return result;
            }
            Long freightModelId = exist.getModelId();

            FreightModel existModel = freightModelDao.findById(freightModelId);
            if(existModel == null) {
                log.error("fail to find freight model by id={}", freightModelId);
                result.setError("freight.model.not.found");
                return result;
            }
            if(!Objects.equal(userId, existModel.getSellerId())) {
                log.error("authorize fail, current user id is {}, model seller id is {}", userId, existModel.getSellerId());
                result.setError("authorize.fail");
                return result;
            }

            result.setResult(logisticsSpecialDao.delete(specialId));

            //如果删除后运费模板没有特殊区域信息了，更新运费模板
            List<LogisticsSpecial> list = logisticsSpecialDao.findByModelId(freightModelId);
            if(list == null || list.isEmpty()) {
                FreightModel updated = new FreightModel();
                updated.setId(freightModelId);
                updated.setSpecialExist(0);
                freightModelDao.update(updated);
            }

        }catch(Exception e){
            log.error("delete logistics special failed, specialId={}, error code={}", specialId, e);
            result.setError("freight.special.delete.failed");
        }

        return result;
    }

    @Override
    public Response<FreightModelDto> findById(Long modelId) {
        Response<FreightModelDto> result = new Response<FreightModelDto>();

        if(modelId == null){
            log.error("find freight model need modelId");
            result.setError("freight.model.modelId.null");
            return result;
        }

        try{
            FreightModelDto freightModelDto = transformModel(freightModelDao.findById(modelId));

            //存在特殊区域信息
            if(freightModelDto.getSpecialExist() == 1){
                freightModelDto.setLogisticsSpecialList(logisticsSpecialDao.findByModelId(modelId));
            }
            result.setResult(freightModelDto);
        }catch(Exception e){
            log.error("find freight model failed, modelId={}, error code={}", modelId, e);
            result.setError("freight.model.find.failed");
        }

        return result;
    }

    @Override
    public Response<FreightModel> findDefaultModel() {
        Response<FreightModel> result = new Response<FreightModel>();

        //当默认的运费模板为空时查询一个默认的运费模板
        if(defaultModel == null){
            result.setResult(createDefaultModel());
        }else{
            result.setResult(defaultModel);
        }

        return result;
    }

    @Override
    public Response<List<FreightModel>> findBySellerId(Long sellerId) {
        Response<List<FreightModel>> result = new Response<List<FreightModel>>();

        if(sellerId == null){
            log.error("find freight models needs sellerId.");
            result.setError("freight.model.sellerId.null");
            return result;
        }

        try{
            result.setResult(freightModelDao.findBySellerId(sellerId));
        }catch(Exception e){
            log.error("find freight models failed, sellerId={}, error code={}" , sellerId, e);
            result.setError("freight.model.find.failed");
        }
        return result;
    }

    @Override
    public Response<Paging<FreightModel>> findByParams(Integer pageNo, Integer size, Map<String, Object> params, @ParamInfo("seller") BaseUser seller) {
        Response<Paging<FreightModel>> result = new Response<Paging<FreightModel>>();

        if(seller == null){
            log.error("find freight models needs sellerId.");
            result.setError("freight.model.sellerId.null");
            return result;
        }

        try{
            Map<String , Object> findParams = Maps.newHashMap();
            findParams.putAll(addPagingParam(pageNo , size));
            findParams.putAll(params);

            //这个显示运费模板链表信息是低频操作（所以直接查询,不使用redis回写什么的）
            Paging<FreightModel> freightModels = freightModelDao.findByParams(seller.getId(), findParams);
            // 这里前台显示根本没用到 为毛要？
//            for(FreightModel freightModel : freightModels){
//                freightModel.setBindItemNum(itemService.findByModelId(freightModel.getId()).getResult().size());
//            }

            result.setResult(freightModels);
        }catch(Exception e){
            log.error("find freight models failed, pageNo={}, size={}, params={}, error code={}" , pageNo, size, params, e);
            result.setError("freight.model.find.failed");
        }
        return result;
    }

    /**
     * 添加默认的分页实现逻辑
     * @param pageNo    当前页面编号
     * @param size      分页的页数
     * @return Map
     * 返回分页逻辑参数
     */
    private Map<String , Object> addPagingParam(Integer pageNo, Integer size){
        Map<String , Object> params = Maps.newHashMap();

        pageNo = Objects.firstNonNull(pageNo, 1);
        size = Objects.firstNonNull(size, 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;

        params.put("size" , size);
        params.put("offset" , offset);
        return params;
    }

    /**
     * 验证特殊区域信息填写是否正确
     * @param logisticsSpecialList  特殊区域信息链表
     * @return  Boolean
     * 返回特殊区域信息验证是否通过
     */
    private Response<Boolean> proveLogistics(List<LogisticsSpecial> logisticsSpecialList){
        Response<Boolean> result = new Response<Boolean>();

        Date time = DateTime.now().toDate();
        if(logisticsSpecialList != null){
            for(LogisticsSpecial logisticsSpecial : logisticsSpecialList){
                if(logisticsSpecial.getFirstAmount() == null){
                    log.error("create logistics needs firstAmount.");
                    result.setError("freight.model.special.null");
                    return result;
                }

                if(logisticsSpecial.getFirstFee() == null){
                    log.error("create logistics needs firstFee.");
                    result.setError("freight.model.special.null");
                    return result;
                }

                if(logisticsSpecial.getAddAmount() == null){
                    log.error("create logistics needs addAmount.");
                    result.setError("freight.model.special.null");
                    return result;
                }

                if(logisticsSpecial.getAddFee() == null){
                    log.error("create logistics needs addFee.");
                    result.setError("freight.model.special.null");
                    return result;
                }

                logisticsSpecial.setCreatedAt(time);
                logisticsSpecial.setUpdatedAt(time);
            }
        }

        result.setSuccess(true);
        return result;
    }

    /**
     * 验证模板信息填写是否正确
     * @param freightModel  模板信息
     * @return  Boolean
     * 返回信息验证是否通过
     */
    private Response<Boolean> proveFreight(FreightModel freightModel){
        Response<Boolean> result = new Response<Boolean>();
        if(freightModel.getFirstAmount() == null){
            log.error("create freight model needs firstAmount.");
            result.setError("freight.model.default.null");
            return result;
        }

        if(freightModel.getFirstFee() == null){
            log.error("create freight model needs firstFee.");
            result.setError("freight.model.default.null");
            return result;
        }

        if(freightModel.getAddAmount() == null){
            log.error("create freight model needs addAmount.");
            result.setError("freight.model.default.null");
            return result;
        }

        if(freightModel.getAddFee() == null){
            log.error("create freight model needs addFee.");
            result.setError("freight.model.default.null");
            return result;
        }

        result.setSuccess(true);
        return result;
    }

    /**
     * 将FreightModel对象转换成FreightModelDto对象
     * @param freightModel  模板对象
     * @return FreightModelDto
     * 返回封装对象
     */
    private FreightModelDto transformModel(FreightModel freightModel){
        FreightModelDto freightModelDto = new FreightModelDto();
        freightModelDto.setId(freightModel.getId());
        freightModelDto.setCostWay(freightModel.getCostWay());
        freightModelDto.setCountWay(freightModel.getCountWay());
        freightModelDto.setModelName(freightModel.getModelName());
        freightModelDto.setSellerId(freightModel.getSellerId());
        freightModelDto.setSpecialExist(freightModel.getSpecialExist());
        freightModelDto.setFirstAmount(freightModel.getFirstAmount());
        freightModelDto.setFirstFee(freightModel.getFirstFee());
        freightModelDto.setAddAmount(freightModel.getAddAmount());
        freightModelDto.setAddFee(freightModel.getAddFee());
        freightModelDto.setCreatedAt(freightModel.getCreatedAt());
        freightModelDto.setUpdatedAt(freightModel.getUpdatedAt());

        return freightModelDto;
    }

    /**
     * 查询默认的运费模版是否存在不存在就创建一个
     * @return FreightModel
     * 返回一个默认的运费模板（现在先不记录数据库）
     */
    private FreightModel createDefaultModel(){
        FreightModel defaultModel;

        List<FreightModel> freightModels = freightModelDao.findBySellerId(0l);
        if(freightModels.isEmpty()){
            //创建一个新的默认的运费模板
            FreightModel freightModel = new FreightModel();
            freightModel.setSellerId(0l);
            freightModel.setModelName("默认包邮模板");
            freightModel.setCountWay(1);
            freightModel.setCostWay(2);
            freightModel.setCreatedAt(DateTime.now().toDate());
            freightModel.setUpdatedAt(DateTime.now().toDate());

            freightModel.setId(freightModelDao.create(freightModel));
            defaultModel = freightModel;
        }else{
            defaultModel = freightModels.get(0);
        }

        return defaultModel;
    }
}
