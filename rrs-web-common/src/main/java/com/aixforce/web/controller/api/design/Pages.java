package com.aixforce.web.controller.api.design;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.model.*;
import com.aixforce.site.service.PageService;
import com.aixforce.site.service.SiteInstanceService;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 13-12-2
 */
@Controller
@RequestMapping("/api/design/pages")
public class Pages {
    @Autowired
    private PageService pageService;
    @Autowired
    private SiteService siteService;
    @Autowired
    private SiteInstanceService siteInstanceService;

    /**
     * 新建一个页面，只有子站可以随意新建页面
     *
     * @param instanceId 站点实例id
     * @param name       名字
     * @param path       路径
     * @param keywords SEO 关键字
     * @param description SEO 描述信息
     * @return id
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(@RequestParam Long instanceId,
                       @RequestParam String name,
                       @RequestParam String path,
                       @RequestParam(required = false) String keywords,
                       @RequestParam(required = false) String description) {
        path = path.toLowerCase();
        TemplateInstance templateInstance = authCheck(instanceId);
        Response<List<? extends Page>> pagesR = pageService.findPages(templateInstance.getSiteId(), templateInstance.getId());
        Server500Exception.failToThrow(pagesR);
        for (Page existedPage : pagesR.getResult()) {
            if (existedPage.getPath().equals(path)) {
                throw new JsonResponseException(400, "路径已经存在，不能重复");
            }
        }
        TemplatePage page = new TemplatePage();
        page.setInstanceId(instanceId);
        page.setPageCategory(PageCategory.OTHER);
        page.setName(name);
        page.setPath(path);
        page.setKeywords(keywords);
        page.setDescription(description);
        Response<Long> idr = pageService.createSubSitePage(page);
        Server500Exception.failToThrow(idr);
        return idr.getResult();
    }

    @RequestMapping(value = "/set-index", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void setIndex(@RequestParam Long instanceId, @RequestParam String path) {
        TemplateInstance templateInstance = authCheck(instanceId);
        templateInstance.setIndexPath(path);
        Response<Long> r = siteInstanceService.updateTemplateInstance(templateInstance);
        Server500Exception.failToThrow(r);
    }

    /**
     * 修改子站的页面信息，子站页面可以修改名字路径以及关键字和描述
     *
     * @param id   页面id
     * @param name 名字
     * @param path 路径
     * @param keywords SEO 关键字
     * @param description SEO 描述信息
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(@PathVariable Long id,
                       @RequestParam String name,
                       @RequestParam String path,
                       @RequestParam(required = false) String keywords,
                       @RequestParam(required = false) String description) {
        // always lower case
        path = path.toLowerCase();
        Response<TemplatePage> pr = pageService.findTemplatePageById(id);
        Server500Exception.failToThrow(pr);
        TemplatePage page = pr.getResult();
        TemplateInstance templateInstance = authCheck(page.getInstanceId());
        Response<List<? extends Page>> pagesR = pageService.findPages(templateInstance.getSiteId(), templateInstance.getId());
        Server500Exception.failToThrow(pagesR);
        if (!page.getPath().equals(path)) {
            for (Page existedPage : pagesR.getResult()) {
                if (existedPage.getPath().equals(path)) {
                    throw new JsonResponseException(400, "路径已经存在，不能重复");
                }
            }
        }
        page.setName(name);
        page.setPath(path);
        page.setKeywords(keywords);
        page.setDescription(description);
        Response<Long> updateR = pageService.updateSubSitePage(page);
        Server500Exception.failToThrow(updateR);
    }

    /**
     * 修改店铺的页面信息，店铺页面只可以修改关键字和描述
     *
     * @param id   页面id
     * @param keywords SEO 关键字
     * @param description SEO 描述信息
     */
    @RequestMapping(value = "/shop/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateSitePage(@PathVariable Long id,
                               @RequestParam(required = false) String keywords,
                               @RequestParam(required = false) String description) {
        Response<Page> pr = pageService.findPageById(id);
        Server500Exception.failToThrow(pr);
        Page page = pr.getResult();
        Response<SiteInstance> sir = siteInstanceService.findSiteInstanceById(page.getInstanceId());
        Server500Exception.failToThrow(sir);
        Response<Site> sr = siteService.findById(sir.getResult().getSiteId());
        Server500Exception.failToThrow(sr);
        if (!sr.getResult().getUserId().equals(UserUtil.getUserId())) {
            throw new JsonResponseException(403, "site isnt belong to u");
        }
        page.setKeywords(keywords);
        page.setDescription(description);
        Response<Long> updateR = pageService.updatePage(page);
        Server500Exception.failToThrow(updateR);
    }

    /**
     * 删除一个页面，只有子站的页面可以删除
     *
     * @param id 页面id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void delete(@PathVariable Long id) {
        Response<TemplatePage> pr = pageService.findTemplatePageById(id);
        Server500Exception.failToThrow(pr);
        TemplatePage page = pr.getResult();
        if (page == null) {
            throw new JsonResponseException(400, "page not found");
        }
        authCheck(page.getInstanceId());

        Response<Boolean> deleteR = pageService.deleteSubSitePage(id);
        Server500Exception.failToThrow(deleteR);
    }

    private TemplateInstance authCheck(Long instanceId) {
        Response<TemplateInstance> sir = siteInstanceService.findTemplateInstanceById(instanceId);
        Server500Exception.failToThrow(sir);
        Response<Site> sr = siteService.findById(sir.getResult().getSiteId());
        Server500Exception.failToThrow(sr);
        Site site = sr.getResult();
        // only official site can set index
        if (site.getCategory() != SiteCategory.OFFICIAL) {
            throw new JsonResponseException(500, "category illegal");
        }
        BaseUser.TYPE currentUserType = UserUtil.getCurrentUser().getTypeEnum();
        if (currentUserType != BaseUser.TYPE.ADMIN && currentUserType != BaseUser.TYPE.SITE_OWNER) {
            throw new JsonResponseException(403, "only admin or site_owner can control site pages");
        }
        return sir.getResult();
    }
}
