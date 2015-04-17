-- ----------------------------
-- Table structure for `brands_club_key`
-- ----------------------------
DROP TABLE IF EXISTS `brands_club_key`;
CREATE TABLE `brands_club_key` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` int(11) NOT NULL,
  `brandClub_id` int(11) NOT NULL,
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '0 确定 1已领未确认',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;



-- ----------------------------
-- Table structure for `brand_user_announcement`
-- ----------------------------
DROP TABLE IF EXISTS `brand_user_announcement`;
CREATE TABLE `brand_user_announcement` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `brand_user_id` int(10) NOT NULL,
  `announcement` text,
  `status` int(10) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `title` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `brand_deposit_account`
-- ----------------------------
DROP TABLE IF EXISTS `brand_deposit_account`;
CREATE TABLE `brand_deposit_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `seller_id` bigint(20) NOT NULL COMMENT '商家id',
  `seller_name` varchar(64) NOT NULL COMMENT '商家账户',
  `business` int(11) DEFAULT NULL COMMENT '订单所属行业id',
  `outer_code` varchar(32) DEFAULT NULL COMMENT '商家8码',
  `balance` int(11) DEFAULT NULL COMMENT '商家保证金余额',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  `balance_tech` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `seller_id` (`seller_id`) USING BTREE,
  UNIQUE KEY `seller_name` (`seller_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=761 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `brand_club_type`
-- ----------------------------
DROP TABLE IF EXISTS `brand_club_type`;
CREATE TABLE `brand_club_type` (
  `id` int(11) DEFAULT NULL,
  `brandTypeName` varchar(50) DEFAULT NULL,
  `typeOrder` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `brand_club_slide`
-- ----------------------------
DROP TABLE IF EXISTS `brand_club_slide`;
CREATE TABLE `brand_club_slide` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `httpUrl` varchar(137) DEFAULT NULL,
  `mainImage` varchar(137) DEFAULT NULL,
  `imageType` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `brand_club_product_type`
-- ----------------------------
DROP TABLE IF EXISTS `brand_club_product_type`;
CREATE TABLE `brand_club_product_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `productTypeName` varchar(200) DEFAULT NULL,
  `typeOrder` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `brand_club_product`
-- ----------------------------
DROP TABLE IF EXISTS `brand_club_product`;
CREATE TABLE `brand_club_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `productName` varchar(200) DEFAULT NULL COMMENT '产品名称',
  `productImage` varbinary(200) DEFAULT NULL COMMENT '产品图片',
  `price` int(11) DEFAULT '0' COMMENT '现价',
  `oriprice` int(11) DEFAULT '0' COMMENT '原价',
  `brandClupId` int(11) DEFAULT NULL COMMENT '所属品牌馆',
  `productType` int(11) DEFAULT NULL COMMENT '产品类型',
  `productUrl` varchar(137) DEFAULT NULL COMMENT '产品URL地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `brand_club`
-- ----------------------------
DROP TABLE IF EXISTS `brand_club`;
CREATE TABLE `brand_club` (
  `brand_id` int(11) NOT NULL AUTO_INCREMENT,
  `brand_name` varchar(100) NOT NULL,
  `create_time` date DEFAULT NULL,
  `brand_user_id` int(11) NOT NULL,
  `brand_app_no` varchar(50) NOT NULL,
  `brand_name_en` varchar(50) DEFAULT NULL,
  `brand_logo` varchar(255) NOT NULL,
  `brand_desc` varchar(200) DEFAULT NULL,
  `brand_qualify` varchar(5) NOT NULL,
  `brand_trade_mark` varchar(255) NOT NULL,
  `brand_author` varchar(255) DEFAULT NULL,
  `status` int(10) NOT NULL DEFAULT '0',
  `brand_outer_id` varchar(20) DEFAULT NULL,
  `brandTypeId` int(11) DEFAULT NULL,
  `reason` varchar(50) DEFAULT NULL,
  `frozen_status` int(10) NOT NULL DEFAULT '0' COMMENT '冻结状态:   0正常   1冻结',
  `brand_main_img` varchar(200) DEFAULT NULL COMMENT '前端展示图片',
  `http2` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`brand_id`),
  UNIQUE KEY `UNI_APP_NO` (`brand_app_no`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

