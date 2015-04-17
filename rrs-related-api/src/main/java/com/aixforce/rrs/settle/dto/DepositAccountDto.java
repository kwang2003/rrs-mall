package com.aixforce.rrs.settle.dto;

import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.settle.model.DepositAccount;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-10 10:01 AM  <br>
 * Author: xiao
 */
@ToString
public class DepositAccountDto extends DepositAccount {
    private static final long serialVersionUID = -9045251473280594500L;

    @Getter
    @Setter
    private Boolean locked;             // 是否保证金余额过低被锁定

    @Getter
    @Setter
    private Long depositNeed;        // 应缴纳金额



    public static DepositAccountDto transform(DepositAccount account, Boolean locked) {
        DepositAccountDto dto = new DepositAccountDto();
        BeanMapper.copy(account, dto);
        dto.setLocked(locked);
        return dto;
    }
}
