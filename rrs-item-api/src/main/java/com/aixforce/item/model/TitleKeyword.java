package com.aixforce.item.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;
import java.util.Date;

/**
 * @desc 保存用户在页面上使用过的搜索关键字
 * Created by wanggen on 14-6-30.
 */
@ToString
public class TitleKeyword implements Serializable{

    private static final long serialVersionUID = -2571681298386118948L;
    @Getter
    @Setter
    private Long id;            //自增序列ID

    @Getter
    @Setter
    private Long nameId;        //标识ID

    @Getter
    @Setter
    private String path;        //类目url path

    @Getter
    @Setter
    private String title;       //标题 or 页面标识

    @Getter
    @Setter
    private String keyword;     //与该标题相关的搜索关键字

    @Getter
    @Setter
    private String desc;        //该该搜索关键字描述信息

    @Getter
    @Setter
    private String friendLinks; //友情链接 json 存储

    @Getter
    @Setter
    private Map<String, String> friendLinkMap; //friendLinks 转成map后的对象，不在DB中存储

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;

}
