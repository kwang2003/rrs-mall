package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.model.Site;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by yangzefeng on 13-12-12
 */
@RequestMapping("/api/admin/sites")
@Controller
@Slf4j
public class Sites {

    @Autowired
    private SiteService siteService;

    @Value("#{app.domain}")
    private String officialDomain;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "/{id}/release", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void release(@PathVariable("id") Long siteId, @RequestParam(value = "instanceId", required = false) Long instanceId) {
        Response<Site> siteR = siteService.findById(siteId);
        Server500Exception.failToThrow(siteR);
        Site site = siteR.getResult();
        if (site == null) {
            log.warn("site not found when release, siteId: {}", siteId);
            throw new JsonResponseException(400, "站点不存在");
        }
        BaseUser user = UserUtil.getCurrentUser();
        BaseUser.TYPE currentUserType = user.getTypeEnum();
        switch (site.getCategory()) {
            case OFFICIAL:
                if (currentUserType != BaseUser.TYPE.ADMIN && currentUserType != BaseUser.TYPE.SITE_OWNER) {
                    log.error("only admin and site_owner can release sub site, currentUser: {}", UserUtil.getCurrentUser().getId());
                    throw new JsonResponseException(401, "管理员和运营才能发布子站");
                }
                break;
            case TEMPLATE:
                if (currentUserType != BaseUser.TYPE.ADMIN) {
                    log.error("only admin can release sub template, currentUser: {}", UserUtil.getCurrentUser().getId());
                    throw new JsonResponseException(401, "管理员才能发布模板");
                }
                break;
            case SHOP:
                if (!Objects.equal(user.getId(), site.getUserId())) {
                    log.warn("user(id={}) isn't the owner of site(id={}) ", user.getId(), siteId);
                    throw new JsonResponseException(403, "站点不属于当前用户");
                }
                break;
        }
        instanceId = Objects.firstNonNull(instanceId, site.getDesignInstanceId());
        Response<Long> releaseR = siteService.release(siteId, instanceId);
        Server500Exception.failToThrow(releaseR);

        log.info("user id={} release site instanceId={}", user.getId(), instanceId);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(@RequestParam String name,
                       @RequestParam(required = false) String domain,
                       @RequestParam(required = false) String subDomain) {
        BaseUser user = UserUtil.getCurrentUser();
        Site site = new Site();
        site.setName(name);
        if (!Strings.isNullOrEmpty(domain)) {
            if(domain.endsWith(officialDomain)) {
                log.error("domain can not end with {}", officialDomain);
                throw new JsonResponseException(500, messageSources.get("domain.error"));
            }
            Response<Site> sr = siteService.findByDomain(domain);
            Server500Exception.failToThrow(sr);
            if (sr.getResult() != null) {
                throw new JsonResponseException(500, messageSources.get("domain.exist"));
            }
            site.setDomain(domain);
        }
        if (!Strings.isNullOrEmpty(subDomain)) {
            if(subDomain.endsWith(officialDomain)) {
                log.error("subDomain can not end with {}",officialDomain);
                throw new JsonResponseException(500, messageSources.get("subDomain.error"));
            }
            Response<Site> sr = siteService.findBySubdomain(subDomain);
            Server500Exception.failToThrow(sr);
            if (sr.getResult() != null) {
                throw new JsonResponseException(500, messageSources.get("subDomain.exist"));
            }
            site.setSubdomain(subDomain);
        }
        Response<Long> createR = siteService.createSubSite(user.getId(), site);
        Server500Exception.failToThrow(createR);

        log.info("user id={} create sub site by name={}, domain={}, subDomain={}",
                user.getId(), name, domain, subDomain);

        return createR.getResult();
    }

    @RequestMapping(value = "/templates", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createTemplate(@RequestParam String name) {
        BaseUser user = UserUtil.getCurrentUser();
        Site site = new Site();
        site.setName(name);
        Response<Long> createR = siteService.createTemplateSite(user.getId(), site);
        Server500Exception.failToThrow(createR);
        log.info("user id={} create template by name={}", user.getId(), name);
        return createR.getResult();
    }

    @RequestMapping(value = "/{siteId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(@PathVariable Long siteId, @RequestParam String name,
                       @RequestParam(required = false) String domain,
                       @RequestParam(required = false) String subDomain) {
        Response<Site> sr = siteService.findById(siteId);
        Server500Exception.failToThrow(sr);
        Site site = sr.getResult();
        if (site == null) {
            throw new JsonResponseException(400, "站点不存在");
        }
        BaseUser user = UserUtil.getCurrentUser();
        BaseUser.TYPE currentUserType = user.getTypeEnum();
        switch (site.getCategory()) {
            case OFFICIAL:
                if (currentUserType != BaseUser.TYPE.ADMIN && currentUserType != BaseUser.TYPE.SITE_OWNER) {
                    log.error("only admin and site_owner can edit sub site, currentUser: {}", UserUtil.getCurrentUser().getId());
                    throw new JsonResponseException(401, "管理员和运营才能修改子站");
                }
                break;
            case TEMPLATE:
                if (currentUserType != BaseUser.TYPE.ADMIN) {
                    log.error("only admin can edit sub template, currentUser: {}", UserUtil.getCurrentUser().getId());
                    throw new JsonResponseException(401, "管理员才能修改模板");
                }
                break;
            case SHOP:
                if (!Objects.equal(user.getId(), site.getUserId())) {
                    throw new JsonResponseException(401, "您只能编辑您自己的站点");
                }
                break;
        }
        site.setName(name);
        if (domain != null) {
            if(domain.endsWith(officialDomain)) {
                log.error("domain can not end with .rrs.cn");
                throw new JsonResponseException(500, "域名"+officialDomain+"错误");
            }
            Response<Site> tsr = siteService.findByDomain(domain);
            Server500Exception.failToThrow(tsr);
            if (tsr.getResult() != null && !tsr.getResult().getId().equals(siteId)) {
                throw new JsonResponseException(400, "域名对应的站点已存在");
            }
            site.setDomain(domain);
        }
        if (subDomain != null) {
            if(subDomain.endsWith(officialDomain)) {
                log.error("subDomain can not end with .rrs.cn");
                throw new JsonResponseException(500, "二级域名"+officialDomain+"错误");
            }
            Response<Site> tsr = siteService.findBySubdomain(subDomain);
            Server500Exception.failToThrow(tsr);
            if (tsr.getResult() != null && !tsr.getResult().getId().equals(siteId)) {
                throw new JsonResponseException(400, "二级域名对应的站点已存在");
            }
            site.setSubdomain(subDomain);
        }
        Response<Boolean> updateR = siteService.update(site);
        Server500Exception.failToThrow(updateR);

        log.info("user id={} update site id={} name={}, domain={}, subDomain={}",
                user.getId(), siteId, name, domain, subDomain);
    }

    @RequestMapping(value = "/{siteId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void delete(@PathVariable Long siteId) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> deleteR = siteService.deleteSite(user.getId(), siteId);
        Server500Exception.failToThrow(deleteR);

        log.info("user id={} delete site id={}", user.getId(), siteId);
    }
}
