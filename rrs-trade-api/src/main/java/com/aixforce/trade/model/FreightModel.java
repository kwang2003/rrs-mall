package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:运费模板
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-21.
 */
@ToString
@EqualsAndHashCode
public class FreightModel implements Serializable {
    private static final long serialVersionUID = 5403968896483842086L;

    @Getter
    @Setter
    private Long id;                //自增主键

    @Getter
    @Setter
    private Long sellerId;          //商家编号

    @Getter
    @Setter
    private String modelName;       //模型名称

    @Getter
    @Setter
    private Integer countWay;       //计价方式（1:件数,2:尺寸,3:重量->后两个后期扩展）

    @Getter
    @Setter
    private Integer costWay;        //费用方式（1:买家承担,2:卖家承担）

    @Getter
    @Setter
    private Integer firstAmount;    //首批数量在N范围内

    @Getter
    @Setter
    private Integer firstFee;       //首批价格多少

    @Getter
    @Setter
    private Integer addAmount;      //每增加N数量

    @Getter
    @Setter
    private Integer addFee;         //增加N价格

    @Getter
    @Setter
    private Integer status;         //标注运费模板的状态，是否已经逻辑删除

    @Getter
    @Setter
    private Integer specialExist;   //是否存在特殊地区设定(0:不存在，1:存在.方便select)

    @Getter
    @Setter
    private Integer bindItemNum;    //给模板下绑定的商品数量（一个查询值）

    @Getter
    @Setter
    private Date createdAt;          //创建时间

    @Getter
    @Setter
    private Date updatedAt;          //修改时间

    /**
     * 计价方式
     */
    public enum CountWay{
        NUMBER(1 , "件数"),SIZE(2, "尺寸"),WEIGHT(3 , "重量");

        private final Integer index;

        private final String value;

        private CountWay(Integer index , String value){
            this.index = index;
            this.value = value;
        }

        public static CountWay from(Integer index){
            for(CountWay countWay : CountWay.values()){
                if(Objects.equal(index , countWay.index)){
                    return countWay;
                }
            }
            return null;
        }

        public Integer toNumber() {
            return this.index;
        }

        @Override
        public String toString(){
            return value;
        }
    }

    public enum CostWay{
        BEAR_BUYER(1 , "买家承担"),BEAR_SELLER(2, "卖家承担");

        private final Integer index;

        private final String value;

        private CostWay(Integer index , String value){
            this.index = index;
            this.value = value;
        }

        public static CostWay from(Integer index){
            for(CostWay costWay : CostWay.values()){
                if(Objects.equal(index , costWay.index)){
                    return costWay;
                }
            }
            return null;
        }

        @Override
        public String toString(){
            return value;
        }
    }

    public enum Status {
        ENABLED(1, "启用"),
        DISABLED(-1, "禁用，逻辑删除");

        private Integer value;

        private String description;

        private Status(Integer value, String description) {
            this.value = value;
            this.description = description;
        }

        public static Status fromNumber(Integer number) {
            for(Status status : Status.values()) {
                if(Objects.equal(status.value, number)) {
                    return status;
                }
            }
            return null;
        }

        public int value() {
            return this.value;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
