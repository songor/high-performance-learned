package com.seckill.service;

import com.seckill.model.OrderModel;

public interface OrderService {

    OrderModel createOrder(Integer userId, Integer itemId, Integer amount, Integer promoId, String stockLogId);

}
