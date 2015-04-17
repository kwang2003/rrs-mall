package com.aixforce.web;

import com.aixforce.site.handlebars.HandlebarEngine;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.github.kevinsawicki.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.math.BigDecimal;
import java.net.URLEncoder;

import static com.aixforce.common.utils.Arguments.isNull;


/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-6-3
 */
@Component
public class MallHandlebarsHelper {
    @Autowired
    private HandlebarEngine handlebarEngine;

    @PostConstruct
    private void init() {
        handlebarEngine.registerHelper("urlEncode", new Helper<Object>() {
            @Override
            public CharSequence apply(Object param, Options options) throws IOException {
                String charset = options.param(0, "utf-8");
                return URLEncoder.encode(param.toString(), charset);
            }
        });

        handlebarEngine.registerHelper("shtml", new Helper<Object>() {
            @Override
            public CharSequence apply(Object param, Options options) throws IOException {
                String src = null;
                if (param!=null && param instanceof String && !((String) param).isEmpty()) {
                    src = (String) param;
                }
                String body = "";
                if (src!=null && !src.isEmpty() && src.startsWith("http")) {
                    try {
                        if (options.params.length>0 && isSafeUrl((String)options.params[0])) {
                            src += options.params[0];
                        }
                        HttpRequest request = HttpRequest.get(new URL(src));
                        if (request.ok())
                            body = request.body();
                    } catch (Throwable t) {
                        // Ignore possible error on purpose.
                    }
                }
                return new Handlebars.SafeString(body);
            }

            private boolean isSafeUrl(String url) {
                // TODO always return true for test only.
                return true;
            }
        });

        handlebarEngine.registerHelper("divide", new Helper<Integer>() {
            @Override
            public CharSequence apply(Integer dividend, Options options) throws IOException {
                if (isNull(dividend) || isNull(options.param(0))) {
                    return dividend == null ? "" : dividend.toString();
                }

                Integer divisor = options.param(0, 1);
                if (divisor == 0) {
                    divisor = 1;
                }

                BigDecimal a = new BigDecimal(dividend);
                BigDecimal b = new BigDecimal(divisor);
                return a.divide(b).toString() ;
            }
        });
        handlebarEngine.registerHelper("yzhdTag", new Helper<String>() {
            private Joiner joiner = Joiner.on(";").skipNulls();

            @Override
            public CharSequence apply(String tags, Options options) throws IOException {
                if (Strings.isNullOrEmpty(tags)) {
                    return "";
                }
                return joiner.join(tags.split("/"));
            }
        });
    }

}
