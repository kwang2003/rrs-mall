package com.aixforce.restful.controller.neusoft;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.dto.FacetSearchResult;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemDetail;
import com.aixforce.item.service.ItemSearchService;
import com.aixforce.item.service.ItemService;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.Signatures;
import com.aixforce.rrs.purify.dto.PurifyPageDto;
import com.aixforce.rrs.purify.service.PurifyPageService;
import com.aixforce.rrs.presale.dto.FullItemPreSale;
import com.aixforce.rrs.presale.dto.MarketItem;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.service.OrderCommentService;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 4/10/14
 * Time: 15:11
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/item")
public class NSItems {

    private final static Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();

    @Autowired
    ItemSearchService itemSearchService;

    @Autowired
    AccountService<User> accountService;

    @Autowired
    ItemService itemService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    PurifyPageService purifyPageService;

    @Autowired
    OrderCommentService orderCommentService;

    @Autowired
    PreSaleService preSaleService;

    @Value("#{app.restkey}")
    private String key;

    /**
     * 返回商城的分页的商品列表
     *
     * @param q         查询关键字
     * @param sort      排序方式
     * @param region    地区 id, 必填
     * @param pageNo    分页, 选填(默认为1)
     * @param size      每页纪录数量, 选填(默认为5)
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     * @return          搜索的商品结果
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<FacetSearchResult> list(@RequestParam(value = "q", required = false) String q,
                                                 @RequestParam(value = "order", required = false) String sort,
                                                 @RequestParam("regionId") Integer region,
                                                 @RequestParam("channel") String channel,
                                                 @RequestParam("sign") String sign,
                                                 @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(value = "size", defaultValue = "5") Integer size,
                                                 HttpServletRequest request) {
        HaierResponse<FacetSearchResult> result = new HaierResponse<FacetSearchResult>();

        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Map<String, String> params = Maps.newHashMap();
            params.put("rid", String.valueOf(region));

            if (notEmpty(q))  params.put("q", q);
            if (notEmpty(sort))  params.put("sort", sort);

            Response<FacetSearchResult> itemGetResult = itemSearchService.facetSearchItem(pageNo, size, params);
            checkState(itemGetResult.isSuccess(), itemGetResult.getError());
            result.setResult(itemGetResult.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to query list with region:{}, pageNo:{}, size:{}, error:{}", region, pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query list with region:{}, pageNo:{}, size:{}, error:{}", region, pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query list with region:{}, pageNo:{}, size:{}", region, pageNo, size, e);
            result.setError(messageSources.get("item.search.fail"));
        }

        return result;
    }

    /**
     * 获取分页的商品评价列表
     *
     * @param id        商品的id
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     * @param pageNo    页码, 选填, 默认为1
     * @param size      每页数据条目, 默认为20
     * @return          分页的商品评价
     */
    @RequestMapping(value = "/{id}/comments", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Paging<OrderComment>> comments(@PathVariable Long id,
                                                        @RequestParam(value = "channel") String channel,
                                                        @RequestParam(value = "sign") String sign,
                                                        @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(value = "size", defaultValue = "20") Integer size,
                                                        HttpServletRequest request) {
        HaierResponse<Paging<OrderComment>> result = new HaierResponse<Paging<OrderComment>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Paging<OrderComment>> itemsGet = orderCommentService.viewItemComments(id, pageNo, size);
            checkState(itemsGet.isSuccess(), itemsGet.getError());
            result.setResult(itemsGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to query comments with itemId:{}, pageNo:{}, size:{}, error:{}", id, pageNo, size, e.getMessage());
        } catch (Exception e) {
            log.error("fail to query comments with itemId:{}, pageNo:{}, size:{}", id, pageNo, size, e);
        }
        return result;
    }




    /**
     * 获取商品详情
     *
     * @param id         商品id
     * @param channel    渠道, 必填
     * @param sign       签名, 必填
     * @return      商品详情
     */
    @RequestMapping(value = "/{id}/detail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Map<String,Object>> itemDetail(@PathVariable Long id,
                                                        @RequestParam("channel") String channel,
                                                        @RequestParam("sign") String sign,
                                                        HttpServletRequest request) {

        HaierResponse<Map<String,Object>> result = new HaierResponse<Map<String,Object>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Map<String,Object>> detailGetResult = itemService.findWithDetailsById(id);
            checkState(detailGetResult.isSuccess(), detailGetResult.getError());
            result.setResult(detailGetResult.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to get item or item detail with itemId:{}, error:{}", id, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get item or item detail with itemId:{}", id, e);
            result.setError(messageSources.get("item.query.fail"));
        }
        return result;
    }


