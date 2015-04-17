package com.aixforce.restful.dto;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.restful.util.Signatures;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-17 12:52 PM  <br>
 * Author: xiao
 */
public class HaierResponse<T> extends Response<T>{
    private static final long serialVersionUID = -7692668224294848588L;

    public enum SignType {
        sign, dont_sign, smart
    }

    @Getter
    private String sign;

    @Getter
    private String msg;

    @Getter
    private String sessionId;

    @JsonIgnore
    private SignType isSign = SignType.smart;

    private static final JsonMapper mapper = JsonMapper.nonEmptyMapper();

    public <T extends Response> void setError(MessageSources ms, T resp) {
        super.setError(ms.get(resp.getError()));
    }

    public void sign(String key){
        Object obj = this.getResult();

        if (obj == null) {
            throw new NullPointerException("result can no be null");
        }

        if (Objects.equal(isSign, SignType.smart)) {
            if (!(obj instanceof Paging)) {  // 非Paging对象无需签名（暂定）
                return;
            }
        } else if (Objects.equal(isSign, SignType.sign)) {
            // do nothing
        } else if (Objects.equal(isSign, SignType.dont_sign)) {
            return;
        }

        String toVerify = mapper.toJson(obj) ;
        this.sign = Signatures.sign(toVerify + key, 1);
    }

    public void setResult(T result, String signKey) {
        if (!Strings.isNullOrEmpty(signKey)) this.isSign = SignType.sign;
        super.setResult(result);
        sign(signKey);
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setSessionId(HttpServletRequest request ) {
        sessionId = request.getSession().getId();
    }
    public void setSessionId(String sessionId ) {
        this.sessionId = sessionId;
    }

}
