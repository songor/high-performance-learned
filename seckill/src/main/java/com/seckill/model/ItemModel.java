package com.seckill.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemModel {

    private Integer id;

    private String title;

    private BigDecimal price;

    private BigDecimal stock;

    private String description;

    private Integer sales;

    private String imageUrl;

}
