package com.aixforce.restful.service;

import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.dto.OuterIdDto;

import java.util.List;

/**
 * Created by yangzefeng on 14-1-18
 */
public interface HaierService {

    /**
     * 商品自动发布或者同步库存,如果出现异常，直接跳过
     * @param outerIdDtos 结构为outerIdDto列表的json字串
     * @return 操作结果
     */
    HaierResponse<Boolean> autoReleaseOrUpdateItem(List<OuterIdDto> outerIdDtos);
}
