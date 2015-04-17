package com.aixforce.collect.dto;

import com.aixforce.collect.model.CollectedItem;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.item.model.Item;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-13 1:06 PM  <br>
 * Author: xiao
 */
@ToString
public class CollectedItemInfo extends CollectedItem {

    private static final long serialVersionUID = -4471407965228129689L;

    @Getter
    @Setter
    private Item item;


    public static CollectedItemInfo transform(CollectedItem collectedItem, Item item) {
        CollectedItemInfo collectedItemInfo = new CollectedItemInfo();
        BeanMapper.copy(collectedItem, collectedItemInfo);
        collectedItemInfo.setItem(item);
        return collectedItemInfo;
    }
}
