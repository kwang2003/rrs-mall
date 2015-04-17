package com.aixforce.open.controller;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.item.model.DefaultItem;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.open.dto.RichSku;
import com.aixforce.open.util.RequestUtils;
import com.aixforce.open.util.Signatures;
import com.aixforce.shop.service.ChannelShopsService;
import com.aixforce.site.service.ItemCustomService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aixforce.common.utils.Arguments.isEmpty;
import static com.aixforce.common.utils.Arguments.isNull;
import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 商品
 *
 * Created by yangjingang on 14-8-4.
 */
@Slf4j
@Controller
@RequestMapping("/api/open/item")
public class ItemController {

    @Autowired
    MessageSources messageSources;

    @Autowired
    private ChannelShopsService channelShopsService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemCustomService itemCustomService;

    private static final Splitter splitter = Splitter.on(" ");

    private static final int MAX_SKU_IDS_SIZE = 12;

    private static final Pattern imgUrlPattern = Pattern.compile("(http).*?(\\.jpg|\\.png|\\.bmp|\\.gif|\\.JPG|\\.PNG|\\.BMP|\\.GIF)");

    @RequestMapping(value = "/sku/lists", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<RichSku>> getSkuLists(@RequestParam(value="channel", required = true) String channel,
                                         @RequestParam(value="skuIds", required = true) String skuIds,
                                         @RequestParam(value="sign", required = true) String sign,
                                         HttpServletRequest request) {

        log.info("/sku/lists accepted request with channel:{}, ip:{}, skuIds:{}", channel, RequestUtils.getIpAddr(request), skuIds);

        Response<List<RichSku>> result = new Response<List<RichSku>>();
        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            List<Long> skuIdsList = getSkuIds(skuIds);
            checkArgument(skuIdsList.size() <= MAX_SKU_IDS_SIZE, "skuIds.max.count.over."+MAX_SKU_IDS_SIZE);

            Response<List<Sku>> listResponse = itemService.findSkuByIds(skuIdsList);
            checkState(listResponse.isSuccess(), listResponse.getError());
            List<Sku> skuList = listResponse.getResult();

            List<RichSku> richSkuList = Lists.newArrayList();

            for (Sku sku : skuList) {
                RichSku richSku = BeanMapper.map(sku, RichSku.class);

                Response<Item> itemResponse = itemService.findById(sku.getItemId());
                checkState(itemResponse.isSuccess(), "item of skuId("+sku.getId()+") not found");

                Item item = itemResponse.getResult();
                richSku.setMainImage(item.getMainImage());
                richSku.setItemName(item.getName());

                richSkuList.add(richSku);
            }

            //返回结果需要和传入的参数一致, 如果商品没有找到, 则返回一个空的item对象
            List<RichSku> resultList = Lists.newArrayList();
            for (Long skuId : skuIdsList) {
                boolean isExists = false;
                for (RichSku richSku : richSkuList) {
                    if (Objects.equal(skuId, richSku.getId())) {
                        isExists = true;
                        resultList.add(richSku);
                    }
                }
                if (!isExists) {
                    resultList.add(new RichSku());
                }
            }


            result.setResult(resultList);

        } catch (IllegalArgumentException e) {
            log.error("failed to get sku lists with channel:{}, ip:{}, skuIds:{}, error:{}", channel, RequestUtils.getIpAddr(request), skuIds, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        }  catch (IllegalStateException e) {
            log.error("failed to get sku lists with channel:{}, ip:{}, skuIds:{}, error:{}", channel, RequestUtils.getIpAddr(request), skuIds, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to get sku lists with channel:{}, ip:{}, skuIds:{}, error:{}", channel, RequestUtils.getIpAddr(request), skuIds, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.sku.lists"));
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
    public Response<Map<String,Object>> itemDetail(@PathVariable Long id,
                                                        @RequestParam("channel") String channel,
                                                        @RequestParam("sign") String sign,
                                                        HttpServletRequest request) {

        log.info("/{}/detail accepted request with channel:{}, ip:{}", id, channel, RequestUtils.getIpAddr(request));

        Response<Map<String,Object>> result = new Response<Map<String, Object>>();
        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Response<Map<String,Object>> detailGetResult = itemService.findWithDetailsById(id);
            checkState(detailGetResult.isSuccess(), detailGetResult.getError());

            Map<String,Object> richDetailMap = detailGetResult.getResult();

            Response<String> itemCustomDetailInfoResult = itemCustomService.findByItemOrBundleId(String.valueOf(id));
            checkState(itemCustomDetailInfoResult.isSuccess(), itemCustomDetailInfoResult.getError());


            String showImagesStr ="";
            if (isNull(itemCustomDetailInfoResult.getResult())){
                Response<String> showImageStrResponse = itemCustomService.findTemplateBySpuId(((DefaultItem) richDetailMap.get("defaultItem")).getSpuId());
                checkState(showImageStrResponse.isSuccess(), showImageStrResponse.getError());
                showImagesStr = showImageStrResponse.getResult();
            } else {
                showImagesStr = itemCustomDetailInfoResult.getResult();
            }

            // 从hbs格式的文本中取出图片的url
            String showImagesURLStr ="";
            if (!isEmpty(showImagesStr)) {
                Matcher matcher = imgUrlPattern.matcher(showImagesStr);
                while(matcher.find()) {
                    showImagesURLStr += "," + matcher.group();
                } if (showImagesURLStr.length()>0) showImagesURLStr = showImagesURLStr.substring(1);
                // 商品详情
                richDetailMap.put("showImages", showImagesURLStr);
            }

            result.setResult(richDetailMap);

        } catch (IllegalArgumentException e) {
            log.error("fail to get item or item detail with itemId:{}, channel:{}, ip:{}, error:{}", id, channel, RequestUtils.getIpAddr(request), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        }  catch (IllegalStateException e) {
            log.error("fail to get item or item detail with itemId:{}, channel:{}, ip:{}, error:{}", id, channel, RequestUtils.getIpAddr(request), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get item or item detail with itemId:{}, channel:{}, ip:{}, error:{}", id, channel, RequestUtils.getIpAddr(request), Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.query.item.detail"));
        }
        return result;
    }

    private List<Long> getSkuIds(String skuString) {
        List<String> ids = splitter.splitToList(skuString);

        List<Long> skuIds = Lists.newArrayListWithCapacity(ids.size());
        for (String id : ids) {
            skuIds.add(Long.valueOf(id));
        }
        return skuIds;
    }
}
