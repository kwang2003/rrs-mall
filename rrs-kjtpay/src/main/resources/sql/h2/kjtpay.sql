DROP TABLE IF EXISTS `kjtpay_account`;
CREATE TABLE `kjtpay_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `partner_id` bigint(20) DEFAULT NULL,
  `plat_user_id` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `plat_user` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `member_id` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `member_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table shops add `bound_style` char(1),add `account_id` bigint(20),add `company_name` varchar(255);