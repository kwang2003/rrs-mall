package com.aixforce.rrs.popularizeurl.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.buying.dto.BuyingActivityOrderDto;
import com.aixforce.rrs.buying.model.BuyingOrderRecord;

import javax.annotation.Nullable;

/**
 * Created by 王猛 on 14-9-23
 */
public interface PopularizeUrlService {

    /**
     *
     * @param popUrl 推广url
     * @return 跳转url
     */
    Response<String> getUrl(String popUrl);

    /**
     * 创建推广链接
     * @param popUrl 推广url
     * @param url
     */
    Response<Boolean> createPopUrl(String popUrl, String url);
}
