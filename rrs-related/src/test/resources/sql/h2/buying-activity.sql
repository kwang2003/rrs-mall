DROP TABLE IF EXISTS `buying_activity_defs`;
-- 抢购活动定义表
CREATE TABLE IF NOT EXISTS `buying_activity_defs`(
    `id`                  BIGINT        NOT NULL  AUTO_INCREMENT,
    `activity_name`       varchar(128)      NULL  COMMENT '活动标题',
    `activity_start_at`   DATETIME          NULL  COMMENT '活动开始时间',
    `activity_end_at`     DATETIME      NULL      COMMENT '活动结束时间',
    `order_start_at`      DATETIME      NULL      COMMENT '下单开始时间',
    `order_end_at`        DATETIME      NULL      COMMENT '下单结束时间',
    `pay_limit`           INT           NULL      COMMENT '付款时限',
    `status`              SMALLINT      NULL      COMMENT '状态(1->待发布;2->已发布,代运行;3->正在运行;4->已结束;5->已中止)',
    `shop_id`             BIGINT        NULL      COMMENT '店铺id',
    `shop_name`           VARCHAR(500)  NULL      COMMENT '商品所在店铺名称',
    `seller_id`           BIGINT        NULL      COMMENT '商家id',
    `seller_name`         varchar(64)   NULL      COMMENT '商家账户',
    `business_id`         SMALLINT      NULL      COMMENT '频道id',
    `created_at`          DATETIME      NULL      COMMENT '创建时间',
    `updated_at`          DATETIME      NULL      COMMENT '修改时间',
    PRIMARY KEY (`id`)
);


-- 抢购活动和商品关联表
DROP TABLE IF EXISTS `buying_activity_items`;

CREATE TABLE IF NOT EXISTS `buying_activity_items`(
  `id`                  BIGINT    NOT NULL  AUTO_INCREMENT,
  `item_id`             BIGINT    NOT NULL  COMMENT '商品id',
  `buying_activity_id`  BIGINT    NOT NULL  COMMENT '抢购活动定义id',
  `item_origin_price`   INT       NOT NULL  COMMENT '商品原价',
  `item_buying_price`   INT       NOT NULL  COMMENT '商品抢购价',
  `discount`            INT       NULL      COMMENT '减免金额',
  `buy_limit`           INT       NULL      COMMENT '每个用户限购数',
  `fake_sold_quantity`  INT       NULL      COMMENT '虚拟销量',
  `is_storage`          BOOLEAN   NULL      COMMENT '是否支持分仓',
  `created_at`          DATETIME  NULL      COMMENT '创建时间',
  `updated_at`          DATETIME  NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

CREATE INDEX idx_buying_activity_id ON buying_activity_items (buying_activity_id);


-- 模拟订单表
DROP TABLE IF EXISTS `buying_temp_orders`;
CREATE TABLE IF NOT EXISTS `buying_temp_orders`(
  `id`                 BIGINT        NOT NULL  AUTO_INCREMENT,
  `order_id`           BIGINT            NULL  COMMENT '订单id',
  `buying_activity_id` BIGINT        NOT NULL  COMMENT '抢购活动定义id',
  `sku_id`             BIGINT        NOT NULL  COMMENT 'skuid',
  `buyer_id`           BIGINT        NOT NULL  COMMENT '用户id',
  `seller_id`          BIGINT        NOT NULL  COMMENT '卖家id',
  `shop_id`            BIGINT        NOT NULL  COMMENT '店铺id',
  `buying_price`       INT           NOT NULL  COMMENT '抢购价',
  `sku_quantity`       INT           NOT NULL  COMMENT 'sku购买数量',
  `sku_attribute_json` varchar(512)  NOT NULL  COMMENT 'sku销售属性',
  `item_id`            BIGINT        NOT NULL  COMMENT '商品id',
  `item_image`         varchar(256)  NOT NULL  COMMENT '商品图片',
  `item_name`          varchar(128)  NOT NULL  COMMENT '商品名字',
  `trade_info_id`      BIGINT        NOT NULL  COMMENT '收货信息id',
  `status`             SMALLINT      NOT NULL      COMMENT '状态(0->待下单;1->已下单;-1->已取消，-2 已过期)',
  `order_created_at`   DATETIME      NULL      COMMENT '订单创建时间',
  `region_id`          INT           NOT NULL  COMMENT '区级别的地区id',
  `order_start_at`     DATETIME      NULL      COMMENT '下单开始时间',
  `order_end_at`       DATETIME      NULL      COMMENT '下单结束时间',
  `pay_limit`           INT           NULL      COMMENT '付款时限',
  `created_at`         DATETIME      NULL      COMMENT '创建时间',
  `updated_at`         DATETIME      NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

-- 抢购活动和订单关联表
DROP TABLE IF EXISTS `buying_order_record`;
CREATE TABLE IF NOT EXISTS `buying_order_record`(
  `id`                  BIGINT    NOT NULL  AUTO_INCREMENT,
  `order_id`            BIGINT    NOT NULL  COMMENT '订单id',
  `item_id`             BIGINT    NOT NULL  COMMENT '商品id',
  `buyer_id`            BIGINT    NOT NULL  COMMENT '买家id',
  `seller_id`           BIGINT    NOT NULL  COMMENT '卖家id',
  `quantity`            INT       NOT NULL  COMMENT '购买数量',
  `buying_activity_id`  BIGINT    NOT NULL  COMMENT '抢购活动定义id',
  `item_origin_price`   INT       NOT NULL  COMMENT '商品原价',
  `item_buying_price`   INT       NOT NULL  COMMENT '商品抢购价',
  `discount`            INT       NULL      COMMENT '减免金额',
  `created_at`          DATETIME  NULL      COMMENT '创建时间',
  `updated_at`          DATETIME  NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`)
);













