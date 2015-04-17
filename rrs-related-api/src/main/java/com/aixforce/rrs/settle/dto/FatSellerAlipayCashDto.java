package com.aixforce.rrs.settle.dto;

import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-04 9:27 AM  <br>
 * Author: xiao
 */
@ToString
public class FatSellerAlipayCashDto extends SellerAlipayCash {
    private static final long serialVersionUID = -684161722950563942L;

    @Getter
    @Setter
    private Long hasCashed;     // 已提现金额




    public static FatSellerAlipayCashDto transform(SellerAlipayCash sellerAlipayCash, Long hasCashed) {
        FatSellerAlipayCashDto dto = new FatSellerAlipayCashDto();
        BeanMapper.copy(sellerAlipayCash, dto);
        dto.setHasCashed(hasCashed);
        return dto;
    }

}
