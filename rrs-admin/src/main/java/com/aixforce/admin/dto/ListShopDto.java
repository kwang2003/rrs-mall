package com.aixforce.admin.dto;

import com.aixforce.shop.dto.ShopDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-1-24
 */
@ToString
public class ListShopDto extends ShopDto {
    private static final long serialVersionUID = -8264797353524458255L;
    @Getter
    @Setter
    private String subDomain;
}
