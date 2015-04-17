package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;

/**
 * Desc:运费计算服务接口
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-23.
 */
public interface FreightCountService {
    /**
     * 通过地址编号&商品编号统计运费价格信息
     * @param addressId 省份，市等编号(现在业务需求默认为省份)
     * @param itemId    商品编号
     * @param itemNum   商品数量
     * @return Integer
     * 返回费用
     */
    public Response<Integer> countDefaultFee(@ParamInfo("addressId") Integer addressId, @ParamInfo("itemId") Long itemId,
                                             @ParamInfo("itemNum") Integer itemNum);

    /**
     * 通过物流地址编号&商品编号统计运费价格信息
     * @param userTradeInfoId 省份，市等编号(现在业务需求默认为省份)（当地址为空时获取用户默认的运费地址）
     * @param itemId    商品编号
     * @param itemNum   商品数量
     * @return Integer
     * 返回费用
     */
    public Response<Integer> countDefaultFee(@ParamInfo("userTradeInfoId") Long userTradeInfoId, @ParamInfo("itemId") Long itemId,
                                             @ParamInfo("itemNum") Integer itemNum);

    /**
     * 通过地址编号&订单编号（可以查询到商品绑定的运费模板）计算运费
     * @param addressId 省份，市等编号(现在业务需求默认为省份)
     * @param orderId   订单编号
     * @return Integer
     * 返回费用
     */
    public Response<Integer> countFeeByOrder(@ParamInfo("addressId") Integer addressId, @ParamInfo("orderId") Long orderId);

    /**
     * 通过用户的邮寄地址&订单编号计算运费
     * @param userTradeInfoId 用户邮寄地址编号
     * @param orderId         订单编号
     * @return  Integer
     * 返回费用
     */
    public Response<Integer> countOrderFee(@ParamInfo("userTradeInfoId") Long userTradeInfoId, @ParamInfo("orderId") Long orderId);

    /**
     * 通过地址编号&子订单编号（可以查询到商品绑定的运费模板）计算运费
     * @param addressId     省份，市等编号(现在业务需求默认为省份)
     * @param orderItemId   子订单编号
     * @return  Integer
     * 返回费用
     */
    public Response<Integer> countFeeByOrderItem(@ParamInfo("addressId") Integer addressId, @ParamInfo("orderItemId") Long orderItemId);

    /**
     * 通过用户的邮寄地址&子订单编号计算运费
     * @param userTradeInfoId 用户邮寄地址编号
     * @param orderItemId     子订单编号
     * @return  Integer
     * 返回费用
     */
    public Response<Integer> countOrderItemFee(@ParamInfo("userTradeInfoId") Long userTradeInfoId, @ParamInfo("orderItemId") Long orderItemId);

    /**
     * 通过地址编号&商品统计运费价格信息
     * @param addressId 省份，市等编号(现在业务需求默认为省份)
     * @param item      商品对象
     * @param itemNum   商品数量
     * @return Integer
     * 返回费用
     */
    public Integer countFeeByItem(Integer addressId , Item item, Integer itemNum);
}
