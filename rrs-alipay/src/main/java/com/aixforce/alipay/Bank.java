package com.aixforce.alipay;

import com.aixforce.alipay.exception.BankNotFoundException;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-19 11:37 AM  <br>
 * Author: xiao
 */
public enum  Bank {

    SHBANK("SHBANK", "上海银行", ""),
    BOC("BOCB2C", "中国银行", ""),
    ICBC("ICBCB2C", "中国工商银行", ""),
    ABC("ABC", "中国农业银行", ""),
    CMB("CMB", "招商银行", ""),
    CIB("CIB", "兴业银行", ""),
    CITIC("CITIC", "中信银行", ""),
    CEB("CEB-DEBIT", "中国光大银行", ""),
    PSBC("POSTGC", "中国邮政储蓄银行", ""),
    CCB("CCB", "中国建设银行", ""),
    SPABANK("SPABANK", "平安银行", ""),
    HZCB("HZCBB2C", "杭州银行", ""),
    FDB("FDB", "富滇银行", ""),
    BJRCB("BJRCB", "北京农商银行", ""),
    SPDB("SPDB", "上海浦发银行", ""),
    NBBANK("NBBANK", "宁波银行", ""),
    CMBC("CMBC", "中国民生银行", ""),
    GDB("GDB", "广发银行", ""),
    ICBCBTB("ICBCBTB", "中国工商银行(企业银行)", ""),
    ABCBTB("ABCBTB", "中国农业银行(企业银行)", ""),
    CCBBTB("CCBBTB", "中国建设银行(企业银行)", ""),
    SPDBB2B("SPDBB2B", "上海浦东发展银行(企业银行)", ""),
    BOCBTB("BOCBTB", "中国银行(企业银行)", ""),
    CMBBTB("CMBBTB", "招商银行(企业银行)", ""),
    SCAN_ALIPAY("SCAN-ALIPAY", "支付宝扫描", ""),
    ALIPAY("ALIPAY", "支付宝", ""),
    BOC_CCIP("BOC-CCIP", "中国银行信用卡分期", ""),
    BOC_CCIP_3("BOC-CCIP-3", "中国银行信用卡分3期", ""),
    BOC_CCIP_6("BOC-CCIP-6", "中国银行信用卡分6期", ""),
    BOC_CCIP_9("BOC-CCIP-9", "中国银行信用卡分9期", ""),
    BOC_CCIP_12("BOC-CCIP-12", "中国银行信用卡分12期", ""),
    CCB_CCIP("CCB-CCIP", "中国建设银行信用卡分期", ""),
    CCB_CCIP_3("CCB-CCIP-3", "中国建设银行信用卡分3期", ""),
    CCB_CCIP_6("CCB-CCIP-6", "中国建设银行信用卡分6期", ""),
    CCB_CCIP_12("CCB-CCIP-12", "中国建设银行信用卡分12期", ""),
    CCB_CCIP_18("CCB-CCIP-18", "中国建设银行信用卡分18期", ""),
    CCB_CCIP_24("CCB-CCIP-24", "中国建设银行信用卡分24期", ""),
    CCB_CCIP_FREE("CCB-CCIP-FREE", "中国建设银行信用卡分期免分期费", ""),
    ABC_CCIP("ABC-CCIP", "中国农业银行信用卡分期", ""),
    ABC_CCIP_3("ABC-CCIP-3", "中国农业银行信用卡分3期", ""),
    ABC_CCIP_6("ABC-CCIP-6", "中国农业银行信用卡分6期", ""),
    ABC_CCIP_9("ABC-CCIP-9", "中国农业银行信用卡分9期", ""),
    ABC_CCIP_12("ABC-CCIP-12", "中国农业银行信用卡分12期", ""),
    SPDB_CCIP("SPDB-CCIP", "上海浦发银行信用卡分期", ""),
    SPDB_CCIP_3("SPDB-CCIP-3", "上海浦发银行信用卡分3期", ""),
    SPDB_CCIP_6("SPDB-CCIP-6", "上海浦发银行信用卡分6期", ""),
    SPDB_CCIP_12("SPDB-CCIP-12", "上海浦发银行信用卡分12期", ""),
    SPABANK_CCIP("SPABANK-CCIP", "平安银行信用卡分期", ""),
    SPABANK_CCIP_3("SPABANK-CCIP-3", "平安银行信用卡分3期", ""),
    SPABANK_CCIP_6("SPABANK-CCIP-6", "平安银行信用卡分6期", ""),
    SPABANK_CCIP_12("SPABANK-CCIP-12", "平安银行信用卡分12期", ""),
    CEB_CCIP("CEB-CCIP", "中国光大银行信用卡分期", ""),
    CEB_CCIP_3("CEB-CCIP-3", "中国光大银行信用卡分3期", ""),
    CEB_CCIP_6("CEB-CCIP-6", "中国光大银行信用卡分6期", ""),
    CEB_CCIP_12("CEB-CCIP-12", "中国光大银行信用卡分12期", ""),
    CMB_CCIP("CMB-CCIP", "招商银行信用卡分期", ""),
    CIB_CCIP("CIB-CCIP", "兴业银行信用卡分期", ""),
    CIB_CCIP_3("CIB-CCIP-3", "兴业银行信用卡分3期", ""),
    CIB_CCIP_6("CIB-CCIP-6", "兴业银行信用卡分6期", ""),
    CIB_CCIP_12("CIB-CCIP-12", "兴业银行信用卡分12期", "");

    private final String value;
    private final String display;
    private final String icon;




    private Bank(String value, String display, String icon) {
        this.value = value;
        this.display = display;
        this.icon = icon;
    }

    public static Bank from(String value) {
        for (Bank bank : Bank.values()) {
            if (Objects.equal(bank.value(), value)) {
                return bank;
            }
        }
        throw new BankNotFoundException("bank not found by value: " + value);
    }

    /**
     * 获取所有银行
     *
     * @return 银行列表
     */
    public static List<Bank> getBanks() {
        List<Bank> knownList = Lists.newArrayList();
        Collections.addAll(knownList, Bank.values());
        return knownList;
    }

    public String value() {
        return value;
    }

    public String icon() {
        return icon;
    }

    @Override
    public String toString() {
        return display;
    }
}
