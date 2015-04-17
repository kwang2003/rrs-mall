package com.aixforce.trade.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Desc:晒单
 * Created by songrenfei
 * Date:2014-07-03.
 */
@ToString
@EqualsAndHashCode
public class BaskOrder implements Serializable {


    private static final long serialVersionUID = 5097370625039938819L;

    @Getter
	@Setter
	private Long id;

    @Getter
	@Setter
	private Long orderCommentId;   //评论id

    @Getter
    @Setter
    private Long orderItemId;   //子订单id

    @Getter
	@Setter
	private Long itemId;    //商品id

    @Getter
    @Setter
    private String content;// 内容


    @Getter
    @Setter
    private String pic;// 图片

    @Getter
    @Setter
    private String skuName;// 商品名字

    @Getter
    @Setter
    private String userName;// 买家名字

    @Getter
    @Setter
    private Date createdAt;     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;     // 修改时间


    /**
     * no db
     */
    @Getter
    @Setter
    private List<String> pics;// 图片


}
