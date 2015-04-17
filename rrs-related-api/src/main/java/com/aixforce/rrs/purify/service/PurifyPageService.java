package com.aixforce.rrs.purify.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.dto.PurifyPageDto;

/**
 * Desc:定制页面访问
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-14.
 */
public interface PurifyPageService {
    /**
     * 通过组件数组查询
     * @param seriesId          系列编号
     * @param assemblyIds       组件编号数组
     * @return PurifyPageDto
     * 返回一个封装好的页面数据
     */
    public Response<PurifyPageDto> findPurifyPageInfo(@ParamInfo("seriesId")Long seriesId, @ParamInfo("assemblyIds")Long[] assemblyIds);
}
