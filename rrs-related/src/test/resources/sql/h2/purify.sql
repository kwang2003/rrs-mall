-- -----------------------------------------------------
-- Table `rrs_purify_series` 净水系列
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_purify_series`;

CREATE TABLE IF NOT EXISTS `rrs_purify_series` (
  `id`               BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `series_name`      VARCHAR(32) NOT NULL  COMMENT '系列名称',
  `series_introduce` VARCHAR(64) NOT NULL  COMMENT '净水系列介绍',
  `series_image`     VARCHAR(64) NOT NULL  COMMENT '净水系列图片地址',
  `site_id`          BIGINT      NOT NULL  COMMENT '站点编号(将系列与站点关联)',
  `created_at`       DATETIME    NULL      COMMENT '创建时间',
  `updated_at`       DATETIME    NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`));

INSERT INTO rrs_purify_series ( series_name, series_introduce, series_image, site_id, created_at, updated_at ) VALUES ('厨房系列', '测试数据信息', 'http://asdmaos.asdoko.asdmm.img', 1, now(), now());
INSERT INTO rrs_purify_series ( series_name, series_introduce, series_image, site_id, created_at, updated_at ) VALUES ('厅房系列', '测试数据信息', 'http://asdmaos.asdoko.asdmm.img', 1, now(), now());
INSERT INTO rrs_purify_series ( series_name, series_introduce, series_image, site_id, created_at, updated_at ) VALUES ('测试系列3', '测试数据信息', 'http://asdmaos.asdoko.asdmm.img', 1, now(), now());

-- -----------------------------------------------------
-- Table `rrs_purify_category` 净水组件类目
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_purify_category`;

CREATE TABLE IF NOT EXISTS `rrs_purify_category` (
`id`                  BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
`series_id`           BIGINT      NOT NULL  COMMENT '系列编号',
`stage`               INT         NOT NULL  COMMENT '阶段顺序编号',
`category_name`       VARCHAR(32) NOT NULL  COMMENT '组件类目名称',
`category_image`      VARCHAR(64) NOT NULL  COMMENT '组件类目图片地址',
`created_at`          DATETIME    NULL      COMMENT '创建时间',
`updated_at`          DATETIME    NULL      COMMENT '修改时间',
PRIMARY KEY (`id`));
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (1, 1, '过滤方式', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (1, 2, '品牌', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (1, 3, '外观', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (1, 4, '水量', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (1, 5, '出水方式', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (1, 6, '显示方式', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (2, 1, '产品类型', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (2, 2, '品牌', 'http://127.0.0.1/asdji.png', now(), now());
INSERT INTO rrs_purify_category ( series_id, stage, category_name, category_image, created_at, updated_at ) VALUES (2, 3, '放置方式', 'http://127.0.0.1/asdji.png', now(), now());


-- -----------------------------------------------------
-- Table `rrs_purify_assembly` 净水组件
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_purify_assembly`;

CREATE TABLE IF NOT EXISTS `rrs_purify_assembly` (
`id`                  BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
`category_id`         BIGINT      NOT NULL  COMMENT '组建类目编号',
`assembly_name`       VARCHAR(32) NOT NULL  COMMENT '组件名称',
`assembly_total`      BIGINT      NOT NULL  COMMENT '组件价格',
`assembly_introduce`  VARCHAR(64) NOT NULL  COMMENT '组件介绍',
`assembly_image`      VARCHAR(64) NOT NULL  COMMENT '组件图片地址',
`created_at`          DATETIME    NULL      COMMENT '创建时间',
`updated_at`          DATETIME    NULL      COMMENT '修改时间',
PRIMARY KEY (`id`));
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (1, '反渗透', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (1, '活性炭', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (1, '超滤', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (2, '海尔', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (2, '霍尼韦尔', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (2, '柯淇', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (2, '爱惠浦', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (3, '防尘罩设计', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (3, '箱式设计', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (3, '无罩式设计', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (4, '50G', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (4, '70G', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (4, '100G', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (5, '单出水', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
INSERT INTO rrs_purify_assembly ( category_id, assembly_name, assembly_total, assembly_introduce, assembly_image, created_at, updated_at ) VALUES (5, '双出水', 10000, '测试类目实体对象', 'http://127.0.0.1/image/zero.png', now(), now());
-- -----------------------------------------------------
-- Table `rrs_purify_relation` 净水组件关联
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_purify_relation`;

CREATE TABLE IF NOT EXISTS `rrs_purify_relation` (
`id`                  BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
`assembly_parent`     BIGINT      NOT NULL  COMMENT '上级组件编号',
`assembly_child`      BIGINT      NOT NULL  COMMENT '下级组件编号',
`product_id`          BIGINT      NOT NULL  COMMENT '商品编号(当0编号当前组建没有直接关联商品，n表示关联商品的编号)',
`created_at`          DATETIME    NULL      COMMENT '创建时间',
`updated_at`          DATETIME    NULL      COMMENT '修改时间',
PRIMARY KEY (`id`));

INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (1, 4, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (1, 5, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (1, 6, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (2, 7, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (4, 8, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (4, 9, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (4, 10, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (6, 8, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (6, 9, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (8, 11, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (8, 12, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (8, 13, 0, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (11, 14, 1, now(), now());
INSERT INTO rrs_purify_relation ( assembly_parent, assembly_child, product_id, created_at, updated_at ) VALUES (11, 15, 2, now(), now());

-- -----------------------------------------------------
-- Table `rrs_purify_product` 净水产品
-- -----------------------------------------------------