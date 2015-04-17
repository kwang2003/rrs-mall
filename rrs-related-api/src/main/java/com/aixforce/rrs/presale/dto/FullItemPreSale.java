package com.aixforce.rrs.presale.dto;


import com.aixforce.item.dto.FullItem;
import com.aixforce.rrs.presale.model.PreSale;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-2-13
 */
@ToString
public class FullItemPreSale implements Serializable{

    private static final long serialVersionUID = -2757333834365076103L;
    @Getter
    @Setter
    private FullItem fullItem;

    @Getter
    @Setter
    private PreSale preSale;
}
