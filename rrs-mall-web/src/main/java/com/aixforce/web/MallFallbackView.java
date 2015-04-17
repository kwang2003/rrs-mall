package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.item.model.TitleKeyword;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.site.exception.NotFound404Exception;
import com.aixforce.site.model.Site;
import com.aixforce.site.model.SiteCategory;
import com.aixforce.site.service.SiteService;
import com.aixforce.web.controller.view.ViewRender;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14/10/15
 */
@Controller
public class MallFallbackView {
    @Autowired
    private ViewRender viewRender;

    @Value("#{app.mainSite}")
    private String mainSite;
    @Value("#{app.domain}")
    private String siteDomain;

    @Autowired
    private SiteService siteService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private MessageSources messageSources;

    private static Set<String> platformDomainPrefix = ImmutableSet.<String>builder()
            .add("member.")
            .add("order.")
            .add("bc.")
            .add("passport.")
            .add("pay.")
            .build();

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public void path(@RequestHeader("Host") String domain, HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/") || path.startsWith("/design/")) {
            throw new NotFound404Exception();
        }

        domain = domain.split(":")[0];

        for (String prefix : platformDomainPrefix) {
            if(domain.startsWith(prefix)){ //如果是属于主站的页面
                domain = mainSite;
                break;
            }
        }

        // find domain for shop
        String subDomain = domain.substring(0, domain.length() - siteDomain.length() - 1);
        Response<Site> siteR = siteService.findBySubdomain(subDomain);
        if (siteR.isSuccess() && siteR.getResult() != null) {
            Site site = siteR.getResult();
            if (site.getCategory() == SiteCategory.SHOP) {
                Response<Shop> shopR = shopService.findByUserId(site.getUserId());
                if (shopR.isSuccess() && shopR.getResult() != null) {
                    context.put("seo", buildSEOInfoForShop(shopR.getResult()));
                }
            }
        }

        // remove first "/"
        viewRender.view(domain, path.substring(1), request, response, context);
    }

    private TitleKeyword buildSEOInfoForShop(Shop shop){
        TitleKeyword titleKeyword = new TitleKeyword();
        String title = Joiner.on("|").skipNulls().join(shop.getName(), shop.getStreet(), shop.getPhone());
        String desc = Joiner.on("、").skipNulls().join(shop.getName(), shop.getStreet(), shop.getPhone());
        titleKeyword.setTitle(messageSources.get("shop.templates.title", title));
        titleKeyword.setKeyword(messageSources.get("shop.templates.keywords", title));
        titleKeyword.setDesc(messageSources.get("shop.templates.description", desc));
        return titleKeyword;
    }
}
