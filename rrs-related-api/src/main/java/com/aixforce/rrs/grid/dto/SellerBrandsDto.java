package com.aixforce.rrs.grid.dto;

import com.aixforce.rrs.grid.model.UnitBrand;
import com.aixforce.rrs.grid.model.UnitSeller;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 4/26/14
 * Time: 10:17
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
@ToString
public class SellerBrandsDto implements Serializable {
    private static final long serialVersionUID = 2017486165513884801L;

    @Getter
    @Setter
    private UnitSeller seller = new UnitSeller();

    @Getter
    @Setter
    List<UnitBrand> brands;


}
