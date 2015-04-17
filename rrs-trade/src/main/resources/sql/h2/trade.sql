-- -----------------------------------------------------
-- Table `orders`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `orders`;

CREATE TABLE IF NOT EXISTS `orders` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `origin_id`     BIGINT      NULL  COMMENT '原始订单号',
  `buyer_id`      BIGINT      NULL  COMMENT '买家id',
  `seller_id`     BIGINT      NULL  COMMENT '卖家id',
  `business`      INT         NULL  COMMENT '订单所属行业id',
  `status`        SMALLINT    NULL  COMMENT '订单状态 0:等待买家付款,1:买家已付款,2:卖家已发货,3:交易完成,-1:买家关闭,-2:卖家关闭,-3:卖家退款',
  `type`          SMALLINT    NULL  COMMENT '交易类型 1:普通, 2:预售',
  `trade_info_id` BIGINT      NULL  COMMENT '买家收货信息',
  `deliver_fee`   INT         NULL  COMMENT '邮费',
  `payment_type`  SMALLINT    NULL  COMMENT '付款类型 1:在线支付, 2:货到付款',
  `payment_code`  VARCHAR(64) NULL  COMMENT '付款账户',
  `fee`           INT         NULL  COMMENT '订单总价',
  `channel`       VARCHAR(64) NULL  COMMENT '支付渠道',
  `is_buying`     BIT         NULL  COMMENT '是否抢购订单',
  `paid_at`       DATETIME    NULL  COMMENT '付款时间',
  `delivered_at`  DATETIME    NULL  COMMENT '发货时间',
  `done_at`       DATETIME    NULL  COMMENT '完成时间',
  `canceled_at`   DATETIME    NULL  COMMENT '交易关闭时间',
  `finished_at`   DATETIME    NULL  COMMENT '交易完成时间，交易关闭或成功',
  `created_at`    DATETIME    NULL  COMMENT '创建时间',
  `updated_at`    DATETIME    NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_orders_buyer_id ON orders (buyer_id);
CREATE INDEX idx_orders_seller_id ON orders (seller_id);
CREATE INDEX idx_orders_finished_at ON orders (finished_at);
CREATE INDEX idx_orders_updated_at ON orders (updated_at);
CREATE INDEX idx_orders_origin_id ON orders (origin_id);


CREATE INDEX idx_orders_created_at ON orders (created_at);

-- -----------------------------------------------------
-- Table `orders`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `order_extras`;

