-- -----------------------------------------------------
-- Table `rrs_settlements` 订单结算表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_settlements`;

CREATE TABLE IF NOT EXISTS `rrs_settlements` (
  `id`                      BIGINT      NOT NULL    AUTO_INCREMENT COMMENT '自增主键',
  `order_id`                BIGINT      NOT NULL    COMMENT '订单id',
  `seller_id`               BIGINT      NOT NULL    COMMENT '商家id',
  `seller_name`             VARCHAR(64) NULL        COMMENT '商家名称',
  `buyer_id`                BIGINT      NOT NULL    COMMENT '买家id',
  `buyer_name`              VARCHAR(64) NULL        COMMENT '买家名称',
  `business`                INT         NULL        COMMENT '订单所属行业id 根据不同行业抽取不同的佣金润点',
  `trade_status`            SMALLINT    NOT NULL    COMMENT '交易状态 1:买家已付款,3:交易完成, -3:卖家退款',
  `type`                    SMALLINT    NOT NULL    COMMENT '交易类型 1:普通交易,2:预售',
  `pay_type`                SMALLINT    NOT NULL    COMMENT '支付方式 1:在线支付,2:货到付款,3:积分',
  `payment_code`            VARCHAR(64) NULL        COMMENT '支付宝交易流水号',
  `multi_paid`              SMALLINT    NOT NULL    DEFAULT 0 COMMENT '是否合并支付的订单',
  `fee`                     BIGINT      NOT NULL    COMMENT '订单金额,以分为单位',
  `total_earning`           BIGINT      NULL        COMMENT '收入,以分为单位',
  `total_expenditure`       BIGINT      NULL        COMMENT '支出,以分为单位',
  `seller_earning`          BIGINT      NULL        COMMENT '商家收入,以分为单位',
  `rrs_commission`          BIGINT      NULL        COMMENT '平台佣金收入,以分为单位',
  `score_earning`           BIGINT      NULL        COMMENT '平台积分收入,以分为单位',
  `presell_deposit`         BIGINT      NULL        COMMENT '营业外收入,以分为单位',
  `presell_commission`      BIGINT      NULL        COMMENT '主营业务成本,以分为单位',
  `third_party_commission`  BIGINT      NULL        COMMENT '第三方(如支付宝)佣金,以分为单位',
  `commission_rate`         DECIMAL(9,4)  NULL      DEFAULT 0.0000 COMMENT '佣金扣点',
  `voucher`                 VARCHAR(64) NULL        COMMENT '凭证号',
  `third_party_receipt`     VARCHAR(64) NULL        COMMENT '第三方（如支付宝）佣金发票号',
  `settle_status`           SMALLINT    NOT NULL    COMMENT '结算状态 0:待结算,1:结算中(各收支，如佣金计算完毕),2:待确认,3:已确认,4:已结算,-1:结算失败',
  `fixed`                   BIT         NULL        COMMENT '是否补记账',
  `cashed`                  SMALLINT    NOT NULL    DEFAULT 0 COMMENT '提现状态 0:未提现, 1:已提现',
  `finished`                SMALLINT    NOT NULL    DEFAULT 0 COMMENT '完成/关闭状态 0:未完成 1:已完成',
  `settled`                 SMALLINT    NOT NULL    DEFAULT 0 COMMENT '系统结算完成 0:未完成, 1:已完成',
  `confirmed`               SMALLINT    NOT NULL    DEFAULT 0 COMMENT '商户是否确认 0:未确认， 1:确认',
  `synced`                  SMALLINT    NOT NULL    DEFAULT 0 COMMENT '同步JDE完成 0:未完成, 1:已完成',
  `vouched`                 SMALLINT    NOT NULL    DEFAULT 0 COMMENT '单据更新完成 0:未完成, 1:已完成',
  `ordered_at`              DATETIME    NOT NULL    COMMENT '下单时间',
  `paid_at`                 DATETIME        NULL    COMMENT '付款时间（计算支付宝手续费）',
  `finished_at`             DATETIME    NULL        COMMENT '订单结束时间(计算各种费用)',
  `settled_at`              DATETIME    NULL        COMMENT '结算完成时间',
  `confirmed_at`            DATETIME    NULL        COMMENT '商户确认时间(结算完成，待财务打款填写凭据)',
  `synced_at`               DATETIME    NULL        COMMENT 'JDE同步完成时间',
  `vouched_at`              DATETIME    NULL        COMMENT 'JDE打印凭证时间',
  `third_party_receipt_at`  DATETIME    NULL        COMMENT 'JDE打印第三方（支付宝手续费）发票时间',
  `created_at`              DATETIME    NULL        COMMENT '创建时间',
  `updated_at`              DATETIME    NULL        COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_rs_seller_id ON rrs_settlements (seller_id);
CREATE INDEX idx_rs_settled_at ON rrs_settlements (settled_at);
CREATE INDEX idx_rs_finished_at ON rrs_settlements (finished_at);
CREATE INDEX idx_rs_paid_at ON rrs_settlements (paid_at);



-- -----------------------------------------------------
-- Table `rrs_item_settlements` 子订单结算表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_item_settlements`;

