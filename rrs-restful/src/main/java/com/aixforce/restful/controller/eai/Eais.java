package com.aixforce.restful.controller.eai;

import com.aixforce.common.model.Response;
import com.aixforce.restful.event.OuterCodeEvent;
import com.aixforce.restful.event.RestEventBus;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.user.util.UserVerification.isAdmin;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-04 4:46 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/eai")
public class Eais {

    @Autowired
    private ShopService shopService;

    @Autowired
    private RestEventBus restEventBus;

    @Autowired
    private MessageSources messageSources;

    /**
     * 根据税号更新商家8码
     *
     * @param taxNo         税务登记号
     * @param outerCode     商户88吗
     * @return  操作是否成功
     */
    @RequestMapping(value = "/code/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> updateOuterCode(@RequestParam(value = "taxNo") String taxNo,
                                             @RequestParam(value = "outerCode") String outerCode) {

        Response<Boolean> result = new Response<Boolean>();

        try {
            BaseUser user = UserUtil.getCurrentUser();
            checkArgument(isAdmin(user), "user.has.no.permission");


            checkArgument(notEmpty(outerCode) && outerCode.length() == 10, "outer.code.not.valid");
            checkArgument(notEmpty(taxNo) && taxNo.length() <=20, "tax.no.not.valid");

            Response<List<Shop>> updateResult = shopService.batchUpdateOuterCodeWithTaxNo(taxNo, outerCode);
            checkState(updateResult.isSuccess(), updateResult.getResult());

            // 获取更新成功的店铺列表
            List<Shop> shops = updateResult.getResult();

            // 异步去更新所有的outerCode
            restEventBus.post(new OuterCodeEvent(shops, outerCode));

        } catch (IllegalArgumentException e) {
            log.error("fail to update outerCode with taxNo:{}, outerCode:{}, error:{}",
                    taxNo, outerCode, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to update outerCode with taxNo:{}, outerCode:{}, error:{}",
                    taxNo, outerCode, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to update outerCode with taxNo:{}, outerCode:{}, cause:{}",
                    taxNo, outerCode, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("outer.code.update.fail"));
        }

        return result;
    }
}
