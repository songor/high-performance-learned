package com.seckill.dataobject;

import java.util.Date;

public class PromoDO {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column promo.id
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column promo.name
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column promo.start_date
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    private Date startDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column promo.end_date
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    private Date endDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column promo.item_id
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    private Integer itemId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column promo.item_price
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    private Double itemPrice;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column promo.id
     *
     * @return the value of promo.id
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column promo.id
     *
     * @param id the value for promo.id
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column promo.name
     *
     * @return the value of promo.name
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column promo.name
     *
     * @param name the value for promo.name
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column promo.start_date
     *
     * @return the value of promo.start_date
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column promo.start_date
     *
     * @param startDate the value for promo.start_date
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column promo.end_date
     *
     * @return the value of promo.end_date
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column promo.end_date
     *
     * @param endDate the value for promo.end_date
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column promo.item_id
     *
     * @return the value of promo.item_id
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column promo.item_id
     *
     * @param itemId the value for promo.item_id
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column promo.item_price
     *
     * @return the value of promo.item_price
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public Double getItemPrice() {
        return itemPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column promo.item_price
     *
     * @param itemPrice the value for promo.item_price
     *
     * @mbg.generated Sat Dec 14 23:53:54 CST 2019
     */
    public void setItemPrice(Double itemPrice) {
        this.itemPrice = itemPrice;
    }
}