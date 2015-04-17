package com.aixforce.web.controller.view;

import com.aixforce.common.utils.CommonConstants;
import com.aixforce.site.container.PageRender;
import com.aixforce.site.container.RenderConstants;
import com.aixforce.site.exception.NotFound404Exception;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.exception.UnAuthorize401Exception;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.base.exception.UserNotLoginException;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-09-18
 */
@Component
public class ViewRender {

    private static final Logger log = LoggerFactory.getLogger(ViewRender.class);

    private final static String REQUEST_REGION = "haierRegionId";

    private final static String RID = "rid";

    @Autowired
    private PageRender pageRender;

    @Autowired
    private CommonConstants commonConstants;


    public void view(final String domain, final String path,
                       HttpServletRequest request, HttpServletResponse response,
                       final Map<String, Object> context) {
        prepareContext(request, context);
        Supplier<String> getHtml = new Supplier<String>() {
            @Override
            public String get() {
                return pageRender.render(domain, path, context);
            }
        };
        render(response, getHtml);
    }

    public void layoutView(final String path, HttpServletRequest request, HttpServletResponse response, final Map<String, Object> context) {
        prepareContext(request, context);
        Supplier<String> getHtml = new Supplier<String>() {
            @Override
            public String get() {
                return pageRender.naiveRender(path, context);
            }
        };
        render(response, getHtml);
    }


    public void viewTemplate(final Long instanceId, final String path,
                               HttpServletRequest request, HttpServletResponse response,
                               final Map<String, Object> context) {
        prepareContext(request, context);
        Supplier<String> getHtml = new Supplier<String>() {
            @Override
            public String get() {
                return pageRender.renderTemplate(instanceId, path, context);
            }
        };
        render(response, getHtml);
    }

    public void viewSite(final Long instanceId, final String path,
                           HttpServletRequest request, HttpServletResponse response,
                           final boolean isDesign, final Map<String, Object> context) {
        prepareContext(request, context);
        Supplier<String> getHtml = new Supplier<String>() {
            @Override
            public String get() {
                return pageRender.renderSite(instanceId, path, context, isDesign);
            }
        };
        render(response, getHtml);
    }

    private void prepareContext(HttpServletRequest request, Map<String, Object> context) {
        if (request != null) {
            for (Object name : request.getParameterMap().keySet()) {
                context.put((String) name, request.getParameter((String) name));
            }
            //get regionId from cookies
            Cookie[] cookies = request.getCookies();
            Integer region = null;
            if(cookies != null) {
                for(Cookie cookie : cookies) {
                    if(Objects.equal(cookie.getName(), REQUEST_REGION)) {
                        try {
                            region = Integer.valueOf(cookie.getValue());
                        } catch (NumberFormatException e) {
                            // ignore this
                            log.warn("error region id: {}", cookie.getValue());
                        }
                    }
                }
            }
            if(region != null) {
                context.put(RID, region);
            }
        }
        context.put(RenderConstants.USER, UserUtil.getCurrentUser());
    }

    private void render(HttpServletResponse response, Supplier<String> getHtml) {
        String html = null;
        try {
            html = Objects.firstNonNull(getHtml.get(),"");
        } catch (UserNotLoginException e) {
            try {
                response.sendRedirect("http://" + commonConstants.getMainSite() + "/login");
            } catch (IOException e1) {
                //ignore this fucking exception
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, NotFound404Exception.class);
            Throwables.propagateIfInstanceOf(e, Server500Exception.class);
            Throwables.propagateIfInstanceOf(e, UnAuthorize401Exception.class);
            log.error("render failed, cause:{}", Throwables.getStackTraceAsString(Throwables.getRootCause(e)));
            throw new Server500Exception(e.getMessage(), e);
        }
        try {
            response.setContentType(MediaType.HTML_UTF_8.toString());
            //response.setContentLength(html.getBytes(Charsets.UTF_8).length);
            response.getWriter().write(html);
        } catch (IOException e) {
            // ignore it
        }
    }

}
