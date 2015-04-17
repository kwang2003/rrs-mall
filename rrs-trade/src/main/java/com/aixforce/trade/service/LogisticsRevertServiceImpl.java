package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.dao.LogisticsRevertDao;
import com.aixforce.trade.model.LogisticsRevert;
import com.aixforce.trade.model.OrderItem;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Description：
 * Author：Guo Chaopeng
 * Created on 14-4-22-上午11:40
 */
@Slf4j
@Service
public class LogisticsRevertServiceImpl implements LogisticsRevertService {

    @Autowired
    private LogisticsRevertDao logisticsRevertDao;

    @Autowired
    private OrderQueryService orderQueryService;

    public Response<Long> create(LogisticsRevert logisticsRevert) {
        Response<Long> result = new Response<Long>();

        if (logisticsRevert == null) {
            log.error("logisticsRevert can not be null");
            result.setError("logistics.revert.not.null.fail");
            return result;
        }

        if (logisticsRevert.getOrderItemId() == null) {
            log.error("logisticsRevert orderItemId can not be null");
            result.setError("logistics.orderItemId.not.null.fail");
            return result;
        }

        if (logisticsRevert.getCompanyName() == null) {
            log.error("logisticsRevert companyName can not be null");
            result.setError("logistics.companyName.not.null.fail");
            return result;
        }

        if (logisticsRevert.getFreightNote() == null) {
            log.error("logisticsRevert freightNote can not be null");
            result.setError("logistics.freightNote.not.null.fail");
            return result;
        }

        if (logisticsRevert.getBuyerId() == null) {
            log.error("logisticsRevert buyerId can not be null");
            result.setError("logistics.buyerId.not.null.fail");
            return result;
        }

        if (logisticsRevert.getBuyerName() == null) {
            log.error("logisticsRevert buyerName can not be null");
            result.setError("logistics.buyerName.not.null.fail");
            return result;
        }

        if (logisticsRevert.getLogisticsStatus() == null) {
            log.error("logisticsRevert logisticsStatus can not be null");
            result.setError("logistics.logisticsStatus.not.null.fail");
            return result;
        }

        try {

            //检查该订单的退货物流信息是否早已存在
            LogisticsRevert existed = logisticsRevertDao.findByOrderItemId(logisticsRevert.getOrderItemId());
            if (existed != null) {
                log.error("logisticsRevert duplicated where orderItemId={}", logisticsRevert.getOrderItemId());
                result.setError("logisticsRevert.duplicated");
                return result;
            }

            Response<OrderItem> orderItemResponse = orderQueryService.findOrderItemById(logisticsRevert.getOrderItemId());
            OrderItem orderItem = orderItemResponse.getResult();

            //检查子订单是否存在
            if (orderItem == null) {
                log.error("orderItem(id={}) not found", logisticsRevert.getOrderItemId());
                result.setError("logistics.orderItem.not.found.fail");
                return result;
            } else {
                logisticsRevert.setSendFee(orderItem.getDeliverFee());//设置运费
            }

            //检查该子订单是否属于该买家用户
            if (!Objects.equal(orderItem.getBuyerId(), logisticsRevert.getBuyerId())) {
                log.error("current user don't have the right");
                result.setError("authorize.fail");
                return result;
            }

            //检查卖家是否已同意退货
            if (!Objects.equal(orderItem.getStatus(), OrderItem.Status.AGREE_RETURNGOODS.value())) {
                log.error("seller don't agree return goods yet");
                result.setError("seller.not.agree.return.goods.yet");
                return result;
            }

            Long id = logisticsRevertDao.create(logisticsRevert);
            result.setResult(id);
            return result;

        } catch (Exception e) {
            log.error("fail to record logistics revert where orderItemId={},cause:{}", logisticsRevert.getOrderItemId(), Throwables.getStackTraceAsString(e));
            result.setError("logistics.revert.record.fail");
            return result;
        }
    }


    public Response<Boolean> updateStatus(Long id, Integer status, Long currentUserId) {
        Response<Boolean> result = new Response<Boolean>();

        if (id == null) {
            log.error("logisticsRevert id can not be null");
            result.setError("logistics.revert.id.not.null.fail");
            return result;
        }

        if (currentUserId == null) {
            log.error("user id can not be null");
            result.setError("user.id.not.null");
            return result;
        }

        if (status == null) {
            log.error("status can not be null");
            result.setError("logistics.logisticsStatus.not.null.fail");
            return result;
        }

        try {

            LogisticsRevert logisticsRevert = logisticsRevertDao.findById(id);

            if (logisticsRevert == null) {
                log.error("logisticsRevert(id={}) not found", id);
                result.setError("logistics.revert.not.found");
                return result;
            }

            //检查该退货物流信息是否属于该买家用户
            if (!Objects.equal(logisticsRevert.getBuyerId(), currentUserId)) {
                log.error("current user don't have the right");
                result.setError("authorize.fail");
                return result;
            }

            logisticsRevert.setLogisticsStatus(status);
            logisticsRevertDao.update(logisticsRevert);
            result.setResult(Boolean.TRUE);
            return result;

        } catch (Exception e) {
            log.error("fail to update logisticsRevert status where id={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("logistics.revert.status.update.fail");
            return result;
        }
    }

    public Response<LogisticsRevert> findByOrderItemId(Long orderItemId, Long currentUserId) {
        Response<LogisticsRevert> result = new Response<LogisticsRevert>();

        if (orderItemId == null) {
            log.error("orderItemId can not be null");
            result.setError("logistics.orderItemId.not.null.fail");
            return result;
        }

        if (currentUserId == null) {
            log.error("user id can not be null");
            result.setError("user.id.not.null");
            return result;
        }

        try {

            Response<OrderItem> orderItemResponse = orderQueryService.findOrderItemById(orderItemId);
            OrderItem orderItem = orderItemResponse.getResult();

            //检查子订单是否存在
            if (orderItem == null) {
                log.error("orderItem(id={}) not found", orderItemId);
                result.setError("logistics.orderItem.not.found.fail");
                return result;
            }

            //检查该子订单是否属于该买家用户
            if (!Objects.equal(orderItem.getBuyerId(), currentUserId)) {
                log.error("current user don't have the right");
                result.setError("authorize.fail");
                return result;
            }

            LogisticsRevert logisticsRevert = logisticsRevertDao.findByOrderItemId(orderItemId);
            if (logisticsRevert == null) {
                log.error("logisticsRevert not found where orderItemId={}", orderItemId);
                result.setError("logisticsRevert.not.found");
                return result;
            }

            result.setResult(logisticsRevert);
            return result;
        } catch (Exception e) {
            log.error("fail to query logisticsRevert where orderItemId={},cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("logistics.revert.query.fail");
            return result;
        }
    }

}