CREATE TABLE IF NOT EXISTS `rrs_item_settlements` (
  `id`                      BIGINT        NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `order_id`                BIGINT        NOT NULL  COMMENT '订单id',
  `order_item_id`           BIGINT        NOT NULL  COMMENT '子订单id',
  `seller_id`               BIGINT        NOT NULL  COMMENT '商家id',
  `seller_name`             VARCHAR(64) NULL        COMMENT '商家名称',
  `buyer_id`                BIGINT        NOT NULL  COMMENT '买家id',
  `buyer_name`              VARCHAR(64)   NULL      COMMENT '买家名称',
  `business`                INT           NULL      COMMENT '订单所属行业id 根据不同行业抽取不同的佣金润点',
  `trade_status`            SMALLINT      NOT NULL  COMMENT '交易状态 1:买家已付款,3:交易完成, -3:卖家退款',
  `item_name`               VARCHAR(500)  NULL      COMMENT '商品名称',
  `item_quantity`           INT           NULL      COMMENT '商品数量',
  `type`                    SMALLINT      NOT NULL  COMMENT '交易类型 1:普通交易,2:预售定金,3:预售尾款',
  `pay_type`                SMALLINT      NOT NULL  COMMENT '支付方式 1:在线支付,2:货到付款,3:积分',
  `payment_code`            VARCHAR(64)   NULL      COMMENT '支付宝交易流水号',
  `fee`                     BIGINT        NULL      COMMENT '订单金额',
  `reason`                  VARCHAR(255)  NULL      COMMENT '退货款理由',
  `refund_amount`           BIGINT        NULL      COMMENT '退款金额',
  `total_earning`           BIGINT        NULL      COMMENT '收入,以分为单位',
  `total_expenditure`       BIGINT        NULL      COMMENT '支出,以分为单位',
  `seller_earning`          BIGINT        NULL      COMMENT '商家收入,以分为单位',
  `rrs_commission`          BIGINT        NULL      COMMENT '平台佣金收入,以分为单位',
  `score_earning`           BIGINT        NULL      COMMENT '平台积分收入,以分为单位',
  `presell_deposit`         BIGINT        NULL      COMMENT '营业外收入,以分为单位',
  `presell_commission`      BIGINT        NULL      COMMENT '主营业务成本,以分为单位',
  `third_party_commission`  BIGINT        NULL      COMMENT '第三方(如支付宝)佣金,以分为单位',
  `commission_rate`         DECIMAL(9,4)  NULL      DEFAULT 0.0000 COMMENT '佣金扣点',
  `settle_status`           SMALLINT      NOT NULL  COMMENT '结算状态 1:待结算,2:结算中(各收支，如佣金计算完毕),3:已确认,4:已结算,-1:结算失败',
  `fixed`                   BIT         NULL        COMMENT '是否补记账',
  `voucher`                 VARCHAR(64)   NULL      COMMENT '凭证号',
  `third_party_receipt`     VARCHAR(64)   NULL      COMMENT '第三方（支付宝手续费）发票号',
  `paid_at`                 DATETIME      NULL     COMMENT '付款时间',
  `settled_at`              DATETIME      NULL      COMMENT '结算时间',
  `confirmed_at`            DATETIME      NULL      COMMENT '确认时间',
  `created_at`              DATETIME      NULL      COMMENT '创建时间',
  `updated_at`              DATETIME      NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`));


CREATE INDEX idx_ris_order_id ON rrs_item_settlements (order_id);
CREATE INDEX idx_ris_settled_at ON rrs_item_settlements (settled_at);
CREATE INDEX idx_ris_paid_at ON rrs_item_settlements (paid_at);


-- -----------------------------------------------------
-- Table `rrs_deposit_fees` 商家保证金和技术服务费
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_deposit_fees`;

CREATE TABLE IF NOT EXISTS `rrs_deposit_fees` (
  `id`              BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `seller_id`       BIGINT      NOT NULL  COMMENT '商家id',
  `seller_name`     VARCHAR(64) NOT NULL  DEFAULT '' COMMENT '商家用户名',
  `shop_id`         BIGINT      NULL      COMMENT '店铺id',
  `shop_name`       VARCHAR(64) NULL      COMMENT '店铺名称',
  `outer_code`      VARCHAR(32) NULL      COMMENT '商家8码',
  `business`        INT         NULL      COMMENT '行业id',
  `deposit`         BIGINT      NULL      COMMENT '金额',
  `type`            INT         NULL      COMMENT '类型: 1、新增保证金 2、扣除保证金 3、技术服务费 4、扣保证金',
  `payment_type`    SMALLINT    NULL      COMMENT '付款方式, 0:支付宝 1:快捷通 ',
  `voucher`         VARCHAR(64) NULL      COMMENT '凭证号',
  `receipt`         VARCHAR(64) NULL      COMMENT '发票号',
  `description`     VARCHAR(64) NULL      COMMENT '备注',
  `auto`            TINYINT     NOT NULL  DEFAULT 0 COMMENT '是否系统自动创建 0:非, 1:是',
  `ordered`         SMALLINT    NOT NULL  DEFAULT 0 COMMENT '技术服务费JDE订单创建同完成 0:未完成, 1:已完成 , 当前仅当 type = 3 时',
  `synced`          SMALLINT    NOT NULL  DEFAULT 0 COMMENT '同步JDE完成 0:未完成, 1:已完成',
  `vouched`         SMALLINT    NOT NULL  DEFAULT 0 COMMENT '单据更新完成 0:未完成, 1:已完成',
  `receipted`       SMALLINT    NOT NULL  DEFAULT 0 COMMENT '发票更新完成 0:未完成, 1:已完成',
  `ordered_at`      DATETIME    NULL      COMMENT '技术服务费JDE订单创建同完成时间',
  `synced_at`       DATETIME    NULL      COMMENT 'JDE同步完成时间',
  `vouched_at`      DATETIME    NULL      COMMENT 'JDE打印凭证时间',
  `receipted_at`    DATETIME    NULL      COMMENT 'JDE打印发票时间',
  `created_at`      DATETIME    NULL      COMMENT '创建时间',
  `updated_at`      DATETIME    NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_rsf_seller_id ON `rrs_deposit_fees` (seller_id);
CREATE INDEX idx_rsf_synced_at ON `rrs_deposit_fees` (synced_at);


-- -----------------------------------------------------
-- Table `rrs_service_fees`  平台技术服务费
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_deposit_account`;

CREATE TABLE IF NOT EXISTS `rrs_deposit_account` (
  `id`            BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `seller_id`     BIGINT      NOT NULL  COMMENT '商家id',
  `seller_name`   VARCHAR(64) NOT NULL  COMMENT '商家账户',
  `shop_id`       BIGINT      NULL      COMMENT '店铺id',
  `shop_name`     VARCHAR(64) NULL      COMMENT '店铺名称',
  `business`      INT         NULL      COMMENT '订单所属行业id',
  `outer_code`    VARCHAR(32) NULL      COMMENT '商家8码',
  `balance`       BIGINT      NULL      COMMENT '商家保证金余额',
  `created_at`    DATETIME    NULL      COMMENT '创建时间',
  `updated_at`    DATETIME    NULL      COMMENT '修改时间',
  UNIQUE KEY (`seller_id`) ,
  UNIQUE KEY (`seller_name`) ,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `rrs_daily_settlements`   日订单汇总
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_daily_settlements`;

CREATE TABLE IF NOT EXISTS `rrs_daily_settlements` (
  `id`                      BIGINT    NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `order_count`             INT       NULL      COMMENT '订单数量',
  `total_earning`           BIGINT    NULL      COMMENT '收入,以分为单位',
  `total_expenditure`       BIGINT    NULL      COMMENT '支出,以分为单位',
  `seller_earning`          BIGINT    NULL      COMMENT '商家收入,以分为单位',
  `rrs_commission`          BIGINT    NULL      COMMENT '平台佣金收入,以分为单位',
  `score_earning`           BIGINT    NULL      COMMENT '平台积分收入,以分为单位',
  `presell_deposit`         BIGINT    NULL      COMMENT '营业外收入,以分为单位',
  `presell_commission`      BIGINT    NULL      COMMENT '主营业务成本,以分为单位',
  `third_party_commission`  BIGINT    NULL      COMMENT '第三方(如支付宝)佣金,以分为单位',
  `confirmed_at`            DATETIME  NULL      COMMENT '商家确认时间',
  `created_at`              DATETIME  NULL      COMMENT '创建时间',
  `updated_at`              DATETIME  NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_daily_confirmed_at_uniq` (`confirmed_at` ASC));

CREATE INDEX idx_rds_confirmed_at ON rrs_daily_settlements (confirmed_at);


-- -----------------------------------------------------
-- Table `rrs_seller_settlements`    商户订单日汇总
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_seller_settlements`;

CREATE TABLE IF NOT EXISTS `rrs_seller_settlements` (
  `id`                      BIGINT      NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `seller_id`               BIGINT      NOT NULL  COMMENT '商家id',
  `seller_name`             VARCHAR(64) NULL      COMMENT '商家名称',
  `outer_code`              VARCHAR(32) NULL      COMMENT '商家8码',
  `business`                INT         NULL      COMMENT '订单所属行业id 根据不同行业抽取不同的佣金润点',
  `order_count`             INT         NOT NULL  COMMENT '交易笔数',
  `total_earning`           BIGINT      NULL      COMMENT '收入,以分为单位',
  `total_expenditure`       BIGINT      NULL      COMMENT '支出,以分为单位',
  `seller_earning`          BIGINT      NULL      COMMENT '商家收入,以分为单位',
  `rrs_commission`          BIGINT      NULL      COMMENT '平台佣金收入,以分为单位',
  `score_earning`           BIGINT      NULL      COMMENT '平台积分收入,以分为单位',
  `presell_deposit`         BIGINT      NULL      COMMENT '预售定金扣除,以分为单位',
  `presell_commission`      BIGINT      NULL      COMMENT '主营业务成本,以分为单位',
  `third_party_commission`  BIGINT      NULL      COMMENT '第三方(如支付宝)佣金,以分为单位',
  `voucher`                 VARCHAR(64) NULL      COMMENT '凭证号',
  `third_party_receipt`     VARCHAR(64) NULL      COMMENT '第三方(支付宝)手续费凭证号',
  `settle_status`           SMALLINT    NOT NULL  COMMENT '结算状态 0:待结算,1:结算中(各收支，如佣金计算完毕),2:待确认,3:已确认,4:已结算,-1:结算失败',
  `confirmed`               SMALLINT    NOT NULL  DEFAULT 0  COMMENT '商家确认时间',
  `synced`                  SMALLINT    NOT NULL  DEFAULT 0  COMMENT '是否已同步JDE',
  `vouched`                 SMALLINT    NOT NULL  DEFAULT 0  COMMENT '是否已打印凭据',
  `receipted`               SMALLINT    NOT NULL  DEFAULT 0  COMMENT '是否已打印发票',
  `printed`                 SMALLINT    NULL      DEFAULT 0  COMMENT '是否已打印结算单',
  `confirmed_at`            DATETIME    NULL      COMMENT '商家确认时间',
  `synced_at`               DATETIME    NULL      COMMENT '同步JDE完成时间',
  `vouched_at`              DATETIME    NULL      COMMENT 'JDE打印凭证时间',
  `third_party_receipt_at`  DATETIME    NULL      COMMENT 'JDE打印第三方（支付宝手续费）发票时间',
  `printed_at`              DATETIME    NULL      COMMENT '打印结算单时间',
  `created_at`              DATETIME    NULL      COMMENT '创建时间',
  `updated_at`              DATETIME    NULL      COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_rss_confirmed_at ON rrs_seller_settlements (confirmed_at);
CREATE INDEX idx_rss_synced_at ON rrs_seller_settlements (synced_at);
CREATE INDEX idx_rss_seller_id ON rrs_seller_settlements (seller_id);



-- -----------------------------------------------------
-- Table `rrs_alipay_cash`     支付宝日汇总
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_alipay_cash`;

CREATE TABLE IF NOT EXISTS `rrs_alipay_cash` (
  `id`                      BIGINT    NOT NULL  AUTO_INCREMENT COMMENT '自增主键',
  `cash_total_count`        INT       NULL      DEFAULT 0 COMMENT '提现明细数量',
  `total_fee`               BIGINT    NOT NULL  COMMENT '总金额：总金额=总收入金额-总支出金额',
  `alipay_fee`              BIGINT    NOT NULL  COMMENT '支付宝手续费',
  `cash_fee`                BIGINT    NOT NULL  COMMENT '可提现金额: 可提现金额=总金额-支付宝手续费-退款金额',
  `refund_fee`              BIGINT    NULL      DEFAULT 0 COMMENT '退款金额',
  `status`                  SMALLINT  NOT NULL  COMMENT '状态：0:未提现  1:已提现',
  `summed_at`               DATETIME  NOT NULL  COMMENT '统计时间',
  `created_at`              DATETIME  NULL      COMMENT '创建时间',
  `updated_at`              DATETIME  NULL      COMMENT '修改时间',
  UNIQUE KEY (`summed_at`) ,
  PRIMARY KEY (`id`));

CREATE INDEX idx_rac_summed_at ON rrs_alipay_cash (summed_at);


-- -----------------------------------------------------
-- Table `rrs_seller_alipay_cash`  商户支付宝日汇总
-- -----------------------------------------------------

DROP TABLE IF EXISTS `rrs_seller_alipay_cash`;

CREATE TABLE IF NOT EXISTS `rrs_seller_alipay_cash` (
  `id`                      BIGINT      NOT NULL    AUTO_INCREMENT COMMENT '自增主键',
  `seller_id`               BIGINT      NOT NULL    COMMENT '商家id',
  `seller_name`             VARCHAR(64) NULL        COMMENT '商家名称',
  `outer_code`              VARCHAR(32) NULL        COMMENT '商家8码',
  `business`                INT         NULL        COMMENT '行业编码 1:家电 2:家具 3:家装 4:家饰 5:净水',
  `cash_total_count`        INT         NULL        DEFAULT 0 COMMENT '提现明细数量',
  `total_fee`               BIGINT      NOT NULL    COMMENT '总金额：总金额=总收入金额-总支出金额',
  `alipay_fee`              BIGINT      NOT NULL    COMMENT '支付宝手续费',
  `cash_fee`                BIGINT      NOT NULL    COMMENT '可提现金额: 可提现金额=总金额-支付宝手续费-退款金额',
  `refund_fee`              BIGINT      NULL        DEFAULT 0 COMMENT '退款金额',
  `voucher`                 VARCHAR(64) NULL        COMMENT '凭证号',
  `status`                  SMALLINT    NOT NULL    COMMENT '状态：0:未提现  1:已提现',
  `synced`                  SMALLINT    NOT NULL    DEFAULT 0 COMMENT '同步JDE完成 0:未完成, 1:已完成',
  `vouched`                 SMALLINT    NOT NULL    DEFAULT 0 COMMENT '是否打印凭证 0:未完成, 1:已完成',
  `synced_at`               DATETIME    NULL        COMMENT '同步时间',
  `vouched_at`              DATETIME    NULL        COMMENT 'JDE打印凭证时间',
  `summed_at`               DATETIME    NOT NULL    COMMENT '统计时间',
  `created_at`              DATETIME    NULL        COMMENT '创建时间',
  `updated_at`              DATETIME    NULL        COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_rsac_summed_at ON rrs_seller_alipay_cash (summed_at);
CREATE INDEX idx_rsac_union ON rrs_seller_alipay_cash (summed_at, seller_id);


-- -----------------------------------------------------
-- Table `rrs_settle_jobs`  对账任务
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_settle_jobs`;

CREATE TABLE IF NOT EXISTS `rrs_settle_jobs` (
  `id`            BIGINT   NOT NULL   AUTO_INCREMENT COMMENT '自增主键',
  `dependency_id` BIGINT   NULL       DEFAULT -1 COMMENT '依赖的任务标识',
  `type`          INT      NOT NULL   COMMENT '处理类型: 1:标记完成订单，2:结算订单, 3:日汇总报表',
  `status`        SMALLINT NOT NULL   COMMENT '状态：0:未处理,1:处理中,2:处理完成,-1:处理失败',
  `cost`          BIGINT   NULL       COMMENT '处理耗时',
  `done_at`       DATETIME NOT NULL   COMMENT '处理时间',
  `traded_at`     DATETIME NOT NULL   COMMENT '交易时间',
  `created_at`    DATETIME NULL       COMMENT '创建时间',
  `updated_at`    DATETIME NULL       COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_rsj_done_at ON rrs_settle_jobs (done_at);
CREATE INDEX idx_rsj_traded_at ON rrs_settle_jobs (traded_at);
CREATE INDEX idx_rsj_union ON rrs_settle_jobs (done_at, `type`);


-- -----------------------------------------------------
-- Table `rrs_business_rates`  费率
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_business_rates`;

CREATE TABLE IF NOT EXISTS `rrs_business_rates` (
  `id`            BIGINT        NOT NULL   AUTO_INCREMENT COMMENT '自增主键',
  `business`      INT           NOT NULL   COMMENT '业务类型',
  `rate`          DECIMAL(5,2)  NOT NULL   COMMENT '费率',
  `created_at`    DATETIME      NULL       COMMENT '创建时间',
  `updated_at`    DATETIME      NULL       COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_rbr_business_uniq` (`business` ASC));


-- -----------------------------------------------------
-- Table `rrs_alipay_trans`  支付宝对账查询记录
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_alipay_trans_load`;

CREATE TABLE IF NOT EXISTS `rrs_alipay_trans_load` (
  `id`                          BIGINT        NOT NULL   AUTO_INCREMENT COMMENT '自增主键',
  `query_start`                 DATETIME      NOT NULL   COMMENT '查询启动日期',
  `query_end`                   DATETIME      NOT NULL   COMMENT '查询截止日期',
  `page_no`                     INT           NOT NULL   COMMENT '查询页',
  `page_size`                   INT           NOT NULL   COMMENT '分页大小',
  `status`                      INT           NOT NULL   COMMENT '1:处理完成 -1:存在问题(需要重跑)',
  `next`                        TINYINT       NOT NULL   DEFAULT '0' COMMENT '是否存在下一批次',
  `created_at`                  DATETIME      NULL       COMMENT '创建时间',
  `updated_at`                  DATETIME      NULL       COMMENT '修改时间',
  PRIMARY KEY (`id`)
);


-- -----------------------------------------------------
-- Table `rrs_alipay_trans`  支付宝交易对账数据
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_alipay_trans`;

CREATE TABLE IF NOT EXISTS `rrs_alipay_trans` (
  `id`                          BIGINT        NOT NULL   AUTO_INCREMENT COMMENT '自增主键',
  `balance`                     VARCHAR(32)   NULL       COMMENT '账户余额',
  `bank_account_name`           VARCHAR(32)   NULL       COMMENT '银行账户名称',
  `bank_account_no`             VARCHAR(32)   NULL       COMMENT '银行账户',
  `bank_name`                   VARCHAR(64)   NULL       COMMENT '银行名',
  `buyer_name`                  VARCHAR(127)  NULL       COMMENT '买家姓名',
  `buyer_account`               VARCHAR(32)   NULL       COMMENT '买家账户',
  `currency`                    VARCHAR(16)   NULL       COMMENT '货币代码(156:人民币)',
  `deposit_bank_no`             VARCHAR(32)   NULL       COMMENT '充值网银流水',
  `income`                      VARCHAR(32)   NULL       COMMENT '收入金额',
  `iw_account_log_id`           VARCHAR(32)   NULL       COMMENT '帐务流水',
  `memo`                        VARCHAR(127)  NULL       COMMENT '备注信息',
  `merchant_out_order_no`       VARCHAR(64)   NULL       COMMENT '外部交易编号（订单号）',
  `other_account_email`         VARCHAR(127)  NULL       COMMENT '帐务对方邮箱',
  `other_account_fullname`      VARCHAR(127)  NULL       COMMENT '帐务对方全称',
  `other_user_id`               VARCHAR(32)   NULL       COMMENT '帐务对方支付宝用户号',
  `outcome`                     VARCHAR(32)   NULL       COMMENT '支出金额',
  `partner_id`                  VARCHAR(32)   NULL       COMMENT '合作者身份id',
  `seller_account`              VARCHAR(32)   NULL       COMMENT '买家支付宝人民币支付帐号(user_id+0156)',
  `seller_fullname`             VARCHAR(64)   NULL       COMMENT '卖家姓名',
  `service_fee`                 VARCHAR(32)   NULL       COMMENT '交易服务费',
  `service_fee_ratio`           VARCHAR(16)   NULL       COMMENT '交易服务费率',
  `total_fee`                   VARCHAR(32)   NULL       COMMENT '交易总金额',
  `trade_no`                    VARCHAR(32)   NULL       COMMENT '支付宝交易流水',
  `trade_refund_amount`         VARCHAR(32)   NULL       COMMENT '累计退款金额',
  `trans_account`               VARCHAR(32)   NULL       COMMENT '帐务本方支付宝人民币资金帐号(user_id+0156)',
  `trans_code_msg`              VARCHAR(16)   NULL       COMMENT '业务类型',
  `trans_date`                  VARCHAR(32)   NULL       COMMENT '交易发生日期',
  `trans_out_order_no`          VARCHAR(32)   NULL       COMMENT '商户订单号',
  `sub_trans_code_msg`          VARCHAR(32)   NULL       COMMENT '子业务类型代码，详见文档',
  `sign_product_name`           VARCHAR(32)   NULL       COMMENT '签约产品',
  `rate`                        VARCHAR(16)   NULL       COMMENT '费率',
  `created_at`                  DATETIME      NULL       COMMENT '创建时间',
  `updated_at`                  DATETIME      NULL       COMMENT '修改时间',
  PRIMARY KEY (`id`)
 );

CREATE INDEX idx_rat_trans_no ON rrs_alipay_trans (trade_no);
CREATE INDEX idx_rat_merchant_no ON rrs_alipay_trans (merchant_out_order_no);



-- -----------------------------------------------------
-- Table `rrs_abnormal_trans`  挂账
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_abnormal_trans`;

CREATE TABLE IF NOT EXISTS `rrs_abnormal_trans` (
  `id`                BIGINT        NOT NULL    AUTO_INCREMENT COMMENT '自增主键',
  `settlement_id`     BIGINT        NOT NULL    COMMENT '帐务id',
  `order_id`          BIGINT        NOT NULL    COMMENT '订单id',
  `reason`            VARCHAR(64)   NOT NULL    COMMENT '挂账原因',
  `created_at`        DATETIME      NULL        COMMENT '创建时间',
  `updated_at`        DATETIME      NULL        COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX `idx_abnormal_settlement_id_uniq` ON rrs_abnormal_trans (`settlement_id` ASC);

-- -----------------------------------------------------
-- Table `rrs_deposit_fee_cash`  支付宝押金提现单
-- -----------------------------------------------------
DROP TABLE IF EXISTS `rrs_deposit_fee_cash`;

CREATE TABLE IF NOT EXISTS `rrs_deposit_fee_cash` (
  `id`          BIGINT      NOT NULL    AUTO_INCREMENT COMMENT '自增主键',
  `deposit_id`  BIGINT      NULL        COMMENT '关联的基础费用id',
  `seller_id`   BIGINT      NOT NULL    COMMENT '商家id',
  `seller_name` VARCHAR(64) NULL        COMMENT '商家名称',
  `shop_id`     BIGINT      NULL        COMMENT '店铺id',
  `shop_name`   VARCHAR(64) NULL        COMMENT '店铺名称',
  `outer_code`  VARCHAR(32) NULL        COMMENT '商家8码',
  `business`    INT         NULL        COMMENT '行业id',
  `cash_fee`    BIGINT      NOT NULL    COMMENT '可提现金额: 可提现金额=总金额-支付宝手续费',
  `cash_type`   INT         NULL        COMMENT '类型: 1.保证金, 2:技术服务费',
  `status`      SMALLINT    NOT NULL    COMMENT '状态: 1:已提现',
  `voucher`     VARCHAR(64) NULL        COMMENT '凭证号',
  `vouched`     SMALLINT    NOT NULL    DEFAULT 0 COMMENT '单据更新完成 0:未完成, 1:已完成',
  `synced`      SMALLINT    NOT NULL    DEFAULT 0 COMMENT '同步JDE完成 0:未完成, 1:已完成',
  `synced_at`   DATETIME    NULL        COMMENT '同步时间',
  `vouched_at`  DATETIME    NULL        COMMENT 'JDE打印凭证时间',
  `created_at`  DATETIME    NULL        COMMENT '创建时间',
  `updated_at`  DATETIME    NULL        COMMENT '修改时间',
  UNIQUE INDEX `idx_daily_deposit_id_uniq` (`deposit_id` ASC),
  PRIMARY KEY (`id`)
);

-- -----------------------------------------------------
-- Table `brands_sellers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `brands_sellers`;
create table `brands_sellers` (
  `brand_id`    BIGINT      NOT NULL      COMMENT '品牌id',
  `brand_name`  VARCHAR(64) DEFAULT NULL  COMMENT '品牌名',
  `seller_id`   BIGINT      NOT NULL      COMMENT '卖家id',
  `seller_name` VARCHAR(64) DEFAULT NULL  COMMENT '卖家',
  `shop_id`     BIGINT      NOT NULL      COMMENT '店铺id'
);

create INDEX idx_brands_sellers_brand_id on brands_sellers(`brand_id`);
create INDEX idx_brands_sellers_seller_id on brands_sellers(`seller_id`);




-- -----------------------------------------------------
-- Table `rrs_order_alipay_cash`  订单提现明细
-- -----------------------------------------------------

DROP TABLE IF EXISTS `rrs_order_alipay_cash`;

CREATE TABLE IF NOT EXISTS `rrs_order_alipay_cash` (
  `id`              BIGINT        NOT NULL    AUTO_INCREMENT COMMENT '自增主键',
  `order_id`        BIGINT        NOT NULL    COMMENT '订单id',
  `order_item_id`   BIGINT        NULL        COMMENT '子订单id',
  `type`            SMALLINT      NOT NULL    COMMENT '订单类型',
  `buyer_id`        BIGINT        NOT NULL    COMMENT '买家id',
  `buyer_name`      VARCHAR(64)   NULL        COMMENT '买家名称',
  `seller_id`       BIGINT        NOT NULL    COMMENT '商家id',
  `seller_name`     VARCHAR(64)   NULL        COMMENT '商家名称',
  `shop_id`         BIGINT        NULL        COMMENT '店铺id',
  `shop_name`       VARCHAR(255)  NULL        COMMENT '店铺名称',
  `total_fee`       BIGINT        NOT NULL    COMMENT '总金额：总金额 = 总收入金额-总支出金额',
  `alipay_fee`      BIGINT        NOT NULL    COMMENT '支付宝手续费',
  `cash_fee`        BIGINT        NOT NULL    COMMENT '可提现金额: 可提现金额=总金额-支付宝手续费-退款金额',
  `refund_fee`      BIGINT        NULL        DEFAULT 0 COMMENT '退款金额',
  `status`          SMALLINT      NOT NULL    COMMENT '状态：0:未提现  1:已提现',
  `fixed`           BIT           NULL        COMMENT '是否补记账',
  `voucher`         VARCHAR(64)   NULL        COMMENT '凭证号',
  `operator`        VARCHAR(64)   NULL        COMMENT '提现人',
  `traded_at`       DATETIME      NOT NULL    COMMENT '记录时间',
  `cashed_at`       DATETIME      NULL        COMMENT '提现时间',
  `cash_type`       SMALLINT      NULL        COMMENT '提现类型：1:普通订单提现-1:普通订单退款提现,2 预售定金提现 -2 预售定金退款 3尾款-3尾款退款提现',
  `created_at`      DATETIME      NULL        COMMENT '创建时间',
  `updated_at`      DATETIME      NULL        COMMENT '修改时间',
  PRIMARY KEY (`id`));

CREATE INDEX idx_roac_traded_at ON rrs_order_alipay_cash (traded_at);
CREATE INDEX idx_roac_order_id ON rrs_order_alipay_cash (order_id);
CREATE INDEX idx_roac_seller_id ON rrs_order_alipay_cash (seller_id);




-- --------------------------------------------------------
-- Table `mocked_alipay_trans`  支付宝交易对账数据（模拟数据，仅在测试环境使用）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `mocked_alipay_trans`;

CREATE TABLE IF NOT EXISTS `mocked_alipay_trans` (
  `id`                          BIGINT        NOT NULL   AUTO_INCREMENT COMMENT '自增主键',
  `balance`                     VARCHAR(32)   NULL       COMMENT '账户余额',
  `bank_account_name`           VARCHAR(32)   NULL       COMMENT '银行账户名称',
  `bank_account_no`             VARCHAR(32)   NULL       COMMENT '银行账户',
  `bank_name`                   VARCHAR(64)   NULL       COMMENT '银行名',
  `buyer_name`                  VARCHAR(127)  NULL       COMMENT '买家姓名',
  `buyer_account`               VARCHAR(32)   NULL       COMMENT '买家账户',
  `currency`                    VARCHAR(16)   NULL       COMMENT '货币代码(156:人民币)',
  `deposit_bank_no`             VARCHAR(32)   NULL       COMMENT '充值网银流水',
  `income`                      VARCHAR(32)   NULL       COMMENT '收入金额',
  `iw_account_log_id`           VARCHAR(32)   NULL       COMMENT '帐务流水',
  `memo`                        VARCHAR(127)  NULL       COMMENT '备注信息',
  `merchant_out_order_no`       VARCHAR(64)   NULL       COMMENT '外部交易编号（订单号）',
  `other_account_email`         VARCHAR(127)  NULL       COMMENT '帐务对方邮箱',
  `other_account_fullname`      VARCHAR(127)  NULL       COMMENT '帐务对方全称',
  `other_user_id`               VARCHAR(32)   NULL       COMMENT '帐务对方支付宝用户号',
  `outcome`                     VARCHAR(32)   NULL       COMMENT '支出金额',
  `partner_id`                  VARCHAR(32)   NULL       COMMENT '合作者身份id',
  `seller_account`              VARCHAR(32)   NULL       COMMENT '买家支付宝人民币支付帐号(user_id+0156)',
  `seller_fullname`             VARCHAR(64)   NULL       COMMENT '卖家姓名',
  `service_fee`                 VARCHAR(32)   NULL       COMMENT '交易服务费',
  `service_fee_ratio`           VARCHAR(16)   NULL       COMMENT '交易服务费率',
  `total_fee`                   VARCHAR(32)   NULL       COMMENT '交易总金额',
  `trade_no`                    VARCHAR(32)   NULL       COMMENT '支付宝交易流水',
  `trade_refund_amount`         VARCHAR(32)   NULL       COMMENT '累计退款金额',
  `trans_account`               VARCHAR(32)   NULL       COMMENT '帐务本方支付宝人民币资金帐号(user_id+0156)',
  `trans_code_msg`              VARCHAR(16)   NULL       COMMENT '业务类型',
  `trans_date`                  VARCHAR(32)   NULL       COMMENT '交易发生日期',
  `trans_out_order_no`          VARCHAR(32)   NULL       COMMENT '商户订单号',
  `sub_trans_code_msg`          VARCHAR(32)   NULL       COMMENT '子业务类型代码，详见文档',
  `sign_product_name`           VARCHAR(32)   NULL       COMMENT '签约产品',
  `rate`                        VARCHAR(16)   NULL       COMMENT '费率',
  `created_at`                  DATETIME      NULL       COMMENT '创建时间',
  `updated_at`                  DATETIME      NULL       COMMENT '修改时间',
  PRIMARY KEY (`id`)
);
