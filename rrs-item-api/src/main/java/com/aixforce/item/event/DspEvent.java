package com.aixforce.item.event;

import lombok.Data;

/**
 * @Description: Dsp 推广。商品库存发生变动时记录商品id<br/>
 * @Author: Benz.Huang@goodaysh.com <br/>
 * @DATE: 2014/12/22 <br/>
 */
@Data
public class DspEvent {
    protected long itemId;
    
    public DspEvent(long itemId){
        this.itemId = itemId;

    }
}
