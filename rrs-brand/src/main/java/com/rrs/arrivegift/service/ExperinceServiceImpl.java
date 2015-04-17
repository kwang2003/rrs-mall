package com.rrs.arrivegift.service;

import com.aixforce.common.model.Response;
import com.rrs.arrivegift.dao.ExperinceDao;
import com.rrs.arrivegift.model.Experince;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by zhum01 on 2014/10/24.
 */
@Service
public class ExperinceServiceImpl implements ExperinceService{
    @Autowired
    private ExperinceDao experinceDao;

    @Override
    public Response<Experince> queryExperinceByMap(Long shopId) {
        Response<Experince> result = new Response<Experince>();
        Experince experince =  experinceDao.queryExperinceByMap(shopId);
        result.setResult(experince);
        return result;
    }
}
