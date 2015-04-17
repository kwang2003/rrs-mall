package com.aixforce.trade.manager;

import com.aixforce.exception.ServiceException;
import com.aixforce.trade.dao.UserTradeInfoDao;
import com.aixforce.trade.model.UserTradeInfo;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User: yangzefeng
 * Date: 13-12-2
 * Time: 下午5:48
 */
@Component
public class TradeInfoManager {
    private final static Logger log = LoggerFactory.getLogger(TradeInfoManager.class);

    @Autowired
    private UserTradeInfoDao userTradeInfoDao;

    @Transactional
    public Long create(UserTradeInfo userTradeInfo) {
        return process(userTradeInfo);
    }

    @Transactional
    public Boolean makeDefault(Long userId, Long id) {
        List<UserTradeInfo> userTradeInfos = userTradeInfoDao.findValidByUserId(userId);
        boolean found = false;
        for (UserTradeInfo userTradeInfo : userTradeInfos) {
            if (Objects.equal(id, userTradeInfo.getId())) {
                found = true;
            }
        }
        if (!found) {
            log.error("can not find tradeInfo where id={} and userId={}", id, userId);
            throw new ServiceException("set default address failed");
        }
        Iterable<UserTradeInfo> previousDefaults = Iterables.filter(userTradeInfos, new Predicate<UserTradeInfo>() {
            @Override
            public boolean apply(UserTradeInfo tradeInfo) {
                return Objects.equal(tradeInfo.getIsDefault(), 1);
            }
        });
        for (UserTradeInfo previousDefault : previousDefaults) {
            previousDefault.setIsDefault(0);
            userTradeInfoDao.update(previousDefault);
        }
        userTradeInfoDao.makeDefault(id);
        return true;
    }

    @Transactional
    public Long update(UserTradeInfo userTradeInfo) {
        return process(userTradeInfo);
    }

    /**
     * 实际上收货信息是不允许物理修改或物理者删除的,所以对于修改,处理方式是逻辑删除再添加新信息,对于删除,则是逻辑删除
     *
     * @param userTradeInfo 收货信息
     * @return 收获信息的id
     */
    private Long process(UserTradeInfo userTradeInfo) {
        if (userTradeInfo.getId() != null) {
            //we should invalidate old userTradeInfo
            userTradeInfoDao.invalidate(userTradeInfo.getId());
            userTradeInfo.setId(null);
        }
        userTradeInfo.setStatus(1);
        userTradeInfoDao.create(userTradeInfo);
        return userTradeInfo.getId();
    }

}
