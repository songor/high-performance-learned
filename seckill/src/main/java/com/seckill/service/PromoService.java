package com.seckill.service;

import com.seckill.model.PromoModel;

public interface PromoService {

    PromoModel getPromoByItemId(Integer itemId);

    void publish(Integer promoId);

    String generateSecKillToken(Integer userId, Integer itemId, Integer promoId);

}
