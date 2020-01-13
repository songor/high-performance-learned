package com.seckill.service.impl;

import com.seckill.dao.PromoDOMapper;
import com.seckill.dataobject.PromoDO;
import com.seckill.model.ItemModel;
import com.seckill.model.PromoModel;
import com.seckill.model.UserModel;
import com.seckill.service.ItemService;
import com.seckill.service.PromoService;
import com.seckill.service.UserService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromoServiceImpl.class);

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

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
        LocalDateTime end = LocalDateTime.ofInstant(promoModel.getEndDate().toInstant(), ZoneId.systemDefault());
        if (now.isBefore(start)) {
            promoModel.setStatus(1);
        } else if (now.isAfter(end)) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public void publish(Integer promoId) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO == null || promoDO.getItemId() == null || promoDO.getItemId() == 0) {
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
        // 秒杀大闸
        redisTemplate.opsForValue().set("promo_door_" + promoId, itemModel.getStock() * 5);
    }

    @Override
    public String generateSecKillToken(Integer userId, Integer itemId, Integer promoId) {
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            return null;
        }

        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null) {
            return null;
        }

        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            return null;
        }

        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO == null) {
            return null;
        }

        int status = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.ofInstant(promoDO.getStartDate().toInstant(), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(promoDO.getEndDate().toInstant(), ZoneId.systemDefault());
        if (now.isBefore(start)) {
            status = 1;
        } else if (now.isAfter(end)) {
            status = 3;
        } else {
            status = 2;
        }

        if (status != 2) {
            return null;
        }

        long result = redisTemplate.opsForValue().increment("promo_door_" + promoId, -1);
        if (result < 0) {
            return null;
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String key = "promo_token_" + userId + "_" + itemId + "_" + promoId;
        redisTemplate.opsForValue().set(key, token);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
        return token;
    }

}
