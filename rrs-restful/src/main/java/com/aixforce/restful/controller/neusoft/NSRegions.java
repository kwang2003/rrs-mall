package com.aixforce.restful.controller.neusoft;

import com.aixforce.common.model.Response;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.Signatures;
import com.aixforce.user.model.Address;
import com.aixforce.user.service.AddressService;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 3/28/14
 * Time: 10:10
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/region")
public class NSRegions {

    @Autowired
    AddressService addressService;

    @Autowired
    private MessageSources messageSources;


    @Value("#{app.restkey}")
    private String key;

    /**
     * 获取地址列表信息
     *
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     *
     * 获取省市区信息
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<Address>> getAddresses(@RequestParam("channel") String channel,
                                                     @RequestParam("sign") String sign,
                                                     HttpServletRequest request) {
        HaierResponse<List<Address>> result = new HaierResponse<List<Address>>();
        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<List<Address>> addressGet = addressService.getTreeOf(0);
            checkState(addressGet.isSuccess(), addressGet.getError());
            result.setResult(addressGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to query addresses, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query addresses", e);
            result.setError(messageSources.get("address.list.query.fail"));
        }
        return result;
    }
}
