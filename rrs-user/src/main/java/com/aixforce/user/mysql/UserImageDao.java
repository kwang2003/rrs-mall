package com.aixforce.user.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.common.utils.MapBuilder;
import com.aixforce.user.model.UserImage;
import com.google.common.base.Objects;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-13
 */
@Repository
public class UserImageDao extends SqlSessionDaoSupport {
    public UserImage findById(Long id) {
        return getSqlSession().selectOne("UserImage.findById", id);
    }

    public Integer totalCountOf(Long userId) {
        return getSqlSession().selectOne("UserImage.totalCountOf", userId);
    }

    public Paging<UserImage> findByUserIdAndCategory(Long userId, String category, Integer offset, Integer pageSize) {
        Map<String, Object> params = MapBuilder.<String, Object>of()
                .put("userId", userId).put("category", category)
                .put("offset", offset).put("limit", pageSize).map();
        Integer count = getSqlSession().selectOne("UserImage.countByUserIdAndCategory", params);
        if (count == 0) {
            return new Paging<UserImage>(0L, Collections.<UserImage>emptyList());
        }
        List<UserImage> userImages = getSqlSession().selectList("UserImage.findByUserIdAndCategory", params);
        return new Paging<UserImage>(count.longValue(), userImages);
    }


    public void create(UserImage userImage) {
        getSqlSession().insert("UserImage.create", userImage);
    }

    public void delete(Long id) {
        getSqlSession().delete("UserImage.delete", id);
    }

    public void deleteByUserId(Long userId) {
        getSqlSession().delete("UserImage.deleteByUserId", userId);
    }

    public long totalSizeByUserId(Long userId) {
        Long total = getSqlSession().selectOne("UserImage.totalSize", userId);
        return Objects.firstNonNull(total, 0L);
    }
}
