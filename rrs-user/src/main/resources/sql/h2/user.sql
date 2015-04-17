-- -----------------------------------------------------
-- Table `users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `users`;

CREATE TABLE IF NOT EXISTS `users` (
  `id`                 BIGINT        NOT NULL   AUTO_INCREMENT COMMENT '自增主键',
  `email`              VARCHAR(64)   NULL       COMMENT '用户注册email',
  `name`               VARCHAR(64)   NULL       COMMENT '用户nick',
  `mobile`             VARCHAR(16)   NULL       COMMENT '用户手机号码',
  `encrypted_password` VARCHAR(64)   NOT NULL   COMMENT '用户密码，加盐加密',
  `type`               SMALLINT      NOT NULL   COMMENT '用户类型 0:管理员 1:买家 2:卖家 3:站点拥有者 4:设计师(无) 5:子帐号 6:批发商(海尔) 7:其他 8:代理商(多多宝) 9:财务',
  `avatar`             VARCHAR(255)  NULL       COMMENT '用户头像Url',
  `status`             SMALLINT      NOT NULL   COMMENT '用户状态 0：未激活 1：正常，-1：冻结',
  `tags`               VARCHAR(1024) NULL       COMMENT '标记，位操作代表特定含义',
  `parent`             BIGINT        NULL       COMMENT '子账号的父账号id',
  `third_part_id`      VARCHAR(64)   NULL       COMMENT '第三方(支付宝)帐号',
  `third_part_type`    SMALLINT      NULL       COMMENT '第三方帐号类型',
  `created_at`         DATETIME      NOT NULL   COMMENT '创建时间',
  `updated_at`         DATETIME      NOT NULL   COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_users_email_UNIQUE` (`email` ASC),
  UNIQUE INDEX `idx_users_name_UNIQUE` (`name` ASC),
  UNIQUE INDEX `idx_users_mobile_UNIQUE` (`mobile` ASC));


-- -----------------------------------------------------
-- Table `user_profiles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_profiles`;

CREATE TABLE IF NOT EXISTS `user_profiles` (
  `id`            BIGINT       NOT NULL   AUTO_INCREMENT COMMENT '自增主键',
  `user_id`       BIGINT       NOT NULL   COMMENT '用户id',
  `phone`         VARCHAR(32)  NULL       COMMENT '电话',
  `real_name`     VARCHAR(32)  NULL       COMMENT '真实姓名',
  `id_card_num`   VARCHAR(20)  NULL       COMMENT '身份证号',
  `birthday`      CHAR(10)     NULL       COMMENT '出生日期',
  `gender`        SMALLINT     NULL       COMMENT '性别 1:男, 2:女',
  `province_id`   BIGINT       NULL       COMMENT '所在省份id',
  `city_id`       BIGINT       NULL       COMMENT '所在城市id',
  `region_id`     BIGINT       NULL       COMMENT '所在地区id',
  `address`       VARCHAR(255) NULL       COMMENT '详细地址',
  `buyer_credit`  INT          NULL       COMMENT '买家信用',
  `seller_credit` INT          NULL       COMMENT '卖家信用',
  `description`   VARCHAR(255) NULL       COMMENT '自我简介',
  `extra`         VARCHAR(512) NULL       COMMENT '以json格式存储的其余信息',
  `created_at`    DATETIME     NOT NULL   COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL   COMMENT '修改时间',
  PRIMARY KEY (`id`));
CREATE INDEX idx_profiles_user_id ON user_profiles (user_id);

-- -----------------------------------------------------
-- Table `addresses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `addresses`;

CREATE TABLE IF NOT EXISTS `addresses` (
  `id`        INT         NOT NULL  COMMENT '自增主键',
  `parent_id` INT         NULL      COMMENT '上级区域编码',
  `name`      VARCHAR(64) NULL      COMMENT '区域名称',
  `level`     SMALLINT    NULL      COMMENT '区域级别 0:中国 1:省、直辖市、行政区 2:地市 3:区、县',
  PRIMARY KEY (`id`));

CREATE INDEX idx_addresses_parent_id ON addresses (parent_id);


-- -----------------------------------------------------
-- Table `user_extras`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_extras`;

