package com.aixforce.shop.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 店铺审核通过事件
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-07 5:22 PM  <br>
 * Author: xiao
 */
@RequiredArgsConstructor
public class ApprovePassEvent {


    @Getter
    private final List<Long> ids;     // 审核通过的店铺id

}
