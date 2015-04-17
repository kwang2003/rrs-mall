package com.aixforce.item.dao.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.item.model.TitleKeyword;
import org.elasticsearch.common.base.Predicate;
import org.elasticsearch.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @desc 提供对 页面-搜索 关键字信息表的 CRUD 操作方法
 * <p/>
 * Created by wanggen on 14-6-30.
 */
@Repository
public class TitleKeywordDao extends SqlSessionDaoSupport {

    // mapper 的命名空间
    private final static String namespace = "TitleKeyword.";


    public Long create(TitleKeyword titleKeyword) {
        getSqlSession().insert(namespace + "create", titleKeyword);
        return titleKeyword.getId();
    }


    public int deleteById(Long id) {
        return getSqlSession().delete(namespace + "deleteById", id);
    }


    public TitleKeyword findById(Long id) {
        return getSqlSession().selectOne(namespace + "findById", id);
    }

    /**
     * 根据nameId 查询结果
     *
     * @param nameId nameId
     * @return 结果 {@link com.aixforce.item.model.TitleKeyword}
     */
    public TitleKeyword findByNameId(Long nameId) {
        return getSqlSession().selectOne(namespace + "findByNameId", nameId);
    }

    public TitleKeyword findByPath(String path) {
        return getSqlSession().selectOne(namespace + "findByPath", path);
    }

    /**
     * 根据条件查询结果集的总条数
     *
     * @return 该条件的结果集数
     */
    private int selectCount() {
        return getSqlSession().<Integer>selectOne(namespace + "selectCount");
    }


    /**
     * 分页查询
     *
     * @param start 结果集的开始(起始为0)
     * @param limit 该次查询需返回的数量
     * @return 查询结果
     */
    public Paging<TitleKeyword> findAll(Map<String, Object> param, final Integer start, final Integer limit) {
        Map<String, Object> nonNullParam = Maps.filterEntries(param, new Predicate<Map.Entry<String, Object>>() {
            public boolean apply(@Nullable Map.Entry<String, Object> entry) {
                return entry != null && entry.getValue() != null && !entry.getValue().toString().trim().equals("");
            }
        });
        Paging<TitleKeyword> result = new Paging<TitleKeyword>();
        long count = getSqlSession().<Integer>selectOne(namespace + "selectCount", nonNullParam);
        result.setTotal(count);
        nonNullParam.put("start", start);
        nonNullParam.put("limit", limit);
        result.setData(getSqlSession().<TitleKeyword>selectList(namespace + "findAll", nonNullParam));
        return result;
    }


    /**
     * 更新操作
     *
     * @param updateParam 更新操作参数
     * @return 更新影响行数
     */
    public int update(TitleKeyword updateParam) {
        return getSqlSession().update(namespace + "update", updateParam);
    }
}
