package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.item.dto.FullDefaultItem;
import com.aixforce.item.model.BaseSku;
import com.aixforce.item.model.DefaultItem;

import java.util.List;

/**
 * Created by yangzefeng on 13-12-17
 */
public interface DefaultItemService {

    /**
     * 创建默认商品(模版商品)
     *
     * @param defaultItem 默认商品详情
     * @param skus        所有sku
     * @return 是否创建成功
     */
    Response<Boolean> create(DefaultItem defaultItem, List<BaseSku> skus);

    /**
     * 更新默认商品(模版商品)，这里要求skus的id必须不为空
     *
     * @param defaultItem 默认商品详情
     * @param skus        所有sku
     * @return 是否更新成功
     */
    Response<Boolean> update(DefaultItem defaultItem, List<BaseSku> skus);

    /**
     * 根据spu找已发过的默认商品(模版商品)
     *
     * @param spuId spuId
     * @return defaultItem
     */
    Response<DefaultItem> findDefaultItemBySpuId(Long spuId);

    /**
     * 根据spuId返回默认商品和spu信息
     *
     * @param spuId spuId
     * @return defaultItem和spu
     */
    Response<FullDefaultItem> findRichDefaultItemBySpuId(@ParamInfo("spuId") Long spuId);

    /**
     * 根据spuIds查询模版商品
     * @param ids 第一栏商品ids
     * @return defaultItem列表
     */
    Response<List<DefaultItem>> findBySpuIds(@ParamInfo("spus") String ids);

    /**
     * 根据spuIds查询模版商品
     * @param ids 第一栏商品ids
     * @param rid 区域id
     * @return defaultItem列表
     */
    Response<List<DefaultItem>> findBySpuIdsAndRid(@ParamInfo("spus") String ids,@ParamInfo("rid") Integer rid);

    /**
     * 根据outerId查找defaultItem， 用于haier同步库存
     * @param outerId 来自haier的id
     * @return 模版商品
     */
    Response<DefaultItem> findByOuterId(String outerId);
}
