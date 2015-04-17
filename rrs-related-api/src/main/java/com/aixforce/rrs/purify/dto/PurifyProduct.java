package com.aixforce.rrs.purify.dto;

import com.aixforce.item.model.Item;
import com.aixforce.rrs.purify.model.PurifyAssembly;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Desc:封装后的商品信息对象
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-14.
 */
@ToString
public class PurifyProduct extends Item {
    @Getter
    @Setter
    private List<PurifyAssembly> purifyAssemblyList;        //用于最后货物选定页面现实各种组件的信息
}
