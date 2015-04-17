package com.aixforce.shop.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.shop.dto.RichShop;
import com.aixforce.shop.dto.ShopDto;
import com.aixforce.shop.dto.ShopInfoDto;
import com.aixforce.shop.dto.ShopSidebar;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.model.ShopPaperwork;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-28
 */
@SuppressWarnings("unused")
public interface ShopService {




    /**
     * 查询指定状态的店铺
     *
     * @param statuses      状态集合
     * @param pageNo        分页
     * @param size          每页数据量
     * @return 用户分页信息
     */
    Response<Paging<Shop>> findBy(List<Integer> statuses, Integer pageNo, Integer size);

    /**
     * 获取品牌馆认领店铺列表
     * @param params
     * @param pageNo
     * @param size
     * @return
     */
    Response<Paging<ShopDto>> findShopsByBrand(@ParamInfo("params") Map<String, String> params,
                                               @ParamInfo("pageNo") Integer pageNo,
                                               @ParamInfo("size") Integer size);


    /**
     * 根据店铺id查找店铺
     *
     * @param shopId 店铺id
     * @return 查找结果
     */
    Response<Shop> findById(@Nonnull Long shopId);

    /**
     * 根据卖家id查找店铺
     *
     * @param userId 卖家id
     * @return 查找结果
     */
    Response<Shop> findByUserId(@Nonnull @ParamInfo("sellerId") Long userId);

    /**
     * 根据卖家查找店铺,这个接口是用来配组件的
     *
     * @param user 卖家
     * @return 查找结果（包括证件信息）
     */
    Response<ShopInfoDto> findByUser(@ParamInfo("baseUser") BaseUser user);

    /**
     * 根据店铺名查找店铺
     *
     * @param name 店铺名
     * @return 查找结果
     */
    Response<Shop> findByName(@Nonnull String name);

    /**
     * 创建店铺
     *
     * @param shop 待创建的店铺
     * @param shopPaperwork 店铺证件
     * @return 设置了id的shop
     */
    Response<Shop> create(@Nonnull Shop shop, @Nonnull ShopPaperwork shopPaperwork);


     /**
     * 更新除状态和用户之外的店铺信息
     *
     * @param shop 店铺
     * @return 是否更新成功
     */
    Response<Boolean> update(@Nonnull Shop shop);

    /**
     * 更新店铺（这个是为通过审批的情况下更改申请的店铺信息）
     *
     * @param shop 待创建的店铺
     * @param shopPaperwork 店铺证件
     * @return Boolean
     * 返回更改是否成功
     */
    Response<Boolean> update(@Nonnull Shop shop, @Nonnull ShopPaperwork shopPaperwork);

    /**
     * 物理删除店铺对象,运营使用
     * @param id 店铺id
     * @return   是否删除成功
     */
    Response<Boolean> delete(Long id);

    /**
     * 更新店铺状态,这个接口是给运营用的
     *
     * @param shopId 店铺id
     * @param status 目标状态
     * @return 是否更新成功
     */
    Response<Boolean> updateStatus(@Nonnull Long shopId, @Nonnull Shop.Status status);

    /**
     * 全量dump搜索引擎
     */
    Response<Boolean> fullDump();


    /**
     * 增量dump搜索引擎
     *
     * @param intervalInMinutes 间隔时间,以分钟计算
     */
    void deltaDump(int intervalInMinutes);


    /**
     * 批量修改店铺审核状态
     *
     * @param ids       店铺ids
     * @param status    店铺状态
     * @return  执行是否成功
     */
    public Response<Boolean> approved(String ids, Integer status);

    /**
     * 批量冻结店铺
     *
     * @param ids    店铺ids
     * @param status 店铺状态，frozen或ok
     */
    Response<Boolean> updateStatusByIds(String ids, Integer status);

    /**
     * 根据shopId或者userId查询店铺
     *
     * @param params 参数
     * @return shopDto
     */
    Response<Paging<ShopDto>> find(@ParamInfo("params") Map<String, String> params,
                                   @ParamInfo("pageNo") Integer pageNo,
                                   @ParamInfo("size") Integer size);

    /**
     * 返回店铺侧边栏信息,该接口买家能调用
     *
     * @param sellerId 店主id
     * @return 店铺侧边栏信息
     */
    Response<ShopSidebar> findShopSideBar(@ParamInfo("sellerId") Long sellerId);

