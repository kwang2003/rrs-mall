package com.aixforce.admin.web;

import com.aixforce.site.exception.NotFound404Exception;
import com.aixforce.web.controller.view.ViewRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-12
 */
@Controller
public class DefaultView {

    @Autowired
    private ViewRender viewRender;

    /**
     * 管理后台页面直接走layout渲染
     *
     * @param request  http request
     * @param response http response
     * @param context  上下文
     * @return html content
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public void path(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/") || path.startsWith("/design/")) {
            throw new NotFound404Exception();
        }
        // remove first "/"
        viewRender.layoutView(path.substring(1), request, response, context);
    }
}
