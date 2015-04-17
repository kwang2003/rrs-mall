package com.aixforce.user.endpoint;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-04-21 3:47 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class HaierUserClient {

    @Value("#{app.validateUrl}")
    private String url;

    private static final int SUCCESS = 0;
    private static final int USER_NOT_EXIST = 1;
    private static final int MOBILE_INVALID = 2;
    private static final int EMAIL_INVALID = 3;
    private static final int PASS_INVALID = 4;



    private static final String CHARSET = "gb2312";
    private static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final Joiner joiner = Joiner.on('&');

    public Response<Boolean> check(String userName, String password) {
        Response<Boolean> result = new Response<Boolean>();


        try {
            Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
            params.put("userName", userName);
            params.put("password", password);

            String body =  HttpRequest.post(url).contentType(HEADER_CONTENT_TYPE, CHARSET)
                    .send(joiner.withKeyValueSeparator("=").join(params)).connectTimeout(1000).readTimeout(1000)
                    .body();
            log.debug("check result body:{}", body);
            checkState(notNull(body));
            @SuppressWarnings("unchecked")
            Map<String, String> res = JsonMapper.nonEmptyMapper().fromJson(body, Map.class);
            checkState(notNull(res), "user.login.fail");
            Integer state = Integer.parseInt(res.get("result"));
            switch (state) {
                case SUCCESS:
                    result.setResult(Boolean.TRUE);
                    break;
                case USER_NOT_EXIST:
                    result.setError("user.not.found");
                    break;
                case MOBILE_INVALID:
                    result.setError("mobile.not.validate");
                    break;
                case EMAIL_INVALID:
                    result.setError("email.not.validate");
                    break;
                case PASS_INVALID:
                    result.setError("user.password.incorrect");
                    break;

                default:
                    throw new IllegalStateException("v1.state.incorrect");
            }

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to check user state of elder rrs.com", e);
            result.setError("user.login.fail");
        }

        return result;
    }


}
