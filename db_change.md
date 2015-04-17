### 扩展商品组合套餐中商品原价字段

    ALTER TABLE `item_bundles` ADD COLUMN `original_price` INT NOT NULL COMMENT '原价';


### 增加订单扩展信息字段

    ALTER TABLE `order_extras` ADD COLUMN `deliver_time` VARCHAR(256) NULL COMMENT '送达时段' AFTER `invoice`;
    ALTER TABLE `order_extras` ADD COLUMN `has_logistics` bit(1) DEFAULT NULL COMMENT '是否有物流信息' AFTER `deliver_time`;
    ALTER TABLE `order_extras` ADD COLUMN `logistics_info` TEXT DEFAULT NULL COMMENT '是否有物流信息' AFTER `has_logistics`;
    ALTER TABLE `order_extras` ADD COLUMN `has_install` bit(1) DEFAULT NULL COMMENT '是否有物流快递信息' AFTER `logistics_info`;
    ALTER TABLE `order_extras` ADD COLUMN `install_type` tinyint(4) DEFAULT NULL COMMENT '是否有物流快递信息' AFTER `has_install`;
    ALTER TABLE `order_extras` ADD COLUMN `install_name` varchar(64) DEFAULT NULL COMMENT '是否有物流快递信息' AFTER `install_type`;

### 订单物流相关的表
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

### items中添加 配送方式id

    ALTER TABLE `items` ADD COLUMN `delivery_method_id` BIGINT NULL COMMENT '配送方式id' AFTER `freight_modelName`;
    
### orderItems中添加 配送承诺

    ALTER TABLE `order_items` ADD COLUMN `delivery_promise` VARCHAR(255) NULL COMMENT '配送承诺' AFTER `refund_amount`;
    
    
### 抢购活动定义表
    DROP TABLE IF EXISTS `buying_activity_defs`;
    CREATE TABLE IF NOT EXISTS `buying_activity_defs`(
        `id`                  BIGINT        NOT NULL  AUTO_INCREMENT,
        `activity_name`       varchar(128)      NULL  COMMENT '活动标题',
        `activity_start_at`   DATETIME          NULL  COMMENT '活动开始时间',
        `activity_end_at`     DATETIME      NULL      COMMENT '活动结束时间',
        `order_start_at`      DATETIME      NULL      COMMENT '下单开始时间',
        `order_end_at`        DATETIME      NULL      COMMENT '下单结束时间',
        `pay_limit`           INT           NULL      COMMENT '付款时限',
        `status`              SMALLINT      NULL      COMMENT '状态(1->待发布;2->已发布,代运行;3->正在运行;4->已结束;5->已中止)',
        `created_at`          DATETIME      NULL      COMMENT '创建时间',
        `updated_at`          DATETIME      NULL      COMMENT '修改时间',
        PRIMARY KEY (`id`)
    );
    
### 抢购活动和商品关联表
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
      `is_storage`          BOOLEAN       NULL      COMMENT '是否支持分仓',
      `created_at`          DATETIME  NULL      COMMENT '创建时间',
      `updated_at`          DATETIME  NULL      COMMENT '修改时间',
      PRIMARY KEY (`id`)
    );
    
    CREATE INDEX idx_buying_activity_id ON buying_activity_items (buying_activity_id);
    
    
### 模拟订单表
    DROP TABLE IF EXISTS `buying_temp_orders`;
    CREATE TABLE IF NOT EXISTS `buying_temp_orders`(
      `id`                 BIGINT        NOT NULL  AUTO_INCREMENT,
      `order_id`           BIGINT            NULL  COMMENT '订单id',
      `buying_activity_id` BIGINT        NOT NULL  COMMENT '抢购活动定义id',
      `sku_id`              BIGINT        NOT NULL  COMMENT 'skuid',
      `buying_price`       INT           NOT NULL  COMMENT '抢购价',
      `sku_quantity`       INT           NOT NULL  COMMENT 'sku购买数量',
      `sku_attribute_json` varchar(512)  NOT NULL  COMMENT 'sku销售属性',
      `item_id`            BIGINT        NOT NULL  COMMENT '商品id',
      `item_image`         varchar(256)  NOT NULL  COMMENT '商品图片',
      `item_name`           varchar(128)  NOT NULL  COMMENT '商品名字',
      `trade_info_id`      BIGINT        NOT NULL  COMMENT '收货信息id',
      `created_at`         DATETIME      NULL      COMMENT '创建时间',
      `updated_at`         DATETIME      NULL      COMMENT '修改时间',
      PRIMARY KEY (`id`)
    );
    
