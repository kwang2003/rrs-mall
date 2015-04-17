DROP TABLE IF EXISTS `pre_authorization_deposit_order`;
CREATE TABLE `pre_authorization_deposit_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单号',
  `trade_no` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '付宝支交易流水号',
  `status` int(1) DEFAULT '0' COMMENT '预授权押金状态：0未冻结资金、1资金冻结、2资金扣除、-1资金解冻（退款）',
  `type` int(1) DEFAULT NULL COMMENT '类型：1预授权，2押金',
  `payer_logon_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '付款方支付宝账号',
  `payer_user_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '付款方支付宝用户号',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id_index` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='预授权押金订单表';