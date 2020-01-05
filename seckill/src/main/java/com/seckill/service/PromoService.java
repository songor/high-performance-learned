package com.seckill.service;

import com.seckill.model.PromoModel;

public interface PromoService {

    PromoModel getPromoByItemId(Integer itemId);

    void publish(Integer promoId);

}
