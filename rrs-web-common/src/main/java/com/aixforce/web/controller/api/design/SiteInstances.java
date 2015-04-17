package com.aixforce.web.controller.api.design;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.model.*;
import com.aixforce.site.service.PageService;
import com.aixforce.site.service.SiteInstanceService;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 13-11-29
 */
@Controller
@RequestMapping("/api/design/instances")
public class SiteInstances {
    private final static Logger log = LoggerFactory.getLogger(SiteInstance.class);

    @Autowired
    private SiteService siteService;

    @Autowired
    private SiteInstanceService siteInstanceService;
    @Autowired
    private PageService pageService;

    @Autowired
    private MessageSources messageSources;

    private JsonMapper jsonMapper = JsonMapper.JSON_NON_EMPTY_MAPPER;
    private JavaType gdatasType = jsonMapper.createCollectionType(Map.class, String.class, String.class);

    @RequestMapping(value = "/template/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void saveTemplateInstance(@PathVariable Long id, @RequestParam Long pageId,
                                     @RequestParam String header, @RequestParam String body, @RequestParam String footer,
                                     @RequestParam(required = false) String parts,
                                     @RequestParam(required = false) String gdatas) {
        BaseUser.TYPE currentUserType = UserUtil.getCurrentUser().getTypeEnum();
        if (currentUserType != BaseUser.TYPE.ADMIN && currentUserType != BaseUser.TYPE.SITE_OWNER) {
            log.error("only admin or site_owner can touch template instance, but userId={}", UserUtil.getUserId());
            throw new JsonResponseException(401,"only admin and site_owner can touch template");
        }
        try {
            Response<TemplateInstance> sir = siteInstanceService.findTemplateInstanceById(id);
            Server500Exception.failToThrow(sir);
            sir.getResult().setHeader(header);
            sir.getResult().setFooter(footer);
            Response<TemplatePage> pr = pageService.findTemplatePageById(pageId);
            Server500Exception.failToThrow(pr);
            pr.getResult().setJsonParts(parts);
            pr.getResult().setFixed(body);
            Map<String, String> gdatasMap = null;
            if (!Strings.isNullOrEmpty(gdatas)) {
                gdatasMap = JsonMapper.nonEmptyMapper().fromJson(gdatas, gdatasType);
            }
            Response<Long> saveR = siteInstanceService.saveTemplateInstanceWithPage(sir.getResult(), pr.getResult(), gdatasMap);
            Server500Exception.failToThrow(saveR);
        } catch (Server500Exception e) {
            log.error("failed to save template instance for template(id={}) and page(id={}),error code:{} ",id,pageId,e.getMessage());
            throw new JsonResponseException(500,messageSources.get(e.getMessage()));
        }
    }

    @RequestMapping(value = "/site/{instanceId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void saveSiteInstance(@PathVariable Long instanceId, @RequestParam Long pageId, @RequestParam String parts) {
        if(parts.length()>20480){
            log.error("parts length is {},and long than 20k",parts.length());
            throw new JsonResponseException(500, messageSources.get("content.too.long"));
        }
        Response<SiteInstance> sir = siteInstanceService.findSiteInstanceById(instanceId);
        if(!sir.isSuccess()){
            log.error("failed to find siteInstance(id={}),error code:{}",instanceId, sir.getError());
            throw new JsonResponseException(500,messageSources.get(sir.getError()));
        }
        Response<Site> sr = siteService.findById(sir.getResult().getSiteId());
        if (!sr.isSuccess()) {
            log.error("failed to find site(id={}),error code:{}",sir.getResult().getSiteId(), sr.getError());
            throw new JsonResponseException(500, messageSources.get(sir.getError()));
        }
        Site site = sr.getResult();
        BaseUser user = UserUtil.getCurrentUser();
        if (!Objects.equal(site.getUserId(), user.getId())) {
            log.error("site(id={}) not belong to user(id={})", site.getId(), user.getId());
            throw new JsonResponseException(403, "site not belong to u");
        }

        Response<Page> pr = pageService.findPageById(pageId);
        if(!pr.isSuccess()){
            log.error("failed to find page(id={}),error code:{}",pageId, pr.getError());
            throw new JsonResponseException(500,messageSources.get(pr.getError()));
        }
        pr.getResult().setJsonParts(parts);
        Response<Long> saveR = pageService.updatePage(pr.getResult());
        if(!pr.isSuccess()){
            log.error("failed to save siteInstance(id={}) and page(id={}),error code:{}", instanceId, pageId, saveR.getError());
            throw new JsonResponseException(500,messageSources.get(saveR.getError()));
        }
    }
}