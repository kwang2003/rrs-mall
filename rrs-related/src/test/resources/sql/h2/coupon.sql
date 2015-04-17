-- -----------------------------------------------------
-- Table `coupons` 优惠券定义表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `coupons`;

CREATE TABLE `coupons` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name`       VARCHAR(32)  NOT NULL COMMENT '优惠券名称',
  `shop_id`    BIGINT       NOT NULL COMMENT '店铺id',
  `shop_name`  VARCHAR(255) NOT NULL COMMENT '店铺名称（冗余）',
  `seller_id`  BIGINT       NOT NULL COMMENT '商家id（冗余）',
  `amount`     INT          NOT NULL COMMENT '优惠券面额',
  `use_limit`  INT          NOT NULL COMMENT '使用条件，表现形式为满多少才能使用',
  `type`       SMALLINT     NOT NULL COMMENT '种类：前台领取或者活动赠送',
  `status`     SMALLINT     NOT NULL COMMENT '优惠券状态：初始（INIT）发布（RELEASE）生效（VALID）挂起（SUSPEND）过期（EXPIRE）',
  `taken`      INT          NOT NULL COMMENT '领取人数',
  `used`       INT          NOT NULL COMMENT '使用人数（结算中使用了优惠券的订单数）',
  `clicked`    INT          NOT NULL COMMENT '点击人数（进店人数）',
  `start_at`   DATETIME     NOT NULL COMMENT '使用开始时间',
  `end_at`     DATETIME     NOT NULL COMMENT '使用结束时间',
  `created_at` DATETIME     NULL     COMMENT '创建时间',
  `updated_at` DATETIME     NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`));

-- -----------------------------------------------------
-- Table `coupon_usages` 优惠券使用表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `coupon_usages`;

CREATE TABLE `coupon_usages` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `coupon_id`  BIGINT       NOT NULL COMMENT '对应券id',
  `coupon_name` VARCHAR(32) NOT NULL ,
  `buyer_id`   BIGINT       NOT NULL COMMENT '买家id',
  `seller_id`  BIGINT       NOT NULL COMMENT '卖家id',
  `shop_name`  VARCHAR(255) NOT NULL COMMENT '店铺名称（冗余）',
  `amount`     INT          NOT NULL COMMENT '优惠券金额（冗余）',
  `unused`     INT          NOT NULL COMMENT '券未使用数',
  `used`       INT          NOT NULL COMMENT '券使用数',
  `end_at`     DATETIME     NOT NULL COMMENT '到期时间（冗余）',
  `created_at` DATETIME     NULL     COMMENT '创建时间',
  `updated_at` DATETIME     NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`));



-- -------------------------------------------------------
-- activity_definitions 优惠活动定义
-- -------------------------------------------------------
DROP TABLE IF EXISTS `activity_definitions`;
CREATE TABLE `activity_definitions`(
  `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `activity_name` VARCHAR(256)   NOT NULL COMMENT '优惠活动名称',
  `activity_desc` VARCHAR(1024)  NULL     COMMENT '优惠活动描述信息',
  `business_id`   SMALLINT       NOT NULL COMMENT '行业编码 1:家电 2:家具 3:家装 4:家饰 5:净水',
  `activity_type` SMALLINT       NOT NULL COMMENT '优惠码类型(1.公开码; 2.渠道码)',
  `status`        SMALLINT       NOT NULL COMMENT '优惠码状态(1.新建未生效; 2.已生效; -1.已失效; -2.手动使失效)',
  `channel_type`  SMALLINT       NULL     COMMENT '渠道码类别(1.经销商; 2.服务兵),只有当 code_type=2 时该标识有效',
  `discount`      INT            NOT NULL COMMENT '优惠金额',
  `stock`         INT            NULL     COMMENT '优惠码发放数量,若该值为 NULL 则发放数量不限制(用 -1 or NULL 标识无限?)',
  `use_limit`     INT            NULL     COMMENT '买家使用数量 预留字段',
  `order_count`   INT            NOT NULL DEFAULT 0 COMMENT '订单数量',
  `start_at`      DATETIME       NOT NULL COMMENT '有效开始时间',
  `end_at`        DATETIME       NOT NULL COMMENT '有效截止时间',
  `created_at`    DATETIME       NULL     COMMENT '创建时间',
  `updated_at`    DATETIME       NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`)
);



-- -------------------------------------------------------
-- activity_codes 与活动相关的优惠码
-- -------------------------------------------------------
DROP TABLE IF EXISTS `activity_codes`;
CREATE TABLE `activity_codes` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `code`          VARCHAR(128)  NOT NULL COMMENT '优惠码代码',
  `activity_id`   BIGINT        NOT NULL COMMENT '活动ID',
  `activity_name` VARCHAR(256)  NOT NULL COMMENT '优惠码活动名称',
  `activity_type` SMALLINT      NOT NULL COMMENT '冗余，优惠码类型(1.公开码; 2.渠道码)',
  `usage`         INT           NOT NULL DEFAULT 0 COMMENT '',
  `created_at`    DATETIME       NULL     COMMENT '创建时间',
  `updated_at`    DATETIME       NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`)
);


