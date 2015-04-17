DROP TABLE IF EXISTS `pre_deposit`;
CREATE TABLE `pre_deposit` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `spu_id` bigint(20) DEFAULT NULL,
  `earnest` int(11) DEFAULT NULL,
  `remain_money` int(11) DEFAULT NULL,
  `advertise` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `is_release` smallint(6) DEFAULT NULL,
  `is_expire` smallint(6) DEFAULT NULL,
  `earnest_time_limit` int(11) DEFAULT NULL,
  `remain_time_limit` int(11) DEFAULT NULL,
  `preSale_finish_at` datetime DEFAULT NULL,
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
-- ----------------------------
-- Table structure for `pre_sales`
-- ----------------------------
DROP TABLE IF EXISTS `pre_deposits`;
CREATE TABLE `pre_deposits` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `spu_id` bigint(20) NOT NULL COMMENT 'spu id',
  `item_id` bigint(20) NOT NULL COMMENT '商品id',
  `shop_ids` varchar(2048) DEFAULT NULL COMMENT '预售对应的店铺id列表',
  `plain_buy_limit` int(11) DEFAULT NULL COMMENT '普通用户购买限制',
  `by_storage` tinyint(1) DEFAULT NULL COMMENT '是否需要支持分仓,如果为true则支持, 否则不支持',
  `earnest` int(11) DEFAULT NULL COMMENT '定金',
  `remain_money` int(11) DEFAULT NULL COMMENT '尾款',
  `advertise` varchar(255) DEFAULT NULL COMMENT '广告词',
  `price` int(11) DEFAULT NULL COMMENT '价格',
  `fake_sold_quantity` int(11) NOT NULL DEFAULT '0' COMMENT '虚拟销量',
  `is_release` smallint(6) DEFAULT NULL COMMENT '是否已发布',
  `is_expire` smallint(6) DEFAULT NULL COMMENT '是否已过期',
  `status` smallint(6) DEFAULT NULL COMMENT '预售状态 0: 待发布, 1: 已发布待运行 2:运行中 3:已结束 -1:已终止',
  `released_at` datetime DEFAULT NULL COMMENT '发布时间',
  `earnest_time_limit` int(11) DEFAULT NULL COMMENT '以小时为单位的付定金时间',
  `presale_start_at` datetime DEFAULT NULL COMMENT '预售开始时间',
  `presale_finish_at` datetime DEFAULT NULL COMMENT '预售结束时间',
  `remain_start_at` datetime DEFAULT NULL COMMENT '尾款开始时间',
  `remain_finish_at` datetime DEFAULT NULL COMMENT '尾款截止时间',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `smsFloag` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_presale_item_id` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=158 DEFAULT CHARSET=utf8;