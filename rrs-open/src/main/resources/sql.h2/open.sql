-- ----------------------------
-- Table structure for `open_channels`
-- ----------------------------
DROP TABLE IF EXISTS `open_channels`;
CREATE TABLE `open_channels` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `channel` varchar(50) NOT NULL COMMENT '频道KEY',
  `key` varchar(50) NOT NULL COMMENT '密钥',
  `origin` varchar(50) DEFAULT NULL COMMENT '频道来源',
  `type` varchar(50) DEFAULT NULL COMMENT '频道类型（1-品牌，2-店铺）',
  `business_id` int(11) NOT NULL COMMENT '1-家电，2-家具，3-家装，4-家饰，5-净水',
  `role1` varchar(50) DEFAULT NULL COMMENT '角色1',
  `role2` varchar(50) DEFAULT NULL COMMENT '角色2',
  `role3` varchar(50) DEFAULT NULL COMMENT '角色3',
  `role4` varchar(50) DEFAULT NULL COMMENT '角色4',
  `role5` varchar(50) DEFAULT NULL COMMENT '角色5',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除 0:不删 1:删除',
  `created_at` datetime DEFAULT NULL COMMENT '创建日期',
  `updated_at` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `open_channels_auth`
-- ----------------------------
DROP TABLE IF EXISTS `open_channels_auth`;
CREATE TABLE `open_channels_auth` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `method` varchar(100) NOT NULL COMMENT 'API方法',
  `cats` varchar(10) NOT NULL COMMENT '所属类目：1-用户，2-地区，3-商品，4-类目，5-店铺，6-交易，7-物流，8-评价',
  `role1` tinyint(10) DEFAULT NULL COMMENT '角色1，记录需要授权验证的角色，角色类型：1-店铺，2-品牌商，3-第三方',
  `role2` tinyint(10) DEFAULT NULL COMMENT '角色2',
  `role3` tinyint(10) DEFAULT NULL COMMENT '角色3',
  `role4` tinyint(10) DEFAULT NULL COMMENT '角色4',
  `role5` tinyint(10) DEFAULT NULL COMMENT '角色5',
  `role6` tinyint(10) DEFAULT NULL COMMENT '角色6',
  `role7` tinyint(10) DEFAULT NULL COMMENT '角色7',
  `role8` tinyint(10) DEFAULT NULL COMMENT '角色8',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除 0:不删 1:删除',
  `created_at` datetime DEFAULT NULL COMMENT '创建日期',
  `updated_at` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `open_channels_shops`
-- ----------------------------
DROP TABLE IF EXISTS `open_channels_shops`;
CREATE TABLE `open_channels_shops` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `channel` varchar(50) NOT NULL COMMENT '频道KEY',
  `shop_id` bigint(20) NOT NULL COMMENT '店铺ID',
  `user_id` bigint(20) NOT NULL,
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除 0:不删 1:删除',
  `created_at` datetime DEFAULT NULL COMMENT '创建日期',
  `updated_at` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `open_channels_sms_template`
-- ----------------------------
DROP TABLE IF EXISTS `open_channels_sms_template`;
CREATE TABLE `open_channels_sms_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `content` varchar(1000) NOT NULL COMMENT '模板内容',
  `created_at` datetime DEFAULT NULL COMMENT '创建日期',
  `updated_at` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;



