package com.aixforce.rrs.settle.dto;

import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.settle.model.AlipayCash;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-04 9:34 AM  <br>
 * Author: xiao
 */
@ToString
public class FatAlipayCashDto extends AlipayCash {

    private static final long serialVersionUID = 2498904359904165277L;

    @Getter
    @Setter
    private Long hasCashed;     // 已提现金额


    public static FatAlipayCashDto transform(AlipayCash alipayCash, Long hasCashed) {
        FatAlipayCashDto dto = new FatAlipayCashDto();
        BeanMapper.copy(alipayCash, dto);
        dto.setHasCashed(hasCashed);
        return dto;
    }
}
