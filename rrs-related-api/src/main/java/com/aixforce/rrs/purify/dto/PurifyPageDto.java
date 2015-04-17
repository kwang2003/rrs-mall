package com.aixforce.rrs.purify.dto;

import com.aixforce.rrs.purify.model.PurifyAssembly;
import com.aixforce.rrs.purify.model.PurifyCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Desc:页面数据信息对象
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-11.
 */
@ToString
public class PurifyPageDto implements Serializable {
    private static final long serialVersionUID = 6512944588653428481L;

    @Getter
    @Setter
    private List<PurifyCategory> purifyCategoryList;        //返回给前台的用户步骤信息列表

    @Getter
    @Setter
    private List<PurifyAssembly> purifyAssemblyList;        //返回给前台的当前类目中的全部组件信息列表

    @Getter
    @Setter
    private PurifyProduct purifyProduct;                    //商品对象
}
