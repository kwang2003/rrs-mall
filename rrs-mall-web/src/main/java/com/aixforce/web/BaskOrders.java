package com.aixforce.web;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.BaskOrder;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.service.BaskOrderService;
import com.aixforce.trade.service.OrderCommentService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static com.aixforce.common.utils.Arguments.isNull;

/**
 * 晒单
 * Created by songrenfei on 14-9-16.
 */
@Slf4j
@Controller
@RequestMapping("api/baskorder")
public class BaskOrders {
    private final static JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();

    @Autowired
    private BaskOrderService baskOrderService;
    @Autowired
    private ServletContext servletContext;

    private AtomicReference<Set<String>> sensitiveWords;

    private File sensitiveWordsDictionary;

    private volatile long lastModified;
    @Autowired
    private MessageSources messageSources;

    private final static DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    @PostConstruct
    public void init() {
        this.sensitiveWords = new AtomicReference<Set<String>>();
        sensitiveWordsDictionary = new File(servletContext.getRealPath("/")+
                File.separator + "WEB-INF" + File.separator + "sensor_words.txt");
        reload();
        lastModified = sensitiveWordsDictionary.lastModified();
    }

    private void checkIfNeedReload() {
        if (sensitiveWordsDictionary.lastModified() > lastModified) {
            lastModified = sensitiveWordsDictionary.lastModified();
            reload();
        }

    }

    private void reload() {
        try {
            final Set<String> result = Files.readLines(sensitiveWordsDictionary, Charsets.UTF_8, new LineProcessor<Set<String>>() {
                private Set<String> sensitveWords = Sets.newHashSet();

                @Override
                public boolean processLine(String line) throws IOException {
                    sensitveWords.add(line.trim());
                    return true;
                }

                @Override
                public Set<String> getResult() {
                    return sensitveWords;
                }
            });
            this.sensitiveWords.set(result);
        } catch (IOException e) {
            log.error("failed to load sensitive words file ({}), cause:{}", sensitiveWordsDictionary.getAbsolutePath(),
                    Throwables.getStackTraceAsString(e));
        }
    }


    /**
     * 检查敏感字内容 , 并做替换
     *
     * @param content 输入的内容
     */
    public String check(String content) {
        if (!sensitiveWordsDictionary.exists()) {
            return content;
        }
        checkIfNeedReload();
        for (String word : this.sensitiveWords.get()) {
            String target = Strings.repeat("*", word.length());
            content = content.replaceAll(word, target);
        }
        return content;
    }

    /**
     * 创建 晒单
     * @param orderItemId   子订单ID
     * @param json      订单评价 json
     * @return          评价的 ID 列表
     */
    @RequestMapping(value = "/{orderItemId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(@PathVariable("orderItemId") Long orderItemId, @RequestParam("detail") String json) {

        BaskOrder baskOrder = jsonMapper.fromJson(json, BaskOrder.class);
        //Date date = DFT.parseDateTime(baskOrder.getBuyTime()).toDate();//字符串转换为Date
       // baskOrder.setBuyAt(date);
        baskOrder.setPic(jsonMapper.toJson(baskOrder.getPics()));

        if (isNull(baskOrder)) {
            throw new JsonResponseException(500, messageSources.get("bask.order.invalid.argument"));
        }
        baskOrder.setContent(check(baskOrder.getContent()));

        Response<Long> result = baskOrderService.create(orderItemId, baskOrder, UserUtil.getUserId());
        if (result.isSuccess()) {
            return result.getResult();
        }

        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }



    @RequestMapping(value = "/item", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<BaskOrder> viewItemComments(@RequestParam("itemId") Long itemId,
                                                 @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Response<Paging<BaskOrder>> result = baskOrderService.paging(itemId, pageNo, size);

        if (result.isSuccess()) {
            return result.getResult();
        }

        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }
}
