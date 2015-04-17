package com.aixforce.item.dao.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.item.model.SpuBundle;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 套餐模版Dao实现类
 * CREATED BY: IntelliJ IDEA
 * AUTHOR: haolin
 * ON: 14-4-21
 */
@Repository
public class SpuBundleDao extends SqlSessionDaoSupport {

    /**
     * 根据id查询套餐模版
     * @param id 套餐模版id
     * @return 套餐模版
     */
    public SpuBundle findById(Long id) {
        return getSqlSession().selectOne("SpuBundle.findById", id);
    }

    /**
     * 根据多个id查询套餐模版
     * @param ids 主键列表
     * @return 套餐模版列表
     */
    public List<SpuBundle> findByIds(List<Long> ids) {
        return getSqlSession().selectList("SpuBundle.findByIds", ids);
    }

    /**
     * 创建套餐模版
     * @param sb 套餐模版
     * @return 插入记录数
     */
    public int create(SpuBundle sb) {
       return getSqlSession().insert("SpuBundle.create", sb);
    }

    /**
     * 删除套餐模版
     * @param id 主键
     * @return 删除记录数
     */
    public int delete(Long id) {
        return getSqlSession().delete("SpuBundle.delete",
                ImmutableMap.of("id", id));
    }

    /**
     * 更新套餐模版
     * @param sb 套餐模版
     * @return 更新记录数
     */
    public int update(SpuBundle sb) {
        return getSqlSession().update("SpuBundle.update", sb);
    }

    /**
     * 修改套餐模版状态
     * @param status 套餐模版状态
     * @return 更新记录数
     */
    public int onOff(Long sbId, SpuBundle.Status status){
        return getSqlSession().update("SpuBundle.onOff",
                ImmutableMap.of("id", sbId, "status", status.value()));
    }

    /**
     * 增加套餐模版使用次数
     * @param sbId 套餐模版id
     * @return 更新记录数
     */
    public int incrUsedCount(Long sbId){
        return getSqlSession().update("SpuBundle.incrUsedCount", sbId);
    }

    /**
     * 分页条件查询套餐模版
     * @param criteria 查询条件
     * @param offset 起始记录
     * @param limit 分页大小
     * @return 套餐模版分页对象
     */
    public Paging<SpuBundle> paging(Map<String, Object> criteria, long offset, long limit){
        if (criteria == null){
            criteria = Maps.newHashMap();
        }

        Long total = getSqlSession().selectOne("SpuBundle.count", criteria);
        if(total == 0) {
            return new Paging<SpuBundle>(0L, Collections.<SpuBundle>emptyList());
        }

        criteria.put("offset", offset);
        criteria.put("limit", limit);
        List<SpuBundle> data = getSqlSession().selectList("SpuBundle.pagination", criteria);
        return new Paging<SpuBundle>(total, data);
    }

    /**
     * 根据用户分页查找模板
     * @param userIds       用户id列表
     * @return          分页的模板
     */
    public Paging<SpuBundle> pagingByUsers(List<Long> userIds, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userIds", userIds);
        params.put("offset", offset);
        params.put("limit", limit);

        if (userIds ==null||userIds.isEmpty()) {
            return new Paging<SpuBundle>(0l, Lists.<SpuBundle>newArrayList());
        }

        Long count = getSqlSession().selectOne("SpuBundle.countByUids", userIds);
        if (count==null || count==0) {
            return new Paging<SpuBundle>(0l, Collections.<SpuBundle>emptyList());
        }
        return new Paging<SpuBundle>(count, getSqlSession().<SpuBundle>selectList("SpuBundle.findByUids", params));
    }
}
