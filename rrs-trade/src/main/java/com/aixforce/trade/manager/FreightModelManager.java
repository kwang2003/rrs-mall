package com.aixforce.trade.manager;

import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.model.ItemDetail;
import com.aixforce.trade.dao.FreightModelDao;
import com.aixforce.trade.dao.LogisticsSpecialDao;
import com.aixforce.trade.dto.FreightModelDto;
import com.aixforce.trade.model.FreightModel;
import com.aixforce.trade.model.LogisticsSpecial;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Desc:运费模板处理对象
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-22.
 */
@Slf4j
@Component
public class FreightModelManager {
    @Autowired
    private FreightModelDao freightModelDao;

    @Autowired
    private LogisticsSpecialDao logisticsSpecialDao;

    private final LoadingCache<Long , List<LogisticsSpecial>> logisticsCache = CacheBuilder.newBuilder().expireAfterAccess(60 , TimeUnit.SECONDS).build(
        new CacheLoader<Long, List<LogisticsSpecial>>() {
            @Override
            public List<LogisticsSpecial> load(Long modelId) throws Exception {
                //查询优惠券特殊区域设置
                return logisticsSpecialDao.findByModelId(modelId);
            }
        }
    );

    /**
     * 创建运费模板
     * @param freightModelDto   运费模板对象
     * 这里需要添加事务处理机制
     */
    @Transactional
    public void createModel(FreightModelDto freightModelDto){
        if(freightModelDto.getLogisticsSpecialList() == null || freightModelDto.getLogisticsSpecialList().isEmpty()){
            //不存在特殊区域信息
            freightModelDto.setSpecialExist(0);
            freightModelDao.create(freightModelDto);
        }else{
            //存在特殊区域信息
            freightModelDto.setSpecialExist(1);
            Long modelId = freightModelDao.create(freightModelDto);

            //特殊地区绑定modelId
            for(LogisticsSpecial logisticsSpecial : freightModelDto.getLogisticsSpecialList()){
                logisticsSpecial.setModelId(modelId);
            }

            logisticsSpecialDao.createBatch(freightModelDto.getLogisticsSpecialList());
        }
    }

    /**
     * 更新模板对象信息
     * @param freightModelDto   模板对象
     * 这里需要添加事务处理机制
     */
    @Transactional
    public void updateModel(FreightModelDto freightModelDto){
        //不更新状态
        freightModelDto.setStatus(null);
        if(freightModelDto.getLogisticsSpecialList() == null || freightModelDto.getLogisticsSpecialList().isEmpty()){
            //不存在特殊区域信息
            freightModelDto.setSpecialExist(0);
            freightModelDao.update(freightModelDto);
        }else{
            //存在特殊区域信息
            freightModelDto.setSpecialExist(1);
            freightModelDao.update(freightModelDto);

            List<LogisticsSpecial> newList = Lists.newArrayList();

            //新的特殊区域信息
            for(LogisticsSpecial logisticsSpecial : freightModelDto.getLogisticsSpecialList()){
                if(logisticsSpecial.getId() == null){
                    logisticsSpecial.setModelId(freightModelDto.getId());
                    newList.add(logisticsSpecial);
                }else{
                    //更新久的对象信息
                    logisticsSpecialDao.update(logisticsSpecial);
                }
            }

            //创建新的特殊区域信息
            if(!newList.isEmpty()){
                logisticsSpecialDao.createBatch(newList);
            }
        }
    }

    /**
     * 通过运输地址编号&运费模板&商品对象&商品数量计算商品的运费信息
     * @param addressId     运输地址编号
     * @param freightModel  运费模板
     * @param itemNum       商品数量
     * @return Integer
     * 返回运费价格
     */
    public Integer countFee(Integer addressId, FreightModel freightModel, ItemDetail itemDetail, Integer itemNum){
        //商品数量必须大于0
        if(itemNum <= 0){
            log.warn("count freight model fee failed, itemNum={}", itemNum);
            return 0;
        }

        if(freightModel.getSpecialExist() == 1){
            //判断特殊区域信息
            try {
                List<LogisticsSpecial> specialList = logisticsCache.get(freightModel.getId());

                //遍历特殊区域信息
                for(LogisticsSpecial special : specialList){
                    Map<String , String> specialAddress = JsonMapper.nonEmptyMapper().fromJson(
                            special.getAddressModel(), JsonMapper.nonEmptyMapper().createCollectionType(
                                    HashMap.class, String.class, String.class
                            ));

                    //省份信息
                    String provinces = specialAddress.get("p");
                    Iterable<String> parts = Splitter.on(',').omitEmptyStrings().trimResults().split(provinces);

                    //匹配省份信息
                    for(String provinceId : parts){
                        if(provinceId.equals(addressId.toString())){
                            //计算特殊地区运费价格
                            return countBySpecial(freightModel.getCountWay() , special.getFirstAmount(), special.getFirstFee(),
                                    special.getAddAmount(), special.getAddFee(), itemDetail, itemNum);
                        }
                    }
                }
            }catch(Exception e){
                log.error("count freight fee by freight model failed, error code:{}", e);
            }
        }

        //默认运费模板
        return countBySpecial(freightModel.getCountWay() , freightModel.getFirstAmount(), freightModel.getFirstFee(),
                freightModel.getAddAmount(), freightModel.getAddFee(), itemDetail, itemNum);
    }

    /**
     * 通过运费计算方式&运费的计算参数以及商品对象计算运费信息
     * @param countWay      计算方式
     * @param firstAmount   默认大小
     * @param firstFee      默认价格
     * @param addAmount     增量大小
     * @param addFee        增量价格
     * @param itemNum       商品数量
     * @return Integer
     * 返回计算完成的运费价格
     */
    private Integer countBySpecial(Integer countWay , Integer firstAmount, Integer firstFee,
                                   Integer addAmount, Integer addFee, ItemDetail itemDetail, Integer itemNum){
        Integer specialFee = 0;
        switch(FreightModel.CountWay.from(countWay)){
            case NUMBER:{
                if(itemNum > firstAmount){
                    //数量差
                    Integer diffNum = itemNum - firstAmount;

                    //使用向上取整获取需要额外增加的金额
                    Integer addFees = (int)(addFee*Math.ceil((double)diffNum/addAmount));
                    specialFee = firstFee + addFees;
                }else{
                    specialFee = firstFee;
                }
                break;
            }
            case SIZE:{
                if(itemDetail.getFreightSize()*itemNum > firstAmount){
                    //体积超出部分
                    Integer diffSize = itemDetail.getFreightSize()*itemNum - firstAmount;

                    //使用向上取整获取需要额外增加的金额
                    Integer addFees = (int)(addFee*Math.ceil((double)diffSize/addAmount));
                    specialFee = firstFee + addFees;
                }else{
                    specialFee = firstFee;
                }
                break;
            }
            case WEIGHT:{
                if(itemDetail.getFreightWeight()*itemNum > firstAmount){
                    //重量超出部分
                    Integer diffSize = itemDetail.getFreightWeight()*itemNum - firstAmount;

                    //使用向上取整获取需要额外增加的金额
                    Integer addFees = (int)(addFee*Math.ceil((double)diffSize/addAmount));
                    specialFee = firstFee + addFees;
                }else{
                    specialFee = firstFee;
                }
                break;
            }
            default:{
                log.warn("count freight model fee failed, can't find this countWay, countWay={}", countWay);
            }
        }

        return specialFee;
    }
}