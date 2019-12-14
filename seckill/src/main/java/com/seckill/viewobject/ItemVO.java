package com.seckill.viewobject;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemVO {

    private Integer id;

    private String title;

    private BigDecimal price;

    private Integer stock;

    private String description;

    private Integer sales;

    private String imageUrl;

    private Integer promoStatus;

    private Integer promoId;

    private BigDecimal promoPrice;

    private String promoStartDate;

    private String promoEndDate;

}
