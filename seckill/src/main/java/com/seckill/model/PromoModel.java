package com.seckill.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class PromoModel {

    private Integer id;

    private String name;

    /**
     * 0 没有促销
     * 1 未开始
     * 2 进行中
     * 3 已结束
     */
    private Integer status;

    private Date startDate;

    private Date endDate;

    private Integer itemId;

    private BigDecimal itemPrice;

}
