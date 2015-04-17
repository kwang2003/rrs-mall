package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.Arguments;
import com.aixforce.rrs.jde.mdm.JdeMdmRequest;
import com.aixforce.rrs.jde.mdm.MdmPagingResponse;
import com.aixforce.rrs.jde.mdm.MdmUpdating;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-25 2:07 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class OuterCodeScheduleHandle {


    @Value("#{app.mdmSyncUrl}")
    private String mdmSyncUrl;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private ShopService shopService;

    private static final int BATCH_SIZE = 200;


    static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");   // 统一日期时间

    public void full() {
        try {
            log.info("[OUTERCODE-UPDATE-FULL] job begin at {}", DFT.print(DateTime.now()));
            Stopwatch stopwatch = Stopwatch.createStarted();
            Integer pageNo = 1;
            Integer currentSize = BATCH_SIZE;

            while (currentSize == BATCH_SIZE) {
                Response<Paging<Shop>> shopQueryResult = shopService.findWithTaxNo(pageNo, BATCH_SIZE);
                checkState(shopQueryResult.isSuccess(), shopQueryResult.getError());
                Paging<Shop> paging = shopQueryResult.getResult();
                List<Shop> shops = paging.getData();
                log.info("shop with taxNo static {}", shops.size());

                for (Shop shop : shops) {
                    try {
                        log.info("process shop(id:{}, name:{}, taxNo:{}", shop.getId(), shop.getName(), shop.getTaxRegisterNo());
                        Response<ShopExtra> shopExtraQueryResult = shopService.getExtra(shop.getUserId());
                        checkState(shopExtraQueryResult.isSuccess(), shopExtraQueryResult.getError());
                        ShopExtra extra = shopExtraQueryResult.getResult();

                        if (notEmpty(extra.getOuterCode())) {
                            log.info("shop(id:{}) outerCode not empty skipped", shop.getId());
                            continue;
                        }

                        log.info("request for outerCode with taxNo:{}", shop.getTaxRegisterNo());
                        MdmPagingResponse result = JdeMdmRequest.build(mdmSyncUrl)
                                .taxNo(shop.getTaxRegisterNo()).pageNo(1).load();
                        log.info("request result:{}", result);
                        doSynchronized(result);

                    } catch (IllegalStateException e) {
                        log.error("fail to process shop(id:{}, name:{}, taxNo:{}) error:{}",
                                shop.getId(), shop.getName(), shop.getTaxRegisterNo(), e.getMessage());
                    } catch (Exception e) {
                        log.error("fail to process shop(id:{}, name:{}, taxNo:{}) cause:{}",
                                shop.getId(), shop.getName(), shop.getTaxRegisterNo(), Throwables.getStackTraceAsString(e));
                    }
                }

                pageNo ++;
                currentSize = paging.getData().size();
            }

            long sec = stopwatch.elapsed(TimeUnit.SECONDS);
            log.info("[OUTERCODE-UPDATE-FULL] done successfully, cast: {} sec", sec);
        } catch (IllegalStateException e) {
            log.error("[OUTERCODE-UPDATE-FULL] failed to sync outerCode fully, error:{}", e.getMessage());
        } catch (Exception e) {
            log.error("[OUTERCODE-UPDATE-FULL] failed to sync outerCode fully, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }





    public void syncOuterCode(Date now) {
        try {
            log.info("[OUTERCODE-UPDATE] job begin at {}", DFT.print(DateTime.now()));
            Stopwatch stopwatch = Stopwatch.createStarted();

            Integer pageNo = 1;
            DateTime timeNow = new DateTime(now);

            boolean hasNext = Boolean.TRUE;
            while(hasNext) {
                MdmPagingResponse result = JdeMdmRequest.build(mdmSyncUrl)
                        .startAt(timeNow.minusDays(1).toDate())
                        .endAt(timeNow.toDate()).pageNo(pageNo).load(1440);
                doSynchronized(result);
                hasNext = result.hasNext();
                pageNo ++;
            }


            long sec = stopwatch.elapsed(TimeUnit.SECONDS);
            log.info("[OUTERCODE-UPDATE] done successfully, cast: {} sec", sec);

        } catch (IllegalStateException e) {
            log.error("[OUTERCODE-UPDATE] fail to sync outerCode, error:{}", e.getMessage());
        } catch (Exception e) {
            log.error("[OUTERCODE-UPDATE] fail to sync outerCode", e);
        }
    }

    private void doSynchronized(MdmPagingResponse result) {
        List<MdmUpdating> mdmUpdatings = result.getData();
        for (MdmUpdating updating : mdmUpdatings) {
            try {
                log.info("processing taxNo:{} outerCode:{}, updatedAt:{} ",
                        updating.getTaxNo(), updating.getOuterCode(), updating.getUpdatedAt());

                Response<List<Shop>> updateResult = shopService
                        .batchUpdateOuterCodeWithTaxNo(updating.getTaxNo(), updating.getOuterCode());
                checkState(updateResult.isSuccess(), updateResult.getError());

                // 获取更新成功的店铺列表
                List<Shop> shops = updateResult.getResult();

                for (Shop shop : shops) {
                    log.info("updating shop (id:{} name:{}) related to outerCode:{}",
                            shop.getId(), shop.getName(), updating.getOuterCode());
                    settlementService.batchUpdateOuterCodeOfShopRelated(updating.getOuterCode(), shop);
                }
            } catch (IllegalStateException e) {
                log.error("fail to update outer code with updating:{}, error:{}", updating, e.getMessage());
            } catch (Exception e) {
                log.error("fail to update outer code with updating:{}", updating, e);
            }
        }
    }


}
