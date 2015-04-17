package com.aixforce.open.dto;

import com.aixforce.item.model.Sku;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by neusoft on 14-8-7.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RichSku extends Sku {

    // 主图片
    private String mainImage;

    // 商品名称
    private String itemName;
}
