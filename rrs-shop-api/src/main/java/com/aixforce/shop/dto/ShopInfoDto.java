package com.aixforce.shop.dto;

import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.model.ShopPaperwork;
import com.rrs.arrivegift.model.ShopGiftConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Desc:用于前台交互的数据（包含商店详细信息&商店证书等信息）
 * Mail:v@terminus.io
 * author:Michael Zhao
 * Date:2014-05-12.
 */
@ToString
public class ShopInfoDto implements Serializable {

    private static final long serialVersionUID = -5961532825344045613L;

    @Setter
    @Getter
    private Shop shop;                      // 店铺信息

    @Setter
    @Getter
    private ShopPaperwork shopPaperwork;    // 店铺证书等信息

    @Getter
    @Setter
    private ShopExtra extra;                // 扩展信息
    
    @Getter
    @Setter	
    private ShopGiftConfig shopGiftConfig;   // 到店有礼
   
    @Getter
    @Setter
    private Boolean isEhaier;               //是否是ehaier商家
    
    @Getter
    @Setter
    private String isSms;               //是否启用短信通知 1:启用 2：停用
    
    @Getter
    @Setter
    private String displayStorePay;     //显示到店支付方式 0:不显示 1：显示
}
