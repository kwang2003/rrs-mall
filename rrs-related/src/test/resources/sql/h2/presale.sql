DROP TABLE IF EXISTS `pre_sales`;

CREATE TABLE IF NOT EXISTS `pre_sales`(
    `id`                  BIGINT        NOT NULL  AUTO_INCREMENT,
    `spu_id`              BIGINT        NOT NULL  COMMENT 'spu id',
    `item_id`             BIGINT        NOT NULL  COMMENT '商品id',
    `shop_ids`            VARCHAR(2048) NULL      COMMENT '预售对应的店铺id列表',
    `plain_buy_limit`     INT           NULL      COMMENT '普通用户购买限制',
    `by_storage`          BOOLEAN       NULL      COMMENT '是否需要支持分仓,如果为true则支持, 否则不支持',
    `earnest`             INT           NULL      COMMENT '定金',
    `remain_money`        INT           NULL      COMMENT '尾款',
    `advertise`           VARCHAR(255)  NULL      COMMENT '营销语',
    `price`               INT           NULL      COMMENT '价格',
    `fake_sold_quantity`  INT           NOT NULL  DEFAULT 0 COMMENT '虚拟销量',
    `is_release`          SMALLINT      NULL      COMMENT '是否已发布',
    `is_expire`           SMALLINT      NULL      COMMENT '是否已过期',
    `status`              SMALLINT      NULL      COMMENT '预售状态 0: 待发布, 1: 已发布待运行 2:运行中 3:已结束 -1:已终止',
    `released_at`         DATETIME      NULL      COMMENT '发布时间',
    `earnest_time_limit`  INT           NULL      COMMENT '以小时为单位的付定金时间',
    `presale_start_at`    DATETIME      NULL      COMMENT '预售开始时间',
    `presale_finish_at`   DATETIME      NULL      COMMENT '预售结束时间',
    `remain_start_at`     DATETIME      NULL      COMMENT '尾款开始时间',
    `remain_finish_at`    DATETIME      NULL      COMMENT '尾款截止时间',
    `created_at`          DATETIME      NULL      COMMENT '创建时间',
    `updated_at`          DATETIME      NULL      COMMENT '修改时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_presale_item_id ON pre_sales (item_id);

-- 每个商品在对应仓库的库存
DROP TABLE IF EXISTS `storage_stocks`;

CREATE TABLE IF NOT EXISTS `storage_stocks`(
  `id`          BIGINT    NOT NULL  AUTO_INCREMENT,
  `item_id`     BIGINT    NOT NULL  COMMENT '商品id',
  `storage_id`  INT       NOT NULL  COMMENT '仓库id',
  `init_stock`  INT       NOT NULL  COMMENT '初始库存',
  `sold_count`  INT       NULL      COMMENT '当前销量',
  `created_at`  DATETIME  NULL      COMMENT '创建时间',
  `updated_at`  DATETIME  NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

CREATE INDEX idx_ss_item_storage ON storage_stocks (item_id,storage_id);


-- 地区id到仓库码的映射关系
DROP TABLE IF EXISTS `address_storages`;
CREATE TABLE IF NOT EXISTS `address_storages`(
  `id`          BIGINT    NOT NULL  AUTO_INCREMENT,
  `item_id`     BIGINT    NOT NULL  COMMENT '商品id',
  `address_id`  INT       NOT NULL  COMMENT '区级别的地区id',
  `storage_id`  INT       NOT NULL  COMMENT '仓库id',
  `created_at`  DATETIME  NULL      COMMENT '创建时间',
  `updated_at`  DATETIME  NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

CREATE INDEX idx_as_item_district ON address_storages (item_id, address_id);









