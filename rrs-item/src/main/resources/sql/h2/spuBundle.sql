-- -----------------------------------------------------
-- Table `spu_bundles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `spu_bundles` ;

CREATE TABLE IF NOT EXISTS `spu_bundles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '创建的用户id',
  `name` varchar(32) NOT NULL COMMENT '组合套餐名',
  `description` varchar(255) DEFAULT NULL COMMENT '组合套餐描述',
  `id_one` bigint(20) NOT NULL COMMENT '第一个 SPU id',
  `quantity_one` int(11) DEFAULT '1' COMMENT '第一个商品数量',
  `name_one` varchar(32) default null,
  `id_two` bigint(20) NOT NULL COMMENT '第二个 SPU id',
  `quantity_two` int(11) DEFAULT '1' COMMENT '第二个商品数量',
  `name_two` varchar(32) default null,
  `id_three` bigint(20) DEFAULT NULL COMMENT '第三个 SPU id',
  `quantity_three` int(11) DEFAULT '0' COMMENT '第三个商品数量',
  `name_three` varchar(32) default null,
  `id_four` bigint(20) DEFAULT NULL COMMENT '第四个 SPU id',
  `quantity_four` int(11) DEFAULT '0' COMMENT '第四个商品数量',
  `name_four` varchar(32) default null,
  `status` smallint(6) DEFAULT NULL COMMENT '组合套餐模板状态，1-上架，2-下架',
  `used_count` bigint(20) DEFAULT '0' COMMENT '被商家使用的次数',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
);
