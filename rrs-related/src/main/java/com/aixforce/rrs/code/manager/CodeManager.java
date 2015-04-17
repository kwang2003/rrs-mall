package com.aixforce.rrs.code.manager;

import com.aixforce.rrs.code.dao.ActivityDefinitionDao;
import com.aixforce.rrs.code.dao.CodeUsageDao;
import com.aixforce.rrs.code.model.CodeUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-10 9:53 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class CodeManager {

    @Autowired
    private CodeUsageDao codeUsageDao;

    @Autowired
    private ActivityDefinitionDao activityDefinitionDao;

    @Transactional
    public void createCodeUsage(CodeUsage codeUsage) {
        codeUsageDao.create(codeUsage);
        activityDefinitionDao.addOrderUsedCount(codeUsage.getActivityId());
    }
}