    /**
     * 获取店铺宝贝数
     * @param shopId 店铺id
     * @return 店铺宝贝数
     */
    Response<Long> getItemCountByShopId(Long shopId);

    /**
     * 全网店铺搜索
     *
     * @param pageNo 起始页码
     * @param size   返回条数
     * @param params 搜索参数
     * @return 搜索结果
     */
    Response<Paging<RichShop>> searchShop(@ParamInfo("pageNo") int pageNo,
                                                  @ParamInfo("size") int size,
                                                  @ParamInfo("params") Map<String, String> params);

    /**
     * 明星店铺,内部调用search shop方法
     * @return 返回一个销售额最高店铺，一个销量最高店铺
     */
    Response<Map<String, RichShop>> starShop(@ParamInfo("businessId") Long businessId);

    /**
     * 增加店铺销售额，买家确认收货之后调用
     * @param sellerId 卖家id
     * @param sale     销售额
     * @return         操作结果
     */
    Response<Boolean> incrShopSalesBySellerId(Long sellerId, Long sale);

    /**
     * 增加店铺卖出宝贝件数，买家确认收货之后调用
     * @param sellerId  卖家id
     * @param soldQuantity   卖出宝贝数
     * @return          操作结果
     */
    Response<Boolean> incrShopSoldQuantityBySellerId(Long sellerId, Long soldQuantity);

    /**
     * 设置一个店铺的额外信息，不存在时会自动创建记录
     * @param extra 额外信息
     * @return extra记录的id
     */
    Response<Long> setExtra(ShopExtra extra);

    /**
     * 更新店铺的扩展信息
     * @param extra 扩展信息信息
     * @return   操作结果
     */
    Response<Boolean> updateExtra(ShopExtra extra);


    /**
     * 获取店铺的额外信息
     *
     * @param sellerId 卖家id
     * @return  店铺的额外信息
     */
    Response<ShopExtra> getExtra(Long sellerId);

    /**
     * 根据outCode查找shop
     * @param outerCode 店铺外部编码
     * @return shop
     */
    Response<List<Shop>> findByOuterCode(String outerCode);

    /**
     * 增量更新店铺的评分,分值进行累加
     *
     * @param extras     每家店铺新增评论的（四个维度的）总分列表
     * @return              操作是否成功
     */
    Response<List<Long>> bulkUpdateShopExtraScore(List<ShopExtra> extras);

    /**
     * 增量更新店铺的评分,分值进行累加
     *
     * @param extras     店铺新增评论的（四个维度的）总分列表
     * @return              操作是否成功
     */
    Response<Boolean> updateShopExtraScore(ShopExtra extras);

    /**
     * 全量更新shop_extras 表中店铺评分的信息。分值不进行累加
     * @param extra shop_extra列表
     */
    Response<Boolean> fullUpdateShopExtraScore(ShopExtra extra);


    /**
     * 根据店铺id查询店铺证件
     * @param shopId 店铺id
     * @return 店铺证件
     */
    Response<ShopPaperwork>  findByShopId(@ParamInfo("shopId") Long shopId);


    /**
     * 查询店铺扩展信息
     * @param pageNo    页码
     * @param size      返回条数
     * @return  分页信息
     */
    Response<Paging<ShopExtra>> findExtraBy(Integer pageNo, Integer size);

    /**
     * 获取全量统计用的id列表
     *
     * @param lastId    上次处理的最大id
     * @param limit     每次处理的记录数量
     * @return          shop id 的列表
     */
    Response<List<Shop>> forDump(Long lastId, Integer limit);

    /**
     * shop 记录的最大 id
     *
     * @return  最大的id
     */
    Response<Long> maxId();

    /**
     * 批量根据税务登记号更新
     *
     * @param taxNo         税务登记号
     * @param outerCode     商户88码
     * @return  被更新的店铺信息
     */
    Response<List<Shop>> batchUpdateOuterCodeWithTaxNo(String taxNo, String outerCode);


    /**
     * 查询税务等级号不为空的shopExtra
     *
     * @param pageNo        页码
     * @param pageSize      每页大小
     * @return  shopExtra分页信息
     */
    Response<Paging<Shop>> findWithTaxNo(Integer pageNo, Integer pageSize);



}