    /**
     * 前台预售列表,提供给手机端使用
     */
    @RequestMapping(value = "/preSale/list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Paging<MarketItem>> preSaleList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                         @RequestParam(value = "size", required = false) Integer size,
                                                         @RequestParam("channel") String channel,
                                                         @RequestParam("sign") String sign,
                                                         HttpServletRequest request){
        HaierResponse<Paging<MarketItem>> result = new HaierResponse<Paging<MarketItem>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Response<Paging<MarketItem>> preSaleR = preSaleService.paginationByUser(pageNo, size);
            if (!preSaleR.isSuccess()) {
                log.error("fail to find preSale item list, pageNo={}, size={}, error code:{}",
                        pageNo, size, preSaleR.getError());
                result.setError(messageSources.get(preSaleR.getError()));
                return result;
            }
            result.setResult(preSaleR.getResult());
            return result;

        }catch (IllegalArgumentException ex) {
            log.error("fail to find preSale items pageNo={}, size={}, channel={}, sign={},cause:{}",
                    pageNo, size, channel, sign, ex.getMessage());
            result.setError(messageSources.get(ex.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to find preSale items pageNo={}, size={}, channel={}, sign={},cause:{}",
                    pageNo, size, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("preSale.query.fail"));
            return result;
        }
    }


    /**
     * 预售商品详情页
     */
    @RequestMapping(value = "/preSale/{itemId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<FullItemPreSale> preSaleDetail(@PathVariable("itemId") Long itemId,
                                                        @RequestParam("channel") String channel,
                                                        @RequestParam("sign") String sign,
                                                        HttpServletRequest request) {
        HaierResponse<FullItemPreSale> result = new HaierResponse<FullItemPreSale>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Response<FullItemPreSale> preSaleItemR = preSaleService.findFullItemPreSale(itemId);
            if(!preSaleItemR.isSuccess()) {
                log.error("fail to find preSale item detail by itemId={}, error code:{}",
                        itemId, preSaleItemR.getError());
                result.setError(messageSources.get(preSaleItemR.getError()));
                return result;
            }

            result.setResult(preSaleItemR.getResult());
            return result;

        }catch (IllegalArgumentException ex) {
            log.error("fail to find preSale item detail by itemId={}, channel={}, sign={}, cause:{}",
                    itemId, channel, sign, ex.getMessage());
            result.setError(messageSources.get(ex.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to find preSale item detail by itemId={}, channel={}, sign={}, cause:{}",
                    itemId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("preSale.item.not.found"));
            return result;
        }
    }


    /**
     * 水机定制
     *
     * @param  sid          系列编号, 必填
     * @param  assemblyIds  组件页面编号，选填， 默认返回第一步的页面数据
     * @param  channel      渠道, 必填
     * @param  sign         签名, 必填
     *
     */
    @RequestMapping(value = "/{sid}/customize", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<PurifyPageDto> purifyPage(@PathVariable Long sid,
                                                   @RequestParam(value = "assembles", required = false) String assemblyIds,
                                                   @RequestParam("channel") String channel,
                                                   @RequestParam("sign") String sign,
                                                   HttpServletRequest request) {
        HaierResponse<PurifyPageDto> result=  new HaierResponse<PurifyPageDto>();
        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Long[] ids;

            if (notEmpty(assemblyIds)) {

                List<String> idList = splitter.splitToList(assemblyIds);
                ids = convertToLong(idList).toArray(new Long[idList.size()]);
            } else {
                ids = null;
            }

            Response<PurifyPageDto> queryResult = purifyPageService.findPurifyPageInfo(sid, ids);
            checkState(queryResult.isSuccess(), queryResult.getError());
            result.setResult(queryResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to query purifyPage with channel:{}, sign:{}, error:{}", channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query purifyPage with channel:{}, sign:{}, error:{}", channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query purifyPage with channel:{}, sign:{}", channel, sign);
            result.setError(messageSources.get("purify.query.fail"));
        }

        return result;
    }

    private List<Long> convertToLong(List<String> identities) {
        List<Long> ids = Lists.newArrayListWithCapacity(identities.size());
        for (String identity : identities) {
            ids.add(Long.valueOf(identity));
        }
        return ids;
    }

    @NoArgsConstructor
    @ToString
    private static class ItemDetailDto implements Serializable {
        private static final long serialVersionUID = 6505898257928193272L;
        @Setter
        @Getter
        Item item;

        @Getter
        @Setter
        ItemDetail detail;
    }
}