### 抢购活动和订单关联表
    DROP TABLE IF EXISTS `buying_order_record`;
    CREATE TABLE IF NOT EXISTS `buying_order_record`(
        `id`                  BIGINT    NOT NULL  AUTO_INCREMENT,
        `order_id`            BIGINT    NOT NULL  COMMENT '订单id',
        `item_id`            BIGINT     NOT NULL  COMMENT '商品id',
        `buying_activity_id`  BIGINT    NOT NULL  COMMENT '抢购活动定义id',
        `item_origin_price`   INT       NOT NULL  COMMENT '商品原价',
        `item_buying_price`   INT       NOT NULL  COMMENT '商品抢购价',
        `discount`            INT       NULL      COMMENT '减免金额',
        `created_at`          DATETIME  NULL      COMMENT '创建时间',
        `updated_at`          DATETIME  NULL      COMMENT '修改时间',
        PRIMARY KEY (`id`)
    );
    
### 在订单关联表中增加买家id 购买数量

    ALTER TABLE `buying_order_record` ADD COLUMN `buyer_id` BIGINT NOT NULL COMMENT '买家id' AFTER `item_id`;    
    ALTER TABLE `buying_order_record` ADD COLUMN `seller_id` BIGINT NOT NULL COMMENT '卖家id' AFTER `buyer_id`;    
    ALTER TABLE `buying_order_record` ADD COLUMN `quantity` INT    NOT NULL COMMENT '购买数量' AFTER `buyer_id`;    

### 在抢购活动中添加店铺id店铺name 商家id 商家name    
    ALTER TABLE `buying_activity_defs` ADD COLUMN `seller_id` BIGINT NULL COMMENT '商家id' AFTER `status`;    
    ALTER TABLE `buying_activity_defs` ADD COLUMN `seller_name`varchar(64) NULL COMMENT '商家名称' AFTER `seller_id`;    
    ALTER TABLE `buying_activity_defs` ADD COLUMN `shop_id`    BIGINT NULL COMMENT '店铺id' AFTER `seller_name`;    
    ALTER TABLE `buying_activity_defs` ADD COLUMN `shop_name` VARCHAR(500) NULL COMMENT '店铺名称' AFTER `shop_id`;    
    ALTER TABLE `buying_activity_defs` ADD COLUMN `business_id` SMALLINT NULL COMMENT '频道id' AFTER `shop_name`;    
    
### 虚拟订单中加入用户id字段 卖家id 店铺id

    ALTER TABLE `buying_temp_orders` ADD COLUMN `buyer_id` BIGINT NOT NULL COMMENT '用户id' AFTER `sku_id`;   
    ALTER TABLE `buying_temp_orders` ADD COLUMN `seller_id` BIGINT NOT NULL COMMENT '卖id' AFTER `buyer_id`;   
    ALTER TABLE `buying_temp_orders` ADD COLUMN `shop_id` BIGINT NOT NULL COMMENT '店铺id' AFTER `seller_id`;   
    ALTER TABLE `buying_temp_orders` ADD COLUMN `status` SMALLINT NOT NULL COMMENT '状态(0->待下单;1->已下单;-1->已取消)' AFTER `trade_info_id`; 
    ALTER TABLE `buying_temp_orders` ADD COLUMN `order_created_at` DATETIME  NULL COMMENT '订单创建时间' AFTER `status`; 
    ALTER TABLE `buying_temp_orders` ADD COLUMN `region_id` INT NOT NULL COMMENT '区级别的地区id' AFTER `order_created_at`; 
    ALTER TABLE `buying_temp_orders` ADD COLUMN `order_start_at` DATETIME  NULL COMMENT '下单开始时间' AFTER `region_id`; 
    ALTER TABLE `buying_temp_orders` ADD COLUMN `order_end_at` DATETIME  NULL COMMENT '下单结束时间' AFTER `order_start_at`; 
    ALTER TABLE `buying_temp_orders` ADD COLUMN `pay_limit` INT  NULL COMMENT '付款时限' AFTER `order_end_at`; 
    
### 订单表中加入标记位表示是否是抢购订单

    ALTER TABLE `orders` ADD COLUMN `is_buying` BIT NULL COMMENT '是否抢购订单' AFTER `channel`;
    
    
    
    
    

    
