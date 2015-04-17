package com.aixforce.web.controller.view;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.site.container.RenderConstants;
import com.aixforce.site.exception.NotFound404Exception;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.exception.UnAuthorize401Exception;
import com.aixforce.site.model.*;
import com.aixforce.site.service.PageService;
import com.aixforce.site.service.SiteInstanceService;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 13-11-28
 */
@Controller
@RequestMapping("/design")
@Slf4j
public class Design {
    @Autowired
    private ViewRender viewRender;
    @Autowired
    private SiteService siteService;
    @Autowired
    private SiteInstanceService siteInstanceService;
    @Autowired
    private CommonConstants commonConstants;
    @Autowired
    private PageService pageService;
    @Value("#{app.editorLayout}")
    private String editorLayout;

    @RequestMapping(value = "/templates/{siteId}", method = RequestMethod.GET)
    public String designTemplate(@PathVariable Long siteId, Map<String, Object> context) {
        Map<String, Object> editorContext = Maps.newHashMap();
        Response<Site> sr = siteService.findById(siteId);
        Server500Exception.failToThrow(sr);
        Site site = sr.getResult();
        if (site == null) {
            log.error("can not find site when edit template, siteId: {}", siteId);
            throw new NotFound404Exception("未找到对应的站点");
        }
        BaseUser.TYPE currentUserType = UserUtil.getCurrentUser().getTypeEnum();
        if (site.getCategory() == SiteCategory.OFFICIAL) { // 子站只有管理员和运营可以编辑
            if (currentUserType != BaseUser.TYPE.ADMIN && currentUserType != BaseUser.TYPE.SITE_OWNER) {
                log.error("only admin and site_owner can design sub site, currentUser: {}", UserUtil.getCurrentUser().getId());
                throw new UnAuthorize401Exception("管理员和运营才能编辑子站");
            }
        }
        if (site.getCategory() == SiteCategory.TEMPLATE) { // 模板只有管理员可以编辑
            if (currentUserType != BaseUser.TYPE.ADMIN) {
                log.error("only admin can design sub template, currentUser: {}", UserUtil.getCurrentUser().getId());
                throw new UnAuthorize401Exception("管理员才能编辑模板");
            }
        }
        editorContext.put("site", site);
        editorContext.put("mode", site.getCategory());
        Response<TemplateInstance> tir = siteInstanceService.findTemplateInstanceById(site.getDesignInstanceId());
        Server500Exception.failToThrow(tir);
        TemplateInstance templateInstance = tir.getResult();
        if (templateInstance == null) {
            log.error("can not find site instance when edit template, instanceId: {}", site.getDesignInstanceId());
            throw new NotFound404Exception("Can't find template instance.");
        }
        // clean unnecessary data
        templateInstance.setHeader(null);
        templateInstance.setFooter(null);
        editorContext.put("instance", templateInstance);
        Response<List<? extends Page>> pr = pageService.findPages(siteId, templateInstance.getId());
        Server500Exception.failToThrow(pr);
        List<? extends Page> pages = pr.getResult();
        if (pages != null && !pages.isEmpty()) {
            Map<String, Page> pageMap = Maps.newHashMap();
            for (Page page : pages) {
                // clean unnecessary data
                page.setParts(null);
                page.setJsonParts(null);
                if (page instanceof TemplatePage) {
                    ((TemplatePage) page).setFixed(null);
                }
                pageMap.put(page.getPath(), page);
            }
            editorContext.put("pages", pageMap);
            Page indexPage = pageMap.get(Strings.isNullOrEmpty(templateInstance.getIndexPath()) ? "index" : templateInstance.getIndexPath());
            editorContext.put("indexPage", indexPage);
            editorContext.put("currentPage", Objects.firstNonNull(indexPage, pages.get(0)));
        }
        context.put("editorContext", editorContext);
        context.put("title", site.getCategory() == SiteCategory.OFFICIAL ? "子站编辑" : "模板编辑");
        return "views/" + editorLayout;
    }

    @RequestMapping(value = "/sites/{templateId}", method = RequestMethod.GET)
    public String designSite(@PathVariable Long templateId, Map<String, Object> context) {
        BaseUser user = UserUtil.getCurrentUser();
        Map<String, Object> editorContext = Maps.newHashMap();
        Response<Site> sr = siteService.findShopByUserId(user.getId());
        if(!sr.isSuccess()){
            log.error("failed to find shop site for user(id={}),error code:{}",user.getId(),sr.getError());
            throw new NotFound404Exception(sr.getError());
        }
        Site site = sr.getResult();
        editorContext.put("site", site);
        editorContext.put("sellerId", user.getId());
        editorContext.put("mode", site.getCategory());
        Response<Long> useR = siteService.useTemplate(site.getId(), templateId);
        if(!useR.isSuccess()){
            log.error("failed to use template(id={}) for user(id={}),error code:{}",templateId,user.getId(),useR.getError());
            throw new Server500Exception("failed to use template");
        }
        Long siteInstanceId = useR.getResult();
        Response<SiteInstance> si = siteInstanceService.findSiteInstanceById(siteInstanceId);
        if(!si.isSuccess()){
            log.error("failed to find site instance(id={}),error code:{}",
                    siteInstanceId,user.getId(),si.getError());
            throw new NotFound404Exception(si.getError());
        }
        SiteInstance siteInstance = si.getResult();

        // clean unnecessary data
        editorContext.put("instance", siteInstance);
        Response<List<? extends Page>> pr = pageService.findPages(site.getId(), siteInstance.getId());
        Server500Exception.failToThrow(pr);
        List<? extends Page> pages = pr.getResult();
        if (pages != null && !pages.isEmpty()) {
            Map<String, Page> pageMap = Maps.newHashMap();
            for (Page page : pages) {
                // clean unnecessary data
                page.setParts(null);
                page.setJsonParts(null);
                pageMap.put(page.getPath(), page);
            }
            editorContext.put("pages", pageMap);
            Page indexPage = pageMap.get("index");
            editorContext.put("indexPage", indexPage);
            editorContext.put("currentPage", indexPage);
        }
        context.put("editorContext", editorContext);
        context.put("title", "店铺编辑");
        return "views/" + editorLayout;
    }

    @RequestMapping(value = "/pages", method = RequestMethod.GET)
    public void designPage(@RequestHeader("Host") String domain,
                           @RequestParam(required = false) Long instanceId, @RequestParam String path, @RequestParam boolean isSite,
                           HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> context) {
        context.put(RenderConstants.DESIGN_MODE, true);
        if (instanceId == null) {
            domain = domain.split(":")[0];
            String subDomain = domain.substring(0, domain.length() - commonConstants.getDomain().length() - 1);
            Response<Site> siteR = siteService.findBySubdomain(subDomain);
            Server500Exception.failToThrow(siteR, "find site by subDomain error", subDomain);
            Site site = siteR.getResult();
            if (site == null) {
                throw new NotFound404Exception("site not found for subDomain [" + subDomain + "]");
            }
            viewRender.viewTemplate(site.getReleaseInstanceId(), path, request, response, context);
            return;
        }
        if (isSite) {
            // shop component need sellerId
            context.put("sellerId", UserUtil.getCurrentUser().getId());
            viewRender.viewSite(instanceId, path, request, response, true, context);
        } else {
            viewRender.viewTemplate(instanceId, path, request, response, context);
        }
    }
}
