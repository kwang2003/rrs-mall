-- -----------------------------------------------------
-- Table `item_bundles`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `item_bundles`;

CREATE TABLE IF NOT EXISTS `item_bundles` (
  `id`            BIGINT        NOT NULL  AUTO_INCREMENT,
  `seller_id`     BIGINT        NOT NULL  COMMENT '商家id',
  `item_id1`      BIGINT        NULL      COMMENT '组合商品itemId',
  `item_id2`      BIGINT        NULL      COMMENT '组合商品itemId',
  `item_id3`      BIGINT        NULL      COMMENT '组合商品itemId',
  `item_id4`      BIGINT        NULL      COMMENT '组合商品itemId',
  `item1_quantity`INT           NULL      COMMENT '组合商品数量',
  `item2_quantity`INT           NULL      COMMENT '组合商品数量',
  `item3_quantity`INT           NULL      COMMENT '组合商品数量',
  `item4_quantity`INT           NULL      COMMENT '组合商品数量',
  `name`          VARCHAR(32)   NULL      COMMENT '组合商品名称',
  `desc`          VARCHAR(255)  NULL      COMMENT '描述',
  `original_price`INT           NOT NULL  COMMENT '原价',
  `price`         INT           NOT NULL  COMMENT '套餐价',
  `status`        SMALLINT      NOT NULL  COMMENT '状态''(上架（OnShelf）下架（OffShelf）',
  `created_at`    DATETIME      NOT NULL  COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL  COMMENT '更新时间',
  PRIMARY KEY (`id`));