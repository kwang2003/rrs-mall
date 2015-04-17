package com.aixforce.rrs;

import lombok.extern.slf4j.Slf4j;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringApplicationContext;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-04-08 2:18 PM  <br>
 * Author: xiao
 */
@SpringApplicationContext({"classpath:/spring/related-service-test.xml"})
@Transactional(TransactionMode.ROLLBACK)
@Slf4j
public abstract class BaseServiceTest extends UnitilsJUnit4 {}
