
## 任务清单

1、更新 TDK 表,schema 如下

    DROP TABLE IF EXISTS `title_keyword`;

    CREATE TABLE `title_keyword` (
      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '序列号',
      `name_id` BIGINT NOT NULL COMMENT '关键字ID',
      `path` varchar(32) NOT NULL DEFAULT '',
      `title` varchar(128) DEFAULT NULL COMMENT '搜索页面',
      `keyword` varchar(256) DEFAULT NULL COMMENT '页面搜索关键字',
      `desc` varchar(512) DEFAULT NULL COMMENT '描述信息',
      `friend_links` varchar(2048) DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_name_id` (`name_id`),
      KEY `idx_path` (`path`)
    );
