package com.aixforce.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.site.exception.NotFound404Exception;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.exception.UnAuthorize401Exception;
import com.aixforce.site.model.Page;
import com.aixforce.site.model.PageCategory;
import com.aixforce.site.model.Site;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.User;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 13-12-16
 */
@Controller
@RequestMapping("/design")
@Slf4j
public class ItemDesign {
    @Value("#{app.editorLayout}")
    private String editorLayout;
    @Autowired
    private ItemService itemService;
    @Autowired
    private SiteService siteService;

    @RequestMapping(value = "/items/{itemId}", method = RequestMethod.GET)
    public String designItem(@PathVariable Long itemId, Map<String, Object> context) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Item> itemR = itemService.findById(itemId);
        Server500Exception.failToThrow(itemR);
        Item item = itemR.getResult();
        if (!Objects.equal(user.getId(), item.getUserId())) {
            throw new JsonResponseException(403, "item not belong to u");
        }

        Map<String, Object> editorContext = Maps.newHashMap();
        editorContext.put("mode", "ITEM");
        editorContext.put("noPage", true);
        editorContext.put("itemId", itemId);
        editorContext.put("spuId", item.getSpuId());

        Response<Site> sr = siteService.findShopByUserId(user.getId());
        if(!sr.isSuccess()){
            log.error("failed to find shop site for user(id={}),error code:{}",user.getId(),sr.getError());
            throw new NotFound404Exception("shop site not found");
        }
        Site site = sr.getResult();

        if (site.getReleaseInstanceId() == null) {
            throw new NotFound404Exception("Shop site don't release yet.");
        }
        editorContext.put("site", site);
        Page fakeDetailPage = new Page();
        fakeDetailPage.setInstanceId(site.getReleaseInstanceId());
        fakeDetailPage.setPageCategory(PageCategory.DETAIL);
        fakeDetailPage.setName(PageCategory.DETAIL.getName());
        fakeDetailPage.setPath(PageCategory.DETAIL.getPath());
        editorContext.put("currentPage", fakeDetailPage);

        context.put("editorContext", editorContext);
        context.put("title", "商品详情编辑");
        return "views/" + editorLayout;
    }

    @RequestMapping(value = "/pre-items/{itemId}", method = RequestMethod.GET)
    public String designPreItem(@PathVariable Long itemId,
                                @RequestParam(required = false) String url,
                                Map<String, Object> context,
                                HttpServletRequest request) {
        BaseUser user = UserUtil.getCurrentUser();
        if (user.getTypeEnum() != User.TYPE.ADMIN) {
            if (isAjaxRequest(request)) {
                throw new JsonResponseException(401, "u are not admin");
            } else {
                throw new UnAuthorize401Exception("u are not admin");
            }
        }
        Response<Item> itemR = itemService.findById(itemId);
        Server500Exception.failToThrow(itemR);
        Item item = itemR.getResult();

        Map<String, Object> editorContext = Maps.newHashMap();
        editorContext.put("mode", "ITEM");
        editorContext.put("noPage", true);
        editorContext.put("itemId", itemId);
        editorContext.put("spuId", item.getSpuId());

        editorContext.put("pageUrl", Objects.firstNonNull(url, "pre-items"));

        context.put("editorContext", editorContext);
        context.put("title", "商品详情编辑");
        return "views/" + editorLayout;
    }

    @RequestMapping(value = "/item-templates/{spuId}", method = RequestMethod.GET)
    public String designTemplate(@PathVariable Long spuId, @RequestParam(required = false) String url, Map<String, Object> context) {
        // TODO 暂时只查5个 不考虑更多
        Response<Paging<Site>> siteR = siteService.pagination(1, 1, 5);
        Server500Exception.failToThrow(siteR);
        if (siteR.getResult().getData() == null || siteR.getResult().getData().isEmpty()) {
            throw new Server500Exception("no default template found");
        }
        // 找出第一个release了的
        Site site = null;
        for (Site tSite : siteR.getResult().getData()) {
            if (tSite.getReleaseInstanceId() != null) {
                site = tSite;
                break;
            }
        }
        if (site == null) {
            throw new Server500Exception("No released template found");
        }
        Map<String, Object> editorContext = Maps.newHashMap();
        editorContext.put("mode", "ITEM_TEMPLATE");
        editorContext.put("noPage", true);
        editorContext.put("spuId", spuId);
        editorContext.put("site", site);
        Page fakeDetailPage = new Page();
        fakeDetailPage.setInstanceId(site.getReleaseInstanceId());
        fakeDetailPage.setPageCategory(PageCategory.DETAIL);
        fakeDetailPage.setName(PageCategory.DETAIL.getName());
        fakeDetailPage.setPath(PageCategory.DETAIL.getPath());
        editorContext.put("currentPage", fakeDetailPage);

        context.put("editorContext", editorContext);
        context.put("title", "模板商品详情编辑");
        return "views/" + editorLayout;
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return Objects.equal(request.getHeader(HttpHeaders.X_REQUESTED_WITH), "XMLHttpRequest");
    }
}
