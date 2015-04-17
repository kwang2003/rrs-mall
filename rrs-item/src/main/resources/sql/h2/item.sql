-- -----------------------------------------------------
-- Table `items`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `items` ;

CREATE  TABLE IF NOT EXISTS `items` (
  `id`            BIGINT        NOT NULL  AUTO_INCREMENT,
  `spu_id`        BIGINT        NULL      COMMENT '标准商品单元id',
  `user_id`       BIGINT        NULL      COMMENT '用户id',
  `shop_id`       BIGINT        NULL      COMMENT '店铺id',
  `brand_id`      INT           NULL      COMMENT '品牌id',
  `name`          VARCHAR(500)  NULL      COMMENT '商品名称',
  `shop_name`     VARCHAR(500)  NULL      COMMENT '商品所在店铺名称',
  `promotion_title` VARCHAR(127)   NULL      COMMENT '营销语',
  `main_image`    VARCHAR(127)  NULL      COMMENT '主图片',
  `province`      INT           NULL      COMMENT '省、直辖市编码',
  `city`          INT           NULL      COMMENT '城市编码',
  `trade_type`    SMALLINT      NULL      COMMENT '交易类型',
  `status`        SMALLINT      NULL      COMMENT '商品状态 0：未上架，1：上架，，-1：下架，-2：冻结' ,
  `region`        TEXT          NULL      COMMENT '商品所属区域' ,
  `buy_limit`     INT           NULL      COMMENT '限购数量' ,
  `quantity`      INT           NULL      COMMENT '数量',
  `sold_quantity`    INT           NULL      COMMENT '售出数量',
  `price`            INT           NULL      COMMENT '实际价格（优惠后）',
  `origin_price`     INT           NULL      COMMENT '原始单价（优惠前）',
  `on_shelf_at`      DATETIME      NULL      COMMENT '上架时间',
  `off_shelf_at`     DATETIME      NULL      COMMENT '下架时间',
  `freight_size`     BIGINT      NULL      COMMENT '商品的尺寸数据（现在先不考虑，但为了后期方便先设置）',
  `freight_weight`   BIGINT      NULL      COMMENT '商品的重量数据（现在先不考虑，但为了后期方便先设置）',
  `freight_modelId`  BIGINT      NULL      COMMENT '运费模板编号',
  `freight_modelName`VARCHAR(32) NULL      COMMENT '模型名称',
  `delivery_method_id`  BIGINT   NULL      COMMENT '配送方式id',
  `created_at`       DATETIME      NULL      COMMENT '创建日期',
  `updated_at`       DATETIME      NULL      COMMENT '更新日期',
  PRIMARY KEY (`id`) );

create index idx_items_user_id on items(user_id);
create index idx_items_spu_id on items(spu_id);
create index idx_items_updated_at on items(updated_at);

-- -----------------------------------------------------
-- Table `item_details`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `item_details` ;

CREATE  TABLE IF NOT EXISTS `item_details` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `item_id`         BIGINT        NULL  COMMENT '商品id',
  `image1`          VARCHAR(127)  NULL  COMMENT '图片地址1',
  `image2`          VARCHAR(127)  NULL  COMMENT '图片地址2',
  `image3`          VARCHAR(127)  NULL  COMMENT '图片地址3',
  `image4`          VARCHAR(127)  NULL  COMMENT '图片地址4',
  `freight_size`    INT           NULL  COMMENT '商品体积',
  `freight_weight`  INT           NULL  COMMENT '商品重量',
  `packing_list`    TEXT          NULL  COMMENT '包装清单',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_itdtl_item_id_uniq` (`item_id` ASC));

-- -----------------------------------------------------
-- Table `skus`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `skus` ;

CREATE  TABLE IF NOT EXISTS `skus` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `item_id`           BIGINT        NULL COMMENT '商品id',
  `outer_id`          VARCHAR(32)   NULL COMMENT '对应外部系统的id',
  `price`             INT           NULL COMMENT '实际价格',
  `stock`             INT           NULL COMMENT '库存数量',
  `image`             VARCHAR(127)  NULL COMMENT '图片',
  `attribute_key1`    VARCHAR(32)   NULL COMMENT '属性键1',
  `attribute_name1`   VARCHAR(32)   NULL COMMENT '属性名1',
  `attribute_value1`  VARCHAR(32)   NULL COMMENT '属性值1',
  `attribute_key2`    VARCHAR(32)   NULL COMMENT '属性键2',
  `attribute_name2`   VARCHAR(32)   NULL COMMENT '属性名2',
  `attribute_value2`  VARCHAR(32)   NULL COMMENT '属性值2',
  `model`             VARCHAR(64)   NULL COMMENT '型号',
  PRIMARY KEY (`id`)
);

create index idx_skus_item_id on skus(item_id);


-- -----------------------------------------------------
-- Table `brands`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `brands`;

CREATE TABLE IF NOT EXISTS `brands` (
  `id`            INT           NOT NULL  AUTO_INCREMENT COMMENT '品牌id',
  `parent_id`     INT           NULL      COMMENT '上级品牌id',
  `name`          VARCHAR (30)  NULL      COMMENT '品牌名称',
  `english_name`  VARCHAR (30)  NULL      COMMENT '英文名称项',
  `description`   VARCHAR (255) NULL      COMMENT '品牌描述',
  `created_at`    DATETIME      NULL      COMMENT '创建日期',
  `updated_at`    DATETIME      NULL      COMMENT '更新日期',
  PRIMARY KEY (`id`)
);


DROP TABLE IF EXISTS `title_keyword`;

CREATE TABLE IF NOT EXISTS `title_keyword` (
  `id`            BIGINT  NOT NULL  AUTO_INCREMENT COMMENT '序列号',
  `name_id`	      BIGINT  NOT NULL  COMMENT '关键字ID',
  `path`          VARCHAR (32)  NOT NULL COMMENT '路径',
  `title`         VARCHAR (30)  NULL      COMMENT '搜索页面',
  `keyword`  	  VARCHAR (30)  NULL      COMMENT '页面搜索关键字',
  `desc`	      VARCHAR (255) NULL      COMMENT '描述信息',
  `friend_links`  VARCHAR (2048) NULL      COMMENT '友情链接',
  `created_at`    DATETIME      NULL      COMMENT '表创建时间',
  `updated_at`    DATETIME      NULL      COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`)
);
