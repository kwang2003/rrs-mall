package com.aixforce.trade.manager;

import com.aixforce.trade.dao.InstallInfoDao;
import com.aixforce.trade.model.InstallInfo;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 物流安装信息管理
 * Author: haolin
 * On: 9/23/14
 */
@Component
public class InstallInfoManager {

    @Autowired
    private InstallInfoDao installInfoDao;

    /**
     * 同步物流安装信息
     * @param newInstallInfos 安装信息列表
     */
    @Transactional
    public void sync(List<InstallInfo> newInstallInfos){
        List<String> names = Lists.transform(newInstallInfos, new Function<InstallInfo, String>() {
            @Override
            public String apply(InstallInfo info) {
                return info.getName();
            }
        });
        // 已经物理存在的安装信息
        List<InstallInfo> exists = installInfoDao.findByNames(names);
        // 被逻辑删除的安装信息
        List<InstallInfo> deleted = Lists.newArrayList();
        // 过滤出待添加的安装信息
        for (InstallInfo exist : exists){
            if (Objects.equal(InstallInfo.Status.DELETED.value(), exist.getStatus())){
                deleted.add(exist);
            } else {
                newInstallInfos.remove(exist);
            }
        }

        // 更新逻辑删除的安装信息为停用状态
        List<Long> deletedIds = Lists.transform(deleted, new Function<InstallInfo, Long>() {
            @Override
            public Long apply(InstallInfo info) {
                return info.getId();
            }
        });
        installInfoDao.updatesStatus(deletedIds, InstallInfo.Status.DISABLED.value());

        // 添加新的安装信息
        for (InstallInfo newInstallInfo : newInstallInfos){
            newInstallInfo.setStatus(InstallInfo.Status.DISABLED.value());
        }
        installInfoDao.creates(newInstallInfos);
    }
}
