package com.aixforce.admin.web.controller;

import com.aixforce.admin.event.AdminEventBus;
import com.aixforce.admin.event.OuterCodeSetEvent;
import com.aixforce.category.model.BackCategory;
import com.aixforce.category.service.BackCategoryService;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Brand;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.rrs.grid.service.ShopAuthorizeInfoService;
import com.aixforce.rrs.popularizeurl.service.PopularizeUrlService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.aixforce.site.exception.NotFound404Exception;
import com.aixforce.site.exception.Server500Exception;
import com.aixforce.site.model.Site;
import com.aixforce.site.service.SiteService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: yangzefeng
 * Date: 13-11-6
 * Time: 下午2:26
 */
@Controller
@RequestMapping("/api/admin/popUrl")
public class PopUrlController {

    @Autowired
    PopularizeUrlService popularizeUrlService;

    /*
    *推广端连接
     */
    @RequestMapping(value = "/createUrl")
    public void getPopularizeUrl(HttpServletRequest request, HttpServletResponse response,
                                 @RequestHeader("Host") String domain,
                                 @RequestParam("popUrlCode") String popUrlCode, @RequestParam("url") String url) {

        try {

            Response<Boolean> urlR = popularizeUrlService.createPopUrl(popUrlCode, url);

            if (!urlR.isSuccess()) {

                throw new NotFound404Exception(urlR.getError());
            }

        } catch (Exception e) {
        }
    }
}
