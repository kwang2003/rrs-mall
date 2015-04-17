package com.aixforce.trade.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Date: 14-2-12
 * Time: PM2:25
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class OrderComment implements Serializable {

    private static final long serialVersionUID = 2045836352523779142L;

    @Getter
	@Setter
	private Long id;

    @Getter
	@Setter
	private Long orderItemId;

    @Getter
	@Setter
	private Long buyerId;

    @Getter
	@Setter
	private Long itemId;

    @Getter
	@Setter
	private Long shopId;
    
    @Getter
	@Setter
	private Integer rQuality=5;
    
    @Getter
	@Setter
	private Integer rDescribe=5;
    
    @Getter
	@Setter
	private Integer rService=5;
    
    @Getter
	@Setter
	private Integer rExpress=5;
    
    @Getter
	@Setter
	private String comment;

    @Getter
    @Setter
    private String commentReply;// 评论回复

    @Getter
    @Setter
    private String buyerName;

    @Getter
    @Setter
    private Integer orderType;

    @Getter
    @Setter
    private Boolean isBaskOrder;    //是否是晒单评论
    
    @Getter
    @Setter
    private Date createdAt;     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;     // 修改时间

    @Getter
    @Setter
    private Long tradeQuantity; // 统计字段（`count(id)`），没有物理映射

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof OrderComment)) {
            return false;
        }

        OrderComment that = (OrderComment) o;
        return Objects.equal(orderItemId, that.orderItemId)
                && Objects.equal(buyerId, that.buyerId)
                && Objects.equal(shopId, that.shopId)
                && Objects.equal(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderItemId, buyerId, shopId, itemId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id)
                .add("orderItemId", orderItemId)
                .add("buyerId", buyerId)
                .add("buyerName", buyerName)
                .add("shopId", shopId).add("itemId", itemId).add("rQuality", rQuality).add("rDescribe", rDescribe)
                .add("rService", rService).add("rExpress", rExpress).add("comment", comment).toString();
    }

    public Boolean ifValid() {
        if (Strings.isNullOrEmpty(comment)) {
            comment = "";
        } else if (comment.length()>800) {
            return false;
        }

        return (Rate.contains(rDescribe) && Rate.contains(rExpress) &&
                Rate.contains(rQuality) && Rate.contains(rService) && orderItemId>0);
    }

    public static enum Rate {
        one(1),
        two(2),
        three(3),
        four(4),
        five(5);

        private final Integer rate;

        private static final List<Integer> rates =
                Arrays.asList(1,2,3,4,5);

        private Rate(Integer rate) {
            this.rate = rate;
        }

        public static boolean contains(Integer rate) {
            return rates.contains(rate);
        }

        public final Integer value() {
            return this.rate;
        }

        @Override
        public String toString() {
            return String.valueOf(rate);
        }
    }
}
