-- -----------------------------------------------------
-- Table `users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `users`;

CREATE TABLE IF NOT EXISTS `users` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `email`              VARCHAR(64)   NULL
  COMMENT '用户注册email',
  `name`               VARCHAR(64)   NULL
  COMMENT '用户nick',
  `mobile`             VARCHAR(16)   NULL
  COMMENT '用户手机号码',
  `encrypted_password` VARCHAR(64)   NOT NULL
  COMMENT '用户密码，加盐加密',
  `type`               SMALLINT      NOT NULL
  COMMENT '用户类型 0-管理员 1-买家 2-卖家 4-卖家子账号',
  `avatar`             VARCHAR(255)  NULL
  COMMENT '用户头像',
  `status`             SMALLINT      NOT NULL
  COMMENT '用户状态 0：未激活， 1：正常，-1：冻结',
  `tags`               VARCHAR(1024) NULL
  COMMENT '标记，位操作代表特定含义',
  `parent`             BIGINT        NULL
  COMMENT '子账号的父账号id',
  `third_part_id`      VARCHAR(64)   NULL
  COMMENT '第三方帐号',
  `third_part_type`    SMALLINT      NULL
  COMMENT '第三方帐号类型',
  `created_at`         DATETIME      NOT NULL
  COMMENT '创建时间',
  `updated_at`         DATETIME      NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_users_email_UNIQUE` (`email` ASC),
  UNIQUE INDEX `idx_users_name_UNIQUE` (`name` ASC),
  UNIQUE INDEX `idx_users_mobile_UNIQUE` (`mobile` ASC));


-- -----------------------------------------------------
-- Table `user_profiles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_profiles`;

CREATE TABLE IF NOT EXISTS `user_profiles` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id`       BIGINT       NOT NULL
  COMMENT '用户id',
  `phone`         VARCHAR(32)  NULL
  COMMENT '电话',
  `real_name`     VARCHAR(32)  NULL
  COMMENT '真实姓名',
  `id_card_num`   VARCHAR(20)  NULL
  COMMENT '身份证号',
  `birthday`      CHAR(10)     NULL
  COMMENT '出生日期',
  `gender`        SMALLINT     NULL
  COMMENT '性别',
  `province_id`   BIGINT COMMENT '所在省份id',
  `city_id`       BIGINT COMMENT '所在城市id',
  `region_id`     BIGINT COMMENT '所在地区id',
  `address`       VARCHAR(255) COMMENT '详细地址',
  `buyer_credit`  INT          NULL
  COMMENT '买家信用',
  `seller_credit` INT          NULL
  COMMENT '卖家信用',
  `description`   VARCHAR(255) NULL
  COMMENT '自我简介',
  `extra`         VARCHAR(512) NULL
  COMMENT '以json格式存储的其余信息',
  `created_at`    DATETIME     NOT NULL
  COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`));
CREATE INDEX idx_profiles_user_id ON user_profiles (user_id);

-- -----------------------------------------------------
-- Table `addresses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `addresses`;

CREATE TABLE IF NOT EXISTS `addresses` (
  `id`        INT         NOT NULL,
  `parent_id` INT         NULL,
  `name`      VARCHAR(64) NULL,
  `level`     SMALLINT    NULL,
  PRIMARY KEY (`id`));

CREATE INDEX idx_addresses_parent_id ON addresses (parent_id);


-- -----------------------------------------------------
-- Table `orders`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `orders`;

CREATE TABLE IF NOT EXISTS `orders` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `buyer_id`      BIGINT      NULL
  COMMENT '买家id',
  `seller_id`     BIGINT      NULL
  COMMENT '卖家id',
  `business`      INT         NULL
  COMMENT '订单所属行业id',
  `status`        SMALLINT    NULL
  COMMENT '订单状态 0:等待买家付款,1:买家已付款,2:卖家已发货,3:交易完成,-1:买家关闭,-2:卖家关闭,-3:卖家退款',
  `type`          SMALLINT    NULL
  COMMENT '交易类型 1:普通, 2:预售',
  `trade_info_id` BIGINT      NULL
  COMMENT '买家收货信息',
  `deliver_fee`   INT         NULL
  COMMENT '邮费',
  `payment_type`  SMALLINT    NULL
  COMMENT '付款类型 1:在线支付, 2:货到付款, 3:积分',
  `payment_code`  VARCHAR(32) NULL
  COMMENT '付款账户',
  `fee`           INT         NULL
  COMMENT '订单总价',
  `outer_code`    VARCHAR(32) NULL
  COMMENT '商家88码,冗余',
  `paid_at`       DATETIME    NULL
  COMMENT '付款时间',
  `delivered_at`  DATETIME    NULL
  COMMENT '发货时间',
  `done_at`       DATETIME    NULL
  COMMENT '完成时间',
  `canceled_at`   DATETIME    NULL
  COMMENT '交易关闭时间',
  `finished_at`   DATETIME    NULL
  COMMENT '交易完成时间，交易关闭或成功',
  `created_at`    DATETIME    NULL
  COMMENT '创建时间',
  `updated_at`    DATETIME    NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_orders_buyer_id ON orders (buyer_id);
CREATE INDEX idx_orders_seller_id ON orders (seller_id);
CREATE INDEX idx_orders_finished_at ON orders (finished_at);
CREATE INDEX idx_orders_updated_at ON orders (updated_at);

-- -----------------------------------------------------
-- Table `orders`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `order_extras`;

CREATE TABLE IF NOT EXISTS `order_extras` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_id`      BIGINT   NOT NULL
  COMMENT '订单id',
  `buyer_notes`   VARCHAR(200) COMMENT '买家留言',
  `invoice`       VARCHAR(255) COMMENT '发票信息',
  `updated_at`    DATETIME NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_oes_order_id ON order_extras (order_id);


-- -----------------------------------------------------
-- Table `order_items`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `order_items`;

CREATE TABLE IF NOT EXISTS `order_items` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_id`   BIGINT   NULL
  COMMENT '订单id',
  `sku_id`     BIGINT   NULL
  COMMENT 'sku id',
  `item_id`    BIGINT   NULL
  COMMENT '商品id',
  `item_name`  VARCHAR (50) NULL
  COMMENT '商品名称',
  `buyer_id`   BIGINT   NULL
  COMMENT '买家id',
  `fee`        INT      NULL
  COMMENT '总费用',
  `quantity`   INT      NULL
  COMMENT '购买数量',
  `discount`   INT      NULL
  COMMENT '折扣',
  `type`   INT      NULL
  COMMENT '子订单类型 1:普通交易, 2:预售定金, 3:预售尾款',
  `pay_type` INT    NULL
  COMMENT '付款类型',
  `status` INT NULL
  COMMENT '状态',
  `reason`   VARCHAR(255) NULL
  COMMENT '退货款理由',
  `refund_amount` INT NULL
  COMMENT '退款金额',
  `created_at` DATETIME NULL
  COMMENT '创建时间',
  `updated_at` DATETIME NULL
  COMMENT '修改时间',
  `request_refund_at` DATETIME NULL
  COMMENT '申请退款时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_oi_order_id ON order_items (order_id);

