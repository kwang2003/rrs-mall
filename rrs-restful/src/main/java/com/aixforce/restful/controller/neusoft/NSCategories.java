package com.aixforce.restful.controller.neusoft;

import com.aixforce.category.dto.RichCategory;
import com.aixforce.category.service.BackCategoryService;
import com.aixforce.common.model.Response;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.Signatures;
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
 * Date: 4/8/14
 * Time: 14:20
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/categories")
public class NSCategories {

    @Autowired
    BackCategoryService backCategoryService;

    @Autowired
    MessageSources messageSources;

    @Value("#{app.restkey}")
    private String key;

    /**
     * @param channel  渠道, 必填
     * @param sign     签名, 必填
     *
     *
     * 返回商场所有商品类目
     */
    @RequestMapping(value = "/all", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<RichCategory>> list(@RequestParam("channel") String channel,
                                                  @RequestParam("sign") String sign,
                                                  HttpServletRequest request) {
        HaierResponse<List<RichCategory>> result = new HaierResponse<List<RichCategory>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Response<List<RichCategory>> categoriesGetResult = backCategoryService.getTreeOf(0);
            checkState(categoriesGetResult.isSuccess(), categoriesGetResult.getError());
            result.setResult(categoriesGetResult.getResult(), key);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to get back-categories, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get back-categories, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get back-categories", e);
            result.setError(messageSources.get("back.category.query.fail"));
        }

        return result;
    }


}
