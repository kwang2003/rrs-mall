package com.rrs.arrivegift.service;

import com.aixforce.common.model.Response;
import com.rrs.arrivegift.model.Experince;

/**
 * Created by zhum01 on 2014/10/24.
 */
public interface ExperinceService {
    public Response<Experince> queryExperinceByMap(Long shopId);
}