CREATE TABLE IF NOT EXISTS `order_extras` (
  `id`            BIGINT        NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `order_id`      BIGINT        NOT NULL  COMMENT '订单id',
  `buyer_notes`   VARCHAR(200)  NULL      COMMENT '买家留言',
  `invoice`       TEXT          NULL      COMMENT '发票信息(json)  type: 1:普通发票 2:增值税发票 3:电子发票, title: 抬头',
  `deliver_time`  VARCHAR(256)  NULL      COMMENT '送达时段',
  `has_logistics` BIT(1)    NULL      COMMENT '订单是否有物流信息',
  `logistics_info` TEXT         NULL      COMMENT '订单物流信息json字符串',
  `has_install` BIT(1)    NULL      COMMENT '订单是否有安装信息',
  `install_type` TINYINT(4)   NULL COMMENT '安装类型',
  `install_name` VARCHAR(64)  NULL COMMENT '安装公司名称',
  `updated_at`    DATETIME      NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_oes_order_id ON order_extras (order_id);


-- -----------------------------------------------------
-- Table `
-- `
-- -----------------------------------------------------
DROP TABLE IF EXISTS `order_items`;

CREATE TABLE IF NOT EXISTS `order_items` (
  `id`            BIGINT        NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `origin_id`     BIGINT        NULL      COMMENT '原始子订单id',
  `order_id`      BIGINT        NULL      COMMENT '订单id',
  `sku_id`        BIGINT        NULL      COMMENT 'sku id',
  `item_id`       BIGINT        NULL      COMMENT '商品id',
  `item_name`     VARCHAR(500)  NULL      COMMENT '商品名称',
  `brand_id`      BIGINT        NULL      COMMENT '品牌id',
  `business_id`   INT           NULL      COMMENT '行业id',
  `buyer_id`      BIGINT        NULL      COMMENT '买家id',
  `seller_id`     BIGINT        NULL      COMMENT '卖家id',
  `deliver_fee`   INT           NULL      COMMENT '子订单的运费（在用户未付款钱,商家可以更改运费->用于针对组合商品情况）',
  `fee`           INT           NULL      COMMENT '总费用',
  `channel`       VARCHAR(64)   NULL      COMMENT '支付渠道',
  `quantity`      INT           NULL      COMMENT '购买数量',
  `discount`      INT           NULL      COMMENT '折扣',
  `type`          INT           NULL      COMMENT '子订单类型 1:普通交易, 2:预售定金, 3:预售尾款',
  `pay_type`      INT           NULL      COMMENT '付款类型',
  `payment_code`  VARCHAR(64)   NULL      COMMENT '支付宝交易号',
  `status`        INT           NULL      COMMENT '状态',
  `has_comment`    BOOLEAN     NULL  COMMENT '是否已评价',
  `reason`        VARCHAR(255)  NULL      COMMENT '退货款理由',
  `refund_amount` INT           NULL      COMMENT '退款金额',
  `delivery_promise` VARCHAR(255) NULL    COMMENT '配送承诺',
  `refund_at`     DATETIME      NULL      COMMENT '卖家同意退款时间',
  `return_goods_at`   DATETIME  NULL      COMMENT '卖家收到退货时间',
  `is_bask_order`   BOOLEAN     NULL      COMMENT '是否已晒单',
  `created_at`    DATETIME      NULL      COMMENT '创建时间',
  `updated_at`    DATETIME      NULL      COMMENT '修改时间',
  `request_refund_at` DATETIME  NULL      COMMENT '申请退款时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_oi_order_id ON order_items (order_id);
CREATE INDEX idx_oi_seller_id ON order_items (seller_id);
CREATE INDEX idx_oi_buyer_id ON order_items (buyer_id);
CREATE INDEX idx_oi_origin_id ON order_items (origin_id);

-- -----------------------------------------------------
-- Table `deliveries`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `deliveries`;

CREATE TABLE IF NOT EXISTS `deliveries` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_id`   BIGINT      NULL     COMMENT '订单id',
  `company`    SMALLINT    NULL     COMMENT '物流公司',
  `track_code` VARCHAR(64) NULL     COMMENT '物流查询码',
  `type`       SMALLINT    NULL     COMMENT '物流类型',
  PRIMARY KEY (`id`));

-- -----------------------------------------------------
-- Table `comments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `comments`;

CREATE TABLE IF NOT EXISTS `comments` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id`     BIGINT   NULL     COMMENT '评价人id',
  `target_id`   BIGINT   NULL     COMMENT '评价对象id',
  `target_type` SMALLINT NULL     COMMENT '评价类型',
  `type`        SMALLINT NULL     COMMENT '1-差评,2-中评,3-好评',
  `content`     TEXT     NULL     COMMENT '评价内容',
  `status`      SMALLINT NULL     COMMENT '状态',
  `created_at`  DATETIME NULL     COMMENT '创建时间',
  `updated_at`  DATETIME NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_comment_target ON comments (target_type, target_id);
CREATE INDEX idx_comment_create ON comments (created_at);


-- -----------------------------------------------------
-- Table `user_trade_infos`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_trade_infos`;

CREATE TABLE IF NOT EXISTS `user_trade_infos` (
  `id`                INT           NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `user_id`           BIGINT        NOT NULL  COMMENT '所属的用户id',
  `name`              VARCHAR(32)   NOT NULL  COMMENT '收货人名称',
  `phone`             VARCHAR(32)   NOT NULL  COMMENT '收货人电话',
  `province`          VARCHAR(40)   NOT NULL  COMMENT '省份',
  `city`              VARCHAR(40)   NOT NULL  COMMENT '城市',
  `district`          VARCHAR(40)   NOT NULL  COMMENT '区县',
  `province_code`     INT           NULL      COMMENT '省份编码',
  `city_code`         INT           NULL      COMMENT '城市编码',
  `district_code`     INT           NULL      COMMENT '区县编码',
  `street`            VARCHAR(255)  NULL      COMMENT '收货人街道地址',
  `is_default`        SMALLINT      NULL      COMMENT '是否默认地址 0-不是,1-是',
  `zip`               VARCHAR(16)   NULL      COMMENT '邮编',
  `status`            SMALLINT      NOT NULL  COMMENT '1代表有效, -1代表删除',
  `created_at`        DATETIME      NOT NULL  COMMENT '创建时间',
  `updated_at`        DATETIME      NOT NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_trade_user_id ON user_trade_infos (user_id);

-- -----------------------------------------------------
-- Table `rrs_comment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_comments`;

CREATE  TABLE `rrs_comments` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键' ,
  `order_type`    SMALLINT default  null  comment '子订单类型',
  `order_item_id` BIGINT        NOT NULL COMMENT '子订单id',
  `item_id`       BIGINT        NOT NULL COMMENT '商品id',
  `buyer_id`      BIGINT        NOT NULL COMMENT '买家id',
  `shop_id`       BIGINT        NOT NULL COMMENT '店铺',
  `r_quality`     SMALLINT(1)   NULL  DEFAULT 5 COMMENT '商品质量评分',
  `r_describe`    SMALLINT(1)   NULL  DEFAULT 5 COMMENT '商品描述评分',
  `r_service`     SMALLINT(1)   NULL  DEFAULT 5 COMMENT '卖家服务评价',
  `r_express`     SMALLINT(1)   NULL  DEFAULT 5 COMMENT '物流评分',
  `comment`       VARCHAR(1024) NULL     COMMENT '评论内容',
  `comment_reply` VARCHAR(1024) NULL     COMMENT '评论回复内容',
  `is_bask_order` BOOLEAN     NULL  COMMENT '是否是晒单评论',
  `created_at`    DATETIME      NOT NULL COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX `idx_rrs_comment_item` ON rrs_comments(item_id);
CREATE INDEX `idx_rrs_comment_order` ON rrs_comments(order_item_id);
CREATE INDEX `idx_rrs_comment_buyer` ON rrs_comments(buyer_id);
CREATE INDEX `idx_rrs_comment_shop` ON rrs_comments(shop_id);

-- -----------------------------------------------------
    -- Table `logistics_infos`    物流信息(绑定到order:订单-》实现多对一)
    -- -----------------------------------------------------
    DROP TABLE IF EXISTS `logistics_infos`;

    CREATE TABLE IF NOT EXISTS `logistics_infos` (
    `id`                  BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
    `order_id`            BIGINT      NOT NULL  COMMENT '订单编号',
    `sender_id`           BIGINT      NOT NULL  COMMENT '发物流的用户编号(商家or客服)',
    `sender_name`         VARCHAR(64) NOT NULL  COMMENT '发物流的用户名称(商家or客服)',
    `send_fee`            INT         NOT NULL  COMMENT '运费费用',
    `logistics_status`    INT         NOT NULL  COMMENT '物流状态（可以与settlement的trade_status状态关联，1:已发货，2:已收货,3:买家拒绝接收）',
    `company_name`        VARCHAR(64) NOT NULL  COMMENT '物流公司名称',
    `freight_note`        VARCHAR(32) NOT NULL  COMMENT '物流运单编号',
    `created_at`          DATETIME    NULL      COMMENT '创建时间',
    `updated_at`          DATETIME    NULL      COMMENT '修改时间',
    PRIMARY KEY (`id`));
    CREATE INDEX idx_logistics_infos_order_id ON logistics_infos (order_id);


    -- -----------------------------------------------------
    -- Table `logistics_revert`    退货物流信息(绑定到order:订单-》实现多对一)
    -- -----------------------------------------------------
    DROP TABLE IF EXISTS `logistics_reverts`;

    CREATE TABLE IF NOT EXISTS `logistics_reverts` (
    `id`                  BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
    `order_item_id`       BIGINT      NOT NULL  COMMENT '子订单编号',
    `buyer_id`            BIGINT      NOT NULL  COMMENT '买家编号',
    `buyer_name`          VARCHAR(64) NOT NULL  COMMENT '买家用户名称',
    `send_fee`            INT         NOT NULL  COMMENT '运费费用',
    `logistics_status`    INT         NOT NULL  COMMENT '物流状态（1:已发货，2:已收货,3:卖家拒绝接收）',
    `company_name`        VARCHAR(64) NOT NULL  COMMENT '物流公司名称',
    `freight_note`        VARCHAR(32) NOT NULL  COMMENT '物流运单编号',
    `created_at`          DATETIME    NULL      COMMENT '创建时间',
    `updated_at`          DATETIME    NULL      COMMENT '修改时间',
    PRIMARY KEY (`id`));


  -- -----------------------------------------------------
  -- Table `freight_models`    运费模板
  -- -----------------------------------------------------
  DROP TABLE IF EXISTS `freight_models`;

  CREATE TABLE IF NOT EXISTS `freight_models` (
  `id`                  BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `seller_id`           BIGINT      NOT NULL  COMMENT '商家编号',
  `model_name`          VARCHAR(32) NOT NULL  COMMENT '模型名称',
  `count_way`           SMALLINT    NOT NULL  COMMENT '计价方式（1:件数,2:尺寸,3:重量->后两个后期扩展）',
  `cost_way`            SMALLINT    NOT NULL  COMMENT '费用方式（1:买家承担,2:卖家承担）',
  `first_amount`        INT         NULL      COMMENT '首批数量在N范围内',
  `first_fee`           INT         NULL      COMMENT '首批价格多少',
  `add_amount`          INT         NULL      COMMENT '每增加N数量',
  `add_fee`             INT         NULL      COMMENT '增加N价格',
  `status`              SMALLINT    NOT NULL  DEFAULT 1 COMMENT '运费模板状态，是否已禁用（逻辑删除）',
  `special_exist`       SMALLINT    NULL      COMMENT '是否存在特殊地区设定(0:不存在，1:存在.方便select)',
  `created_at`          DATETIME    NULL      COMMENT '创建时间',
  `updated_at`          DATETIME    NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`));

insert into freight_models ( seller_id, model_name, count_way, cost_way, first_amount, first_fee, add_amount, add_fee, special_exist, created_at, updated_at )
values (1,'model1', 1, 1, 10, 10, 100, 10, 1, now(), now()),(1,'model2',1, 2, 10, 10, 100, 10, 1, now(), now()),
(1,'model3',1, 1, 10, 10, 100, 10, 1, now(), now()),(2,'model4',1, 1, 10, 10, 100, 10, 1, now(), now()),(3,'model4',1, 1, 10, 10, 100, 10, 1, now(), now());


  -- -----------------------------------------------------
  -- Table `logistics_specials`    特殊地区收费信息
  -- -----------------------------------------------------
  DROP TABLE IF EXISTS `logistics_specials`;

  CREATE TABLE IF NOT EXISTS `logistics_specials` (
  `id`                  BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `model_id`            BIGINT      NOT NULL  COMMENT '运费模板编号',
  `address_model`       VARCHAR(256)NOT NULL  COMMENT '存储一个地区编号的json字段',
  `first_amount`        INT         NOT NULL  COMMENT '首批数量在N范围内',
  `first_fee`           INT         NOT NULL  COMMENT '首批价格多少',
  `add_amount`          INT         NOT NULL  COMMENT '每增加N数量',
  `add_fee`             INT         NOT NULL  COMMENT '增加N价格',
  `created_at`          DATETIME    NULL      COMMENT '创建时间',
  `updated_at`          DATETIME    NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`));

  insert into logistics_specials ( model_id, address_model, first_amount, first_fee, add_amount, add_fee, created_at, updated_at )
  values (1, '{p:{100001,10002}}', 10, 10, 100, 10, now(), now()),(1, '{p:{100001,10002}}', 10, 10, 100, 10, now(), now()),
  (1, '{p:{100001,10002}}', 10, 10, 100, 10, now(), now()),(2, '{p:{100001,10002}}', 10, 10, 100, 10, now(), now()),
  (2, '{p:{100001,10002}}', 10, 10, 100, 10, now(), now()),(3, '{p:{100001,10002}}', 10, 10, 100, 10, now(), now());


-- -----------------------------------------------------
-- Table `user_vat_invoices`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_vat_invoices` ;

CREATE  TABLE IF NOT EXISTS `user_vat_invoices` (
  `id`                    BIGINT          NOT NULL  AUTO_INCREMENT COMMENT '自增主键' ,
  `user_id`               BIGINT          NOT NULL  COMMENT '用户标识',
  `company_name`          VARCHAR(127)    NOT NULL  COMMENT '公司名称',
  `tax_register_no`       VARCHAR(32)     NOT NULL  COMMENT '税务登记号',
  `register_address`      VARCHAR(127)    NOT NULL  COMMENT '注册地址',
  `register_phone`        VARCHAR(16)     NOT NULL  COMMENT '注册电话',
  `register_bank`         VARCHAR(127)    NOT NULL  COMMENT '注册银行',
  `bank_account`          VARCHAR(32)     NOT NULL  COMMENT '银行帐号',
  `tax_certificate`       VARCHAR(255)    NULL      COMMENT '税务登记证',
  `taxpayer_certificate`  VARCHAR(255)    NULL      COMMENT '一般纳税人证书',
  `created_at`            DATETIME        NULL      COMMENT '创建时间',
  `updated_at`            DATETIME        NULL      COMMENT '更新时间',
  PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX idx_uvi_user_id_uniq on user_vat_invoices(user_id);

DROP TABLE IF EXISTS `delivery_methods`;

CREATE TABLE IF NOT EXISTS `delivery_methods` (
  `id`                    BIGINT        NOT NULL    AUTO_INCREMENT COMMENT '自增主键',
  `name`                  VARCHAR(255)  NOT NULL    COMMENT   '配送方式名称',
  `status`                SMALLINT      NOT NULL    COMMENT   '状态 1启用，-1停用，-2删除;创建时默认状态为停用',
  `type`                  SMALLINT      NOT NULL    COMMENT   '类型，1送达时段，2送达承诺',
  `created_at`            DATETIME        NULL      COMMENT '创建时间',
  `updated_at`            DATETIME        NULL      COMMENT '更新时间',
  PRIMARY KEY (`id`)
);

-- -----------------------------------------------------
-- Table `bask_orders` 晒单
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bask_orders`;

CREATE TABLE IF NOT EXISTS `bask_orders` (
  `id`              BIGINT       NULL AUTO_INCREMENT COMMENT '自增主键',
  `item_id`         BIGINT       NULL           COMMENT '商品id',
  `order_comment_id`   BIGINT       NULL           COMMENT '评论id',
  `order_item_id`   BIGINT       NULL           COMMENT '子订单id',
  `content`         VARCHAR(1024) NOT   NULL     COMMENT '内容',
  `pic`             TEXT         NOT   NULL     COMMENT '图片url',
  `sku_name`        VARCHAR(500)        NULL     COMMENT 'sku名字',
  `user_name`       VARCHAR(500)        NULL     COMMENT '买家名字',
  `created_at`      DATETIME     NULL           COMMENT '创建时间',
  `updated_at`      DATETIME     NULL           COMMENT '修改时间',
  PRIMARY KEY (`id`));



-- -----------------------------------------------------
-- Table `collected_items` 商品收藏
-- -----------------------------------------------------
DROP TABLE IF EXISTS `collected_items`;

CREATE TABLE IF NOT EXISTS `collected_items` (
  `id`                  BIGINT       NULL AUTO_INCREMENT COMMENT '自增主键',
  `buyer_id`            BIGINT       NULL           COMMENT '用户id',
  `item_id`             BIGINT       NULL           COMMENT '商品id',
  `item_name_snapshot`  VARCHAR(500) NULL           COMMENT '商品名称(快照)',
  `created_at`          DATETIME     NULL           COMMENT '创建时间',
  `updated_at`          DATETIME     NULL           COMMENT '修改时间',
  PRIMARY KEY (`id`));



CREATE INDEX idx_ci_buyer_id ON collected_items (buyer_id);


-- -----------------------------------------------------
-- Table `collected_shops` 店铺收藏
-- -----------------------------------------------------
DROP TABLE IF EXISTS `collected_shops`;

CREATE TABLE IF NOT EXISTS `collected_shops` (
  `id`                  BIGINT       NULL AUTO_INCREMENT COMMENT '自增主键',
  `buyer_id`            BIGINT       NULL           COMMENT '用户id',
  `seller_id`           BIGINT       NULL           COMMENT '商家id',
  `shop_id`             BIGINT       NULL           COMMENT '店铺id',
  `shop_name_snapshot`  VARCHAR(500) NULL           COMMENT '店铺名称(快照)',
  `shop_logo_snapshot`  VARCHAR(255) NULL           COMMENT '店铺logo(快照)',
  `created_at`          DATETIME     NULL           COMMENT '创建时间',
  `updated_at`          DATETIME     NULL           COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_cs_buyer_id ON collected_shops (buyer_id);

-- 物流快递信息表
CREATE TABLE `express_infos` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '快递名称',
  `code` varchar(64) DEFAULT '' COMMENT '快递代码',
  `interface_name` varchar(64) NOT NULL DEFAULT '' COMMENT '接口名称',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态, 1启用,0禁用,-1逻辑删除',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='物流快递信息表';
CREATE UNIQUE INDEX idx_express_infos_name ON express_infos(name);

-- 物流安装信息表:
CREATE TABLE `install_infos` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL COMMENT '安装公司名称',
  `code` varchar(64) DEFAULT NULL COMMENT '安装公司代码',
  `interface_name` varchar(64) DEFAULT NULL COMMENT '接口名称',
  `status` tinyint(4) NOT NULL COMMENT '状态,1启用,0停用,-1删除',
  `type` tinyint(4) NOT NULL COMMENT '安装类型,0净水,1家电,2家具,3建材',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='物流安装信息表';
CREATE UNIQUE INDEX idx_install_infos_name ON install_infos(name);

-- 订单物流信息表
CREATE TABLE `order_logistics_infos` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `express_name` varchar(64) DEFAULT NULL COMMENT '快递名称',
  `express_code` varchar(64) DEFAULT NULL COMMENT '快递代码',
  `express_no` varchar(32) DEFAULT NULL COMMENT '快递单号',
  `type` tinyint(4) DEFAULT NULL COMMENT '物流类型',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='订单物流信息表';
CREATE UNIQUE INDEX idx_order_logistics_infos_order_id ON order_logistics_infos(order_id);

-- 订单安装信息表
CREATE TABLE `order_install_infos` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `time` varchar(32) DEFAULT '' COMMENT '时间点',
  `context` text COMMENT '信息',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='订单安装信息表';
CREATE INDEX idx_order_install_infos_order_id ON order_install_infos(order_id);


-- ----------------------------
-- Table structure for `order_job_day_config`
-- ----------------------------
CREATE TABLE `order_job_day_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sku_id` bigint(20) NOT NULL COMMENT '商品skuID',
  `day` tinyint(10) NOT NULL COMMENT '商品自动收货的间隔时间设定',
  `created_At` datetime DEFAULT NULL COMMENT '新建时间',
  `updated_At` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_job_day_config_sku_id_UNIQUE` (`sku_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of order_job_day_config
-- ----------------------------

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `order_job_day`
-- ----------------------------
CREATE TABLE `order_job_day` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `status` int(1) DEFAULT '0' COMMENT '0：未执行job，1：已执行job',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '商品skuId',
  `over_day` date NOT NULL DEFAULT '0000-00-00' COMMENT '自动收货时间',
  `created_At` datetime DEFAULT NULL COMMENT '新建时间',
  `updated_At` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `over_day_index` (`over_day`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

