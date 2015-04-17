package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.dto.FreightModelDto;
import com.aixforce.trade.model.FreightModel;
import com.aixforce.user.base.BaseUser;

import java.util.List;
import java.util.Map;

/**
 * Desc:运费模板服务接口
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-22.
 */
public interface FreightModelService {
    /**
     * 通过输入的模板对象创建模板信息内容
     * @param freightModelDto   模板对象
     * @return Boolean
     * 返回创建是否成功
     */
    public Response<Boolean> createModel(@ParamInfo("freightModelDto")FreightModelDto freightModelDto);

    /**
     * 判断运费模板是否已存在
     * @param sellerId  卖家编号
     * @param modelName 模板名称
     * @param modelId   模板编号（用于更新是判断参数,创建时可为空）
     * @return Boolean
     * 返回是否已存在
     */
    public Response<Boolean> existModel(@ParamInfo("sellerId")Long sellerId, @ParamInfo("modelName")String modelName, @ParamInfo("modelId")Long modelId);

    /**
     * 更新模板信息
     * @param freightModelDto   模板对象
     * @return Boolean
     * 返回更新是否成功
     */
    public Response<Boolean> updateModel(FreightModelDto freightModelDto, Long userId);

    /**
     * 逻辑删除运费模板
     * @param freightModelId 运费模板id
     * @param userId 当前操作用户id
     * @return 删除是否成功
     */
    public Response<Boolean> deleteModel(Long freightModelId, Long userId);

    /**
     * 删除特殊区域信息
     * @param specialId   特殊区域编号
     * @return Boolean
     * 返回更新是否成功
     */
    public Response<Boolean> deleteLogisticsSpecial(Long specialId, Long userId);

    /**
     * 通过模板编号查询详细信息
     * @param modelId   模板编号
     * @return  FreightModelDto
     * 返回模板详细信息
     */
    public Response<FreightModelDto> findById(@ParamInfo("modelId")Long modelId);

    /**
     * 查询系统的默认运费模版信息
     * @return FreightModel
     * 返回默认的运费模版信息（当未找到运费模版时创建一个新的运费模版）
     */
    public Response<FreightModel> findDefaultModel();

    /**
     * 通过商家编号查询全部模板信息
     * @param sellerId  商家编号
     * @return  List
     * 返回模板信息列表
     */
    public Response<List<FreightModel>> findBySellerId(@ParamInfo("sellerId")Long sellerId);

    /**
     * 商家通过删选条件查询模板信息
     * @param pageNo        查询页码，（default 1）
     * @param size          每页返回的数据条数
     * @param params    查询参数(modelName:模糊查找, countWay:计价方式, costWay:费用方式->这些参数都是可选的)
     * @param seller    获取商家对象（这个是渲染引擎中获取的）
     * @return  List
     * 返回模板信息列表
     */
    public Response<Paging<FreightModel>> findByParams(@ParamInfo("pageNo")Integer pageNo, @ParamInfo("size")Integer size,
                                                     @ParamInfo("params")Map<String , Object> params, @ParamInfo("seller") BaseUser seller);
}
