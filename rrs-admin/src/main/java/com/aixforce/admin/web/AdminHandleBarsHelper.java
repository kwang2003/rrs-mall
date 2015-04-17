package com.aixforce.admin.web;

import com.aixforce.site.handlebars.HandlebarEngine;
import com.github.jknack.handlebars.Handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.kevinsawicki.http.HttpRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-6-3
 */
@Component
public class AdminHandleBarsHelper {
    @Autowired
    private HandlebarEngine handlebarEngine;

    @PostConstruct
    private void init() {
        handlebarEngine.registerHelper("urlEncode", new Helper<Object>() {
            @Override
            public CharSequence apply(Object param, Options options) throws IOException {
                String codec = options.param(0, "utf-8");
                return URLEncoder.encode(param.toString(), codec);
            }
        });

        handlebarEngine.registerHelper("shtml", new Helper<Object>() {
            @Override
            public CharSequence apply(Object param, Options options) throws IOException {
                String name = null;
                String src = null;
                if (param!=null && param instanceof String && !((String) param).isEmpty()) {
                    src = (String) param;
                }
                if (options.params.length>0)
                    name = (String)options.params[0];
                String body = "";
                if (src!=null && !src.isEmpty()) {
                    try {
                        HttpRequest request = HttpRequest.get(new URL(src));
                        if (request.ok())
                            body = request.body();
                    } catch (Throwable t) {
                        // Ignore possible error on purpose.
                    }
                }
                if (name==null || name.isEmpty())
                    return new Handlebars.SafeString(body);
                return new Handlebars.SafeString("<h1>Hello, " + name + "</h1>" + body);
            }
        });
    }
}
