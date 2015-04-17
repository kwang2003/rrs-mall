package com.aixforce.rrs.predeposit.dto;

import com.aixforce.rrs.predeposit.model.PreDeposit;
import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.trade.dto.RichOrderItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-2-15
 */
@ToString
public class PreOrderPreDeposit implements Serializable{

    private static final long serialVersionUID = 3140141932950691131L;
    @Getter
    @Setter
    private String sellerName;

    @Getter
    @Setter
    private String shopName;

    @Getter
    @Setter
    private Long sellerId;

    @Getter
    @Setter
    private Boolean isCod;

    @Getter
    @Setter
    private RichOrderItem richOrderItem;

    @Getter
    @Setter
    private PreDeposit preDeposit;

    @Getter
    @Setter
    private Boolean eInvoice;

    @Getter
    @Setter
    private Boolean vatInvoice;

    @Getter
    @Setter
    private Boolean isEhaier;               // 是否是ehaier商家

    @Getter
    @Setter
    private Boolean stockNotEnough;         // 库存不足

    @Getter
    @Setter
    private String remainStartAt;         // 尾款起始时间




}