-- -----------------------------------------------------
-- Table `activity_binds` 优惠活动绑定
-- -----------------------------------------------------
DROP TABLE IF EXISTS `activity_binds`;

CREATE TABLE `activity_binds` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `activity_id`     BIGINT       NOT NULL COMMENT '对应优惠活动id',
  `target_id`   BIGINT       NOT NULL COMMENT '优惠活动绑定id',
  `target_type` SMALLINT     NOT NULL COMMENT '优惠活动绑定的类型1（item）2(spu) 3(品类)',
  `created_at`  DATETIME     NULL     COMMENT '创建时间',
  `updated_at`  DATETIME     NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_cb_activity_id ON activity_binds (activity_id);


-- -----------------------------------------------------
-- Table `code_usages` 优惠码使用情况
-- -----------------------------------------------------
DROP TABLE IF EXISTS `code_usages`;

CREATE TABLE `code_usages` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_id`      BIGINT       NOT NULL COMMENT '订单id',
  `business_id`   SMALLINT     NOT NULL COMMENT '行业编码 1:家电 2:家具 3:家装 4:家饰 5:净水',
  `code`          VARCHAR(32)  NOT NULL COMMENT '优惠码名称',
  `buyer_id`      BIGINT       NOT NULL COMMENT '买家id',
  `buyer_name`    VARCHAR(64)  NOT NULL COMMENT '买家账户',
  `seller_id`     BIGINT       NOT NULL COMMENT '卖家id',
  `seller_name`   VARCHAR(64)  NOT NULL COMMENT '卖家账户',
  `activity_id`   BIGINT       NOT NULL COMMENT '活动id',
  `activity_name` VARCHAR(256) NOT NULL COMMENT '优惠活动名称',
  `activity_type` SMALLINT     NOT NULL COMMENT '活动类型',
  `discount`      INT          NOT NULL COMMENT '优惠金额',
  `origin_price`  INT          NOT NULL COMMENT '优惠前的价格',
  `price`         INT          NOT NULL COMMENT '优惠后的价格',
  `channel_type`  SMALLINT     NULL     COMMENT '渠道码类别(1.经销商; 2.服务兵),只有当 code_type=2 时该标识有效',
  `used_at`       DATETIME     NULL     COMMENT '优惠码使用时间',
  `used_count`    INT          NOT NULL DEFAULT 0 COMMENT '使用优惠码的次数',
  `created_at`    DATETIME     NULL     COMMENT '创建时间',
  `updated_at`    DATETIME     NULL     COMMENT '修改时间',
  PRIMARY KEY (`id`));




-- 优惠码
DROP TABLE IF EXISTS `rrs_coupons`;
CREATE TABLE `rrs_coupons` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `cpName` varchar(32) NOT NULL COMMENT '优惠券名称',
  `channelId` bigint(20) NOT NULL COMMENT '频道ID',
  `startTime` datetime DEFAULT NULL COMMENT '有效期开始时间',
  `endTime` datetime DEFAULT NULL COMMENT '有效期结束时间',
  `area` bigint(20) NOT NULL COMMENT '券区域 目前默认全国',
  `term` int(20) NOT NULL COMMENT '优惠条件 即需要满多少',
  `amount` int(11) NOT NULL COMMENT '优惠券面额优惠金额',
  `userType` int(11) DEFAULT NULL COMMENT '使用用户类型目前只要注册用户即可',
  `useLimit` int(11) NOT NULL COMMENT '单个用户限领数额',
  `status` smallint(6) NOT NULL DEFAULT '2' COMMENT '优惠券状态：未生效（0）暂停（1）生效（2）失效(3)',
  `sendNum` int(11) NOT NULL COMMENT '发券的总数量',
  `sendType` int(11) NOT NULL COMMENT '发券的类型1(抢)2（领）3（发）',
  `sendStartTime` datetime NOT NULL COMMENT '发券期限开始时间',
  `sendEndTime` datetime NOT NULL COMMENT '发券期限结束时间',
  `sendOrigin` varchar(100)  NOT NULL COMMENT '发券说明',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  `memo` text null COMMENT '备注',
  `costsBear` varchar(50)  DEFAULT NULL COMMENT '成本承担 1商家 2频道 3 平台 目前默认商家',
  `couponUse` int(11) DEFAULT NULL COMMENT '已使用优惠券数量',
  `couponReceive` int(11) DEFAULT '0' COMMENT '已领取优惠券数量',
  `categoryId` text null COMMENT '类目id组合',
  `mainImages` varchar(150)  DEFAULT NULL COMMENT '上传图片地址',
  `categoryName` text null COMMENT '类目名组合',
  PRIMARY KEY (`id`)
);

/*Table structure for table `rrs_coupons_area` */
DROP TABLE IF EXISTS `rrs_coupons_area`;
CREATE TABLE `rrs_coupons_area` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `couponsId` bigint(20) DEFAULT NULL COMMENT '券的ID信息',
  `categoryId` bigint(20) DEFAULT NULL COMMENT '商品栏目ID',
  PRIMARY KEY (`id`)
);

/*Table structure for table `rrs_coupons_user_order` */
DROP TABLE IF EXISTS `rrs_coupons_user_order`;
CREATE TABLE `rrs_coupons_user_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `orderId` bigint(20) DEFAULT NULL COMMENT '订单ID',
  `couponId` bigint(20) DEFAULT NULL COMMENT '优惠券Id',
  PRIMARY KEY (`id`)
);

/*Table structure for table `rrs_coupons_user_order_items` */
DROP TABLE IF EXISTS `rrs_coupons_user_order_items`;
CREATE TABLE `rrs_coupons_user_order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `orderId` bigint(20) DEFAULT NULL COMMENT '订单ID',
  `itemId` bigint(20) DEFAULT NULL COMMENT '订单明细Id',
  `freeAmount` bigint(20) DEFAULT NULL COMMENT '优惠金额',
  `couponsId` bigint(20) DEFAULT NULL COMMENT '对应使用的优惠券ID',
  `skuId` bigint(20) DEFAULT NULL COMMENT 'skuId',
  PRIMARY KEY (`id`)
) ;

/*Table structure for table `rrs_coupons_users` */
DROP TABLE IF EXISTS `rrs_coupons_users`;
CREATE TABLE `rrs_coupons_users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) DEFAULT NULL COMMENT '用户Id',
  `couponId` bigint(20) DEFAULT NULL COMMENT '优惠券Id',
  `status` int(11) DEFAULT NULL COMMENT '优惠券状态 1未使用 2使用 3过期',
  PRIMARY KEY (`id`)
);
