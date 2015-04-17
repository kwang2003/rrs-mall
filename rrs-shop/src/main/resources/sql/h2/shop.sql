-- -----------------------------------------------------
-- Table `shops`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `shops`;

CREATE TABLE IF NOT EXISTS `shops` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `name`             VARCHAR(255) NOT NULL  COMMENT '店铺名称',
  `user_id`          BIGINT       NOT NULL  COMMENT '用户id',
  `user_name`        VARCHAR(32)  NOT NULL  COMMENT '用户名称',
  `status`           TINYINT      NOT NULL  COMMENT '状态 0:待审批 1:正常 -2:审批不通过 -1:冻结',
  `phone`            VARCHAR(32)  NULL      COMMENT '电话',
  `fax`              VARCHAR(32)  NULL      COMMENT '传真',
  `email`            VARCHAR(64)  NULL      COMMENT '邮箱地址',
  `business_id`      INT          NOT NULL  COMMENT '分类',
  `image_url`        VARCHAR(255) NULL      COMMENT '图片地址',
  `province`         INT          NULL      COMMENT '省、直辖市编码',
  `city`             INT          NULL      COMMENT '城市编码',
  `region`           INT          NULL      COMMENT '区县编码',
  `street`           VARCHAR(64)  NULL      COMMENT '街道',
  `tax_register_no`  VARCHAR(32)  NULL      COMMENT '税务登记号',
  `is_cod`           SMALLINT     NULL      COMMENT '是否支持货到付款 0:不支持 1:支持',
  `e_invoice`        BOOLEAN      NULL      COMMENT '是否支持电子发票, 默认不支持',
  `vat_invoice`      BOOLEAN      NULL      COMMENT '是否支持增值税发票, 默认不支持',
  `delivery_time`    BOOLEAN      NULL      COMMENT '是否支持配送时间，默认不支持',
  `delivery_promise` BOOLEAN      NULL      COMMENT '是否支持配送承诺，默认不支持',
  `created_at`       DATETIME     NULL      COMMENT '创建时间',
  `updated_at`       DATETIME     NULL      COMMENT '更新时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_shops_user_id ON shops (user_id);

CREATE UNIQUE INDEX idx_shops_name ON shops (name);
CREATE INDEX idx_shops_tax_no ON shops (tax_register_no);

-- -----------------------------------------------------
-- Table `shop_paperworks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `shop_paperworks`;

CREATE TABLE `shop_paperworks` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `shop_id`           BIGINT        NOT NULL  COMMENT '店铺id',
  `business_licence`        VARCHAR(255)   NULL      COMMENT '营业执照',
  `tax_certificate`        VARCHAR(255)   NULL      COMMENT '税务登记证',
  `account_permit`        VARCHAR(255)  NULL      COMMENT '开户许可证',
  `organization_code`       VARCHAR(255)  NULL      COMMENT '组织机构代码证',
  `corporate_identity`  VARCHAR(255)  NULL      COMMENT '法人身份证号',
  `corporate_identityB` VARCHAR(255)  NULL      COMMENT '法人身份证号背面',
  `contract_image1`     VARCHAR(255)  NULL      COMMENT '合同图片',
  `contract_image2`     VARCHAR(255)  NULL      COMMENT '合同图片',
  `created_at`        DATETIME      NULL      COMMENT '创建日期',
  `updated_at`        DATETIME      NULL      COMMENT '修改日期',
  PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX idx_shop_pw_shop_id ON shop_paperworks (shop_id);

-- -----------------------------------------------------
-- Table `shop_categories`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `shop_categories`;

CREATE TABLE IF NOT EXISTS `shop_categories` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(32) NOT NULL COMMENT '店铺类目名称',
  `parent_id`  BIGINT      NOT NULL COMMENT '对应的后台类目id',
  `created_at` DATETIME    NULL     COMMENT '创建时间',
  `updated_at` DATETIME    NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_sc_shop_id ON shop_categories (parent_id);

-- -----------------------------------------------------
-- Table `shop_interior_categories`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `shop_interior_categories`;

CREATE TABLE IF NOT EXISTS `shop_interior_categories` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `shop_id`    BIGINT        NOT NULL COMMENT '店铺id',
  `categories` TEXT          NULL     COMMENT '店铺内类目',
  `created_at` DATETIME      NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME      NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_sc_shop_id_UNIQUE` (`shop_id` ASC));


DROP TABLE IF EXISTS `shop_authorize_infos`;

CREATE TABLE `shop_authorize_infos` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `shop_id`         BIGINT        DEFAULT NULL  COMMENT '店铺id',
  `json_authorize`  TEXT          DEFAULT NULL  COMMENT '授权信息',
  PRIMARY KEY (`id`)
);

-- -----------------------------------------------------
-- Table `shop_extras`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `shop_extras`;

CREATE TABLE `shop_extras` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `shop_id`           BIGINT        NOT NULL  COMMENT '店铺id',
  `outer_code`        VARCHAR(32)   NULL      COMMENT '商家外部编码',
  `ntalker_id`        VARCHAR(32)   NULL      COMMENT 'ntalker id',
  `rate`              DECIMAL(9,4)  NULL      DEFAULT 0.0000 COMMENT '费率',
  `rate_updating`     DECIMAL(9,4)  NULL      DEFAULT 0.0000 COMMENT '待更新的费率',
  `deposit_need`      BIGINT        NULL      DEFAULT 0 COMMENT '应缴纳的保证金',
  `tech_fee_need`     BIGINT        NULL      DEFAULT 0 COMMENT '应缴纳的技术服务费',
  `haier_code`        VARCHAR(100)  NULL      COMMENT '海尔店铺内部编号，特许号',
  `pay_account`       VARCHAR(100)  NULL      COMMENT '支付帐号',
  `pay_account_type`  VARCHAR(100)  NULL      COMMENT '支付帐号类型',
  `r_describe`        BIGINT        NULL      COMMENT '描述',
  `r_service`         BIGINT        NULL      COMMENT '服务',
  `r_express`         BIGINT        NULL      COMMENT '快递',
  `r_quality`         BIGINT        NULL      COMMENT '质量',
  `trade_quantity`    BIGINT        NULL      COMMENT '交易总量',
  `created_at`        DATETIME      NULL      COMMENT '创建日期',
  `updated_at`        DATETIME      NULL      COMMENT '修改日期',
  PRIMARY KEY (`id`)
);
