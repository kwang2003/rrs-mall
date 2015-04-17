/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.category.model.RichAttribute;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.dto.RichSpu;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemDetail;
import com.aixforce.item.model.ItemWithTags;
import com.aixforce.item.model.Sku;
import com.aixforce.search.Pair;
import com.aixforce.user.base.BaseUser;

import java.util.List;
import java.util.Map;

public interface ItemService {
    //系统默认的包邮运费
    public final static Long defaultModelId = -1l;

    //系统默认运费模板名称
    public final static String defaultModelName = "默认包邮模板";

    /**
     * 创建商品,商品的图片信息,以及sku信息
     *
     * @param item       商品
     * @param itemDetail 商品的图片信息
     * @param skus       sku信息
     * @param needValidate 是否需要将价格和模板商品价格进行校验，普通商品需要，预售不需要
     * @return 新创建商品的id
     */
    Response<Long> create(Item item, ItemDetail itemDetail, List<Sku> skus, Boolean needValidate);

    /**
     * 更新商品,其中itemDetail商品体重和商品体积这2个字段允许更新为null
     *
     * @param item       商品
     * @param itemDetail 商品的图片信息
     * @param skus       sku信息
     * @return 是否更新成功
     */
    Response<Boolean> update(Item item, ItemDetail itemDetail, List<Sku> skus);

    /**
     * 减少库存
     *
     * @param skuId    sku id
     * @param itemId   item id
     * @param quantity 数量
     */
    Response<Boolean> decrementStock(Long skuId, Long itemId, Integer quantity);

    /**
     * 因为交易或者退货引起的库存和销量的变化
     *
     * @param skuId    sku id
     * @param itemId   商品id
     * @param quantity 变化量,对于卖出商品为负值,对于退货则为正值
     */
    Response<Boolean> changeSoldQuantityAndStock(Long skuId, Long itemId, Integer quantity);

    /**
     * 增加库存
     *
     * @param skuId    sku id
     * @param itemId   item id
     * @param quantity sku id
     */
    Response<Boolean> incrementStock(Long skuId, Long itemId, Integer quantity);

    /**
     * 批量更新商品状态,这个接口给卖家后台用
     *
     * @param userId 用户id
     * @param status 状态
     * @param ids    商品id列表
     * @return 是否更新成功
     */
    Response<Boolean> bulkUpdateStatus(Long userId, Integer status, List<Long> ids);

    /**
     * 根据id查找商品
     *
     * @param id 商品id
     * @return 商品
     */
    Response<Item> findById(@ParamInfo("id") Long id);

    /**
     * 根据id列表批量查找商品
     *
     * @param ids id列表
     * @return 商品列表
     */
    Response<List<Item>> findByIds(@ParamInfo("ids") List<Long> ids);

    /**
     * 如果商品未售出,则物理删除商品及相关信息,如果商品已经有了交易,则逻辑删除
     *
     * @param userId 用户id
     * @param itemId 商品id
     * @return 是否删除成功
     */
    Response<Boolean> delete(Long userId, Long itemId);

    /**
     * 批量删除商品，如果商品未售出,则物理删除商品及相关信息,如果商品已经有了交易,则逻辑删除
     *
     * @param userId  用户id
     * @param itemIds 商品id
     * @return 是否批量删除成功
     */
    Response<Boolean> bulkDelete(Long userId, List<Long> itemIds);

    /**
     * 查找商品的其他信息
     *
     * @param itemId 商品id
     * @return 商品的其他图片信息
     */
    Response<ItemDetail> findDetailBy(@ParamInfo("itemId") Long itemId);

    /**
     * 查找商品的全部信息,包括商品,图片,sku,属性等
     *
     * @param itemId 商品id
     * @return 商品的全部信息和模版商品信息
     */
    Response<Map<String, Object>> findWithDetailsById(@ParamInfo("itemId") Long itemId);

    /**
     * 根据skuId查找sku
     *
     * @param skuId sku id
     * @return SKU信息
     */
    Response<Sku> findSkuById(@ParamInfo("skuId") Long skuId);

    /**
     * 根据商品id查找商品的属性列表
     *
     * @param itemId 商品id
     * @return 属性列表
     */
    Response<List<RichAttribute>> attributesOf(@ParamInfo("itemId") Long itemId);


    /**
     * 卖家后台商品列表,用于管理店铺内商品，这个接口是被商家后台用的
     *
     * @param baseUser 系统注入的用户
     * @param pageNo   起始页码
     * @param size     返回条数
     * @param params   搜索参数
     * @return 商品列表
     */
    Response<Paging<Item>> sellerItems(@ParamInfo("baseUser") BaseUser baseUser, @ParamInfo("pageNo") Integer pageNo,
                                       @ParamInfo("size") Integer size, @ParamInfo("params") Map<String, String> params);


    /**
     * 根据tag分页查找归属指定tag的商品信息,商品包含tag信息,如果tag为空，根据params查找
     *
     * @param user   卖家
     * @param tag    tag名称
     * @param pageNo 起始页码,从1开始
     * @param size   每页显示条数
     * @return 归属指定tag的商品信息, 商品包含tag信息
     */
    Response<Paging<ItemWithTags>> findItemsOfTag(@ParamInfo("baseUser") final BaseUser user,
                                                  @ParamInfo("tag") final String tag,
                                                  @ParamInfo("pageNo") Integer pageNo,
                                                  @ParamInfo("size") Integer size,
                                                  Map<String, String> params);
    public Response<Paging<ItemWithTags>> findItemsOfTagCoupons(BaseUser user,
                                                                String tag,
                                                                Integer pageNo,
                                                                Integer size,
                                                                Map<String, String> params) ;


    /**
     * 批量更新商品状态
     *
     * @param ids    id列表
     * @param status 商品状态
     * @return 操作是否成功
     */
    Response<Boolean> updateStatusByIds(List<Long> ids, Integer status);

    /**
     * 冻结多个商家，冻结商家所有商品，解冻多个商家
     */
    Response<Boolean> updateStatusBySellerIds(Iterable<Long> sellerIds, Integer status);

    /**
     * 商品查询，可根据sellerId和itemId，这个接口被后台运营用
     *
     * @param pageNo 页号
     * @param param  每页条数
     * @return 商品列表
     */
    Response<Paging<Item>> find(@ParamInfo("pageNo") Integer pageNo,
                                @ParamInfo("params") Map<String, String> param,
                                @ParamInfo("size") Integer size);

    /**
     * 查找未分类商品
     *
     * @param baseUser 登录用户
     * @param pageNo   页号
     * @param size     每页大小
     * @return item列表
     */
    Response<Paging<Item>> findUnclassifiedItems(@ParamInfo("baseUser") BaseUser baseUser,
                                                 @ParamInfo("pageNo") Integer pageNo,
                                                 @ParamInfo("size") Integer size);

    /**
     * 根据spuId查询商品数量
     * @param spuId spuId
     * @return      该spu下商品数量
     */
    Response<Integer> countBySpuId(Long spuId);

    /**
     * 查询一个店铺上架商品数量
     * @param shopId 店铺id
     * @return 商品数量
     */
    Response<Long> countOnShelfByShopId(Long shopId);

    /**
     * 根据spuId查询商品,只在该spu下只有一个商品时才调用该方法
     * @param spuId spuId
     * @return      spuId 对应唯一商品
     */
    Response<List<Item>> findBySpuId(Long spuId);

    Response<List<Item>> findOnShelfBySpuId(Long spuId);

    /**
     * 根据商品id查找spu和sku属性
     * @param id  商品id
     * @return    查询结果
     */
    Response<RichSpu> findRichSpuByItemId(@ParamInfo("itemId")Long id);

    /**
     * 根据商品id查找面包屑
     * @param id 商品id
     * @return   面包屑
     */
    Response<List<Pair>> findBreadCrumbsByItemId(@ParamInfo("itemId")Long id);

    /**
     * 通过运费模板编号查询全部绑定该模板的商品信息(By Michael Zhao)
     * @param modelId   模板编号
     * @return  List
     * 返回商品信息
     */
    Response<List<Item>> findByModelId(@ParamInfo("modelId")Long modelId);

    /**
     * 将商品与运费模板相互绑定(By Michael Zhao)
     * @param item    商品对象（传递绑定数据使用）
     * @return  Boolean
     * 返回绑定是否成功
     */
    public Response<Boolean> bindFreightModel(@ParamInfo("item")Item item);

    /********************************* for HaierService ******************************/
    /**
     * 根据sellerId, spuId查询商品，平台代运营的店铺在一个spu下只有一个商品
     * @param sellerId 商家id
     * @param spuId  spuId
     * @return   查询结果
     */
    Response<Item> findBySellerIdAndSpuId(Long sellerId, Long spuId);

    /**
     * 根据商品id，2个sku属性id查找唯一一个sku
     * @param itemId 商品id
     * @param attributeValue1 sku属性id1
     * @param attributeValue2 sku属性id2
     * @return  sku
     */
    Response<Sku> findSkuByAttributeValuesAndItemId(Long itemId, String attributeValue1, String attributeValue2);

    /**
     * 更新商品和商品图片信息,创建sku
     * @param item  商品
     * @param itemDetail 商品图片
     * @param sku sku
     * @return  是否成功
     */
    Response<Boolean> updateItemAndItemDetailAndCreateSku(Item item, ItemDetail itemDetail, Sku sku);

    /**
     * 根据sku id 列表获取库存列表
     *
     * @param skuIds 库存id列表
     * @return  库存列表
     */
    Response<List<Sku>> findSkuByIds(List<Long> skuIds);

    /**
     * 根据店铺id查找商品
     * @param shopId 店铺id
     * @return 商品列表
     */
    Response<List<Item>> findByShopId(Long shopId);

    /**
     * 根据sellerId找商品
     * @param sellerId 商家id
     * @return 商品列表
     */
    Response<List<Item>> findBySellerId(Long sellerId);

    /**
     * 根据商品id找sku
     */
    Response<List<Sku>> findSkusByItemId(Long itemId);

    /**
     * 批量修改商品的区域信息
     * @param itemIds 商品ids
     * @param region  地区信息
     * @return 是否修改成功
     */
    Response<Boolean> batchUpdateItemRegion(List<Long> itemIds, String region);

    /**************************** item count service ************************/

    /**
     * 移除店铺宝贝数
     * @param shopIds 店铺id列表
     * @return 操作结果
     */
    Response<Boolean> removeItemCountByShopIds(List<Long> shopIds);

    /**
     * 保存店铺宝贝数
     * @param shopId 店铺id
     * @param count  店铺宝贝数
     * @return 操作结果
     */
    Response<Boolean> setItemCountByShopId(Long shopId, Long count);

    /**
     * 商品最大id
     */
    Response<Long> maxIdByShopId(Long shopId);

    /**
     * 分页查找商品，防止调用超时
     */
    Response<List<Item>> findPagingItemByShopId(Long lastId, Long shopId, Integer limit);

    Response<Boolean> batchUpdateItemRegions(List<Item> items);

    /**
     * 更新商品信息
     */
    Response<Boolean> updateItem(Item item);

    Response<Boolean> updateSkus(List<Sku> skus);
}
