package com.aixforce.trade.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.Bootstrap;
import com.aixforce.trade.dao.BaskOrderDao;
import com.aixforce.trade.dao.OrderCommentDao;
import com.aixforce.trade.dao.OrderItemDao;
import com.aixforce.trade.manager.BaskOrderManager;
import com.aixforce.trade.model.BaskOrder;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.model.OrderItem;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.aixforce.common.utils.Arguments.isNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by songrenfei on 14-9-16.
 */
@Slf4j
@Service
public class BaskOrderServiceImpl implements BaskOrderService {

    @Autowired
    private BaskOrderDao baskOrderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private OrderCommentDao orderCommentDao;
    @Autowired
    private BaskOrderManager baskOrderManager;

    @Override
    public Response<Long> create(BaskOrder baskOrder) {
        Response<Long> result = new Response<Long>();
        try {
            checkArgument(!isNull(baskOrder),"illegal.param");
            Long id = baskOrderDao.create(baskOrder);
            result.setResult(id);
        }catch (IllegalArgumentException e){
            log.error("fail to create baskOrder error:{}",e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("fail to create baskOrder:{},cause:{}",baskOrder, Throwables.getStackTraceAsString(e));
            result.setError("fail.to.create.baskOrder");
        }
        return result;
    }

    @Override
    public Response<Long> create(Long orderItemId,BaskOrder baskOrder,Long userId) {
        Response<Long> result = new Response<Long>();
        try {
            checkArgument(!isNull(orderItemId),"illegal.param");
            checkArgument(!isNull(userId),"illegal.param");
            checkArgument(!isNull(baskOrder),"illegal.param");

            OrderItem orderItem = orderItemDao.findById(orderItemId);

            if (orderItem == null) {
                log.error("no order item found for id {}", orderItem);
                result.setError("order.item.find.fail");
                return result;
            }

            if (!Objects.equal(orderItem.getBuyerId(), userId)) {//只有买家才能晒单
                log.error("buyer mismatch,  expected buyer id: {}, but actual user id: {}, ",
                        orderItem.getBuyerId(), userId);
                result.setError("order.comment.not.own.order");
                return result;
            }
            //暂时不和评论关联
//            OrderComment orderComment =orderCommentDao.findByOrderItemId(orderItemId);
//
//            if (orderComment == null) {
//                log.error("no order comment found for id {}", orderItem);
//                result.setError("order.comment.find.fail");
//                return result;
//            }
            BaskOrder exitBaskOrder = baskOrderDao.findByOrderItemId(orderItemId);
            //说明已经晒过单
            if(!isNull(exitBaskOrder)){
                log.error("bask order create duplicate");
                result.setError("bask.order.create.duplicate");
                return result;
            }
            baskOrder.setOrderItemId(orderItemId);
//
//            baskOrder.setOrderCommentId(orderComment.getId());
            Long id = baskOrderManager.updateCommentAndSaveBaskOrder(null,baskOrder,orderItemId);
            result.setResult(id);
        }catch (IllegalArgumentException e){
            log.error("fail to create baskOrder error:{}",e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("fail to create baskOrder:{},cause:{}",baskOrder, Throwables.getStackTraceAsString(e));
            result.setError("fail.to.create.baskOrder");
        }
        return result;
    }

    @Override
    public Response<Boolean> update(BaskOrder baskOrder) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(baskOrder),"illegal.param");
            checkArgument(!isNull(baskOrder.getId()),"illegal.param");
            baskOrderDao.update(baskOrder);
            result.setResult(Boolean.TRUE);
        }catch (IllegalArgumentException e){
            log.error("fail to update baskOrder error:{}",e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("fail to update baskOrder:{},cause:{}",baskOrder, Throwables.getStackTraceAsString(e));
            result.setError("fail.to.update.baskOrder");
        }
        return result;
    }

    @Override
    public Response<Boolean> delete(Long id) {

        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            baskOrderDao.delete(id);
            result.setResult(Boolean.TRUE);
        }catch (IllegalArgumentException e){
            log.error("fail to update baskOrder error:{}",e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("fail to delete baskOrder id={},cause:{}",id, Throwables.getStackTraceAsString(e));
            result.setError("fail.to.delete.baskOrder");
        }
        return result;
    }

    @Override
    public Response<BaskOrder> findById(Long id) {

        Response<BaskOrder> result = new Response<BaskOrder>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            BaskOrder  exitBaskOrder = baskOrderDao.findById(id);
            result.setResult(exitBaskOrder);
        }catch (IllegalArgumentException e){
            log.error("fail to create baskOrder error:{}",e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("fail to find baskOrder by id={},cause:{}",id, Throwables.getStackTraceAsString(e));
            result.setError("fail.to.find.baskOrder");
        }
        return result;
    }

    @Override
    public Response<Paging<BaskOrder>> paging(Long itemId, Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<BaskOrder>> result = new Response<Paging<BaskOrder>>();
        try {
            checkArgument(!isNull(itemId),"illegal.param");
            Paging<BaskOrder> baskOrderPaging = baskOrderDao.pagingByItemId(itemId, page.getOffset(), page.getLimit());
            result.setResult(baskOrderPaging);
        }catch (IllegalArgumentException e){
            log.error("`findByItemId` invoke fail. error:{}", e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e) {
            log.error("`findByItemId` invoke fail. e:{}", e);
            result.setError("bask.order.query.fail");
        }
        return result;
    }
}
