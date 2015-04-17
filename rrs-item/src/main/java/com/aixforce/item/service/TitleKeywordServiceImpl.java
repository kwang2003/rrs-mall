package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.dao.mysql.TitleKeywordDao;
import com.aixforce.item.model.TitleKeyword;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 页面-搜索关键字信息表的操作实现类，提供 insert/delete/update/query 的具体实现
 * <p/>
 * Created by wanggen on 14-6-30.
 */
@Service
@Slf4j
public class TitleKeywordServiceImpl implements TitleKeywordService {
    private static final JavaType JAVA_TYPE = JsonMapper.nonEmptyMapper().createCollectionType(Map.class, String.class, String.class);

    @Autowired
    private TitleKeywordDao titleKeywordDao;    // 注入 Dao bean

    private static final Splitter splitter = Splitter.on("\r\n");

    @Override
    public Response<Long> create(TitleKeyword titleKeyword) {

        Response<Long> insertLen = new Response<Long>();
        try {
            Long id = titleKeywordDao.create(titleKeyword);
            insertLen.setResult(id);
            return insertLen;
        } catch (Exception e) {
            insertLen.setError("titlekeyword.insert.fail");
            log.error("failed to insert record with param:[{}] into title_keyword, Caused by:{}", titleKeyword, Throwables.getStackTraceAsString(e));
            return insertLen;
        }

    }

    @Override
    public Response<Long> deleteById(Long id) {

        Response<Long> delCount = new Response<Long>();
        try {
            int count = titleKeywordDao.deleteById(id);
            delCount.setResult((long) count);
            return delCount;
        } catch (Exception e) {
            delCount.setError("titlekeyword.delete.fail");
            log.error("failed to deleteById from title_keyword with id:[{}], Caused by:{}", id, Throwables.getStackTraceAsString(e));
            return delCount;
        }

    }


    @Override
    public Response<TitleKeyword> findById(Long id) {

        Response<TitleKeyword> resp = new Response<TitleKeyword>();
        try {
            TitleKeyword record = titleKeywordDao.findById(id);
            resp.setResult(record);
            return resp;
        } catch (Exception e) {
            resp.setError("titlekeyword.query.fail");
            log.error("failed to findById record with param:[id={}], Caused by:{}", id, Throwables.getStackTraceAsString(e));
            return resp;
        }
    }

    @Override
    public Response<TitleKeyword> findByNameId(Long nameId) {
        Response<TitleKeyword> resp = new Response<TitleKeyword>();
        try {
            TitleKeyword record = titleKeywordDao.findByNameId(nameId);
            if (record != null) {
                Map<String, String> friendLinkMap = JsonMapper.nonEmptyMapper().fromJson(record.getFriendLinks(), JAVA_TYPE);
                record.setFriendLinkMap(friendLinkMap);
                resp.setResult(record);
            }
            return resp;
        } catch (Exception e) {
            resp.setError("titlekeyword.query.fail");
            log.error("failed to findByNameId records with param:[{}], Caused by:{}", nameId, Throwables.getStackTraceAsString(e));
            return resp;
        }
    }

    @Override
    public Response<TitleKeyword> findByPath(String path) {
        Response<TitleKeyword> resp = new Response<TitleKeyword>();
        try {
            TitleKeyword record = titleKeywordDao.findByPath(path);
            if (record != null) {
                Map<String, String> friendLinkMap = JsonMapper.nonEmptyMapper().fromJson(record.getFriendLinks(), JAVA_TYPE);
                record.setFriendLinkMap(friendLinkMap);
                resp.setResult(record);
            }
            return resp;
        } catch (Exception e) {
            resp.setError("titlekeyword.query.fail");
            log.error("failed to findByNamePath records with param:[{}], Caused by:{}", path, Throwables.getStackTraceAsString(e));
            return resp;
        }
    }

    @Override
    public Response<Paging<TitleKeyword>> findAll(Map<String, Object> param, Integer pageNo, Integer count) {

        Response<Paging<TitleKeyword>> resp = new Response<Paging<TitleKeyword>>();
        try {
            PageInfo pageInfo = new PageInfo(pageNo, count);
            Paging<TitleKeyword> pagingResult = titleKeywordDao.findAll(param, pageInfo.getOffset(), pageInfo.getLimit());
            if (pagingResult.getData() != null) {
                for (TitleKeyword titleKeyword : pagingResult.getData()) {
                    Map<String, String> friendLinkMap = JsonMapper.nonEmptyMapper().fromJson(titleKeyword.getFriendLinks(), JAVA_TYPE);
                    titleKeyword.setFriendLinkMap(friendLinkMap);
                }
                resp.setResult(pagingResult);
            }
            return resp;
        } catch (Exception e) {
            resp.setError("titlekeyword.query.fail");
            log.error("failed to findAll records, Caused by:{}", Throwables.getStackTraceAsString(e));
            return resp;
        }

    }


    @Override
    public Response<Long> update(TitleKeyword updateParam) {

        Response<Long> resp = new Response<Long>();
        try {
            int count = titleKeywordDao.update(updateParam);
            resp.setResult((long) count);
            return resp;
        } catch (Exception e) {
            resp.setError("titlekeyword.update.fail");
            log.error("failed to update records with param:[{}], Caused by:{}", updateParam, Throwables.getStackTraceAsString(e));
            return resp;
        }

    }

    @Override
    public List<Map> findByFcid(@ParamInfo("fcid") Long fcid) {

        List<Map> result = Lists.newArrayList();

        Response<TitleKeyword> titleKeywordResponse = this.findByNameId(fcid);
        if (titleKeywordResponse.isSuccess()) {
            TitleKeyword titleKeyword = titleKeywordResponse.getResult();
            String friendLinks = titleKeyword.getFriendLinks();
            List<String> friendLinksList = splitter.splitToList(friendLinks);
            for (int i=0;i<friendLinksList.size();i++) {
                String friendLink = friendLinksList.get(i);
                // 替换多个空格为一个空格
                friendLink = friendLink.replaceAll("\\s{2,}", " ");
                if (friendLink.contains(" ")) {
                    String[] friendLinkArr = friendLink.split(" ");
                    if (friendLinkArr.length >= 2) {
                        Map map = Maps.newConcurrentMap();
                        map.put("name", friendLinkArr[0]);
                        map.put("url", friendLinkArr[1]);
                        if (i<friendLinksList.size()-2) {
                            map.put("spliter", "|");
                        }
                        result.add(map);
                    }
                }
            }
        }
        return result;
    }
}