CREATE TABLE `user_extras` (
  `id`              BIGINT    NOT NULL    AUTO_INCREMENT COMMENT '主键',
  `business_id`     INTEGER   NULL        COMMENT '运营或者商家等的行业ID',
  `user_id`         BIGINT    NOT NULL    COMMENT '用户ID',
  `trade_quantity`  INTEGER   NOT NULL    DEFAULT '0'  COMMENT '交易笔数',
  `trade_sum`       BIGINT    NOT NULL    DEFAULT '0'  COMMENT '交易总额',
  `his_quantity`    INTEGER   NOT NULL    DEFAULT '0'  COMMENT '历史交易笔数',
  `his_sum`         BIGINT    NOT NULL    DEFAULT '0'  COMMENT '历史交易总额',
  `created_at`      DATETIME  NULL        COMMENT '创建时间',
  `updated_at`      DATETIME  NULL        COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id_UNIQUE` (`user_id` asc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------
-- Table `user_images`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_images` ;

CREATE  TABLE IF NOT EXISTS `user_images` (
  `id`          BIGINT        NOT NULL  AUTO_INCREMENT COMMENT '自增主键' ,
  `user_id`     BIGINT        NOT NULL  COMMENT '用户id' ,
  `category`    VARCHAR(32)   NULL      COMMENT '分类',
  `file_name`   VARCHAR(127)  NULL      COMMENT '图片地址' ,
  `file_size`   INT           NULL      COMMENT '图片大小(字节)',
  `created_at`  DATETIME      NOT NULL  COMMENT '创建时间' ,
  PRIMARY KEY (`id`) )
;

create index idx_ui_user_id on user_images(user_id, category);

-- -----------------------------------------------------
-- Table `user_quotas`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_quotas` ;

CREATE  TABLE IF NOT EXISTS `user_quotas` (
  `id`                BIGINT    NOT NULL  AUTO_INCREMENT COMMENT '自增主键' ,
  `user_id`           BIGINT    NOT NULL  COMMENT '用户id' ,
  `max_image_count`   INT       NULL      COMMENT '最大允许图片数目' ,
  `used_image_count`  INT       NULL      COMMENT '已经使用的图片数目',
  `max_image_size`    BIGINT    NULL      COMMENT '最大允许图片空间大小(字节)',
  `used_image_size`   BIGINT    NULL      COMMENT '已经使用图片空间大小(字节)',
  `max_widget_count`  INT       NULL      COMMENT '最大允许widget数目' ,
  `used_widget_count` INT       NULL      COMMENT '已经使用的widget数目',
  `created_at`        DATETIME  NOT NULL  COMMENT '创建时间' ,
  `updated_at`        DATETIME  NOT NULL  COMMENT '修改时间' ,
  PRIMARY KEY (`id`) )
;

create index idx_uq_user_id on user_quotas(user_id);


-- -----------------------------------------------------
-- Table `user_account_summary`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_account_summary` ;

CREATE  TABLE IF NOT EXISTS `user_account_summary` (
  `id`                BIGINT          NOT NULL  AUTO_INCREMENT COMMENT '自增主键' ,
  `activity`          VARCHAR(64)     NOT NULL  COMMENT '活动' ,
  `channel`           VARCHAR(64)     NOT NULL  COMMENT '渠道' ,
  `from`              VARCHAR(64)     NOT NULL  COMMENT '来源' ,
  `user_id`           BIGINT          NOT NULL  COMMENT '用户id' ,
  `user_name`         VARCHAR(64)     NOT NULL  COMMENT '用户姓名' ,
  `login_type`        INTEGER         NOT NULL  COMMENT '登录类型 1: 用户名 2: 手机 3:邮箱' ,
  `created_at`        DATETIME        NOT NULL  COMMENT '创建时间' ,
  `updated_at`        DATETIME        NOT NULL  COMMENT '修改时间' ,
  PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX idx_uas_user_id_uniq on user_account_summary(user_id);