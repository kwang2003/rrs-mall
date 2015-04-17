package com.aixforce.rrs.purify.dto;

import com.aixforce.rrs.purify.model.PurifyAssembly;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Desc:组件实体类对象
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-11.
 */
@ToString
public class PurifyAssemblyDto extends PurifyAssembly {
    @Getter
    @Setter
    private Long parentId;      //用于在创建组件是绑定组件之间的关系

    @Getter
    @Setter
    private Long productId;     //创建组件时可能会和最终的商品绑定关系

    @Getter
    @Setter
    private Long minTotal;      //最低价格

    @Getter
    @Setter
    private Long maxTotal;      //最高价格
}
