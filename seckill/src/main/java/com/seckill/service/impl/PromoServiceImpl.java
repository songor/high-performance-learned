package com.seckill.service.impl;

import com.seckill.dao.PromoDOMapper;
import com.seckill.dataobject.PromoDO;
import com.seckill.model.PromoModel;
import com.seckill.service.PromoService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class PromoServiceImpl implements PromoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromoServiceImpl.class);

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoModel promoModel = new PromoModel();

        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        if (promoDO == null) {
            promoModel.setStatus(0);
            return promoModel;
        }

        try {
            BeanUtils.copyProperties(promoModel, promoDO);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        promoModel.setItemPrice(BigDecimal.valueOf(promoDO.getItemPrice()));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.ofInstant(promoModel.getStartDate().toInstant(), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(promoDO.getEndDate().toInstant(), ZoneId.systemDefault());
        if (now.isBefore(start)) {
            promoModel.setStatus(1);
        } else if (now.isAfter(end)) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

}
