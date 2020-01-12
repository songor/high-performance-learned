package com.seckill.service.impl;

import com.seckill.dao.ItemDOMapper;
import com.seckill.dao.ItemStockDOMapper;
import com.seckill.dao.StockLogDOMapper;
import com.seckill.dataobject.ItemDO;
import com.seckill.dataobject.ItemStockDO;
import com.seckill.dataobject.StockLogDO;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.ItemModel;
import com.seckill.model.PromoModel;
import com.seckill.rocketmq.StockProducer;
import com.seckill.service.ItemService;
import com.seckill.service.PromoService;
import com.seckill.validator.CustomValidator;
import com.seckill.validator.ValidationResult;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

    private static final String ITEM_VALIDATE_PREFIX = "item_validate_";

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private CustomValidator validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StockProducer stockProducer;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Transactional
    @Override
    public ItemModel createItem(ItemModel itemModel) {
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, result.getErrorMessage());
        }

        ItemDO itemDO = new ItemDO();
        try {
            BeanUtils.copyProperties(itemDO, itemModel);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        itemDOMapper.insertSelective(itemDO);

        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemDO.getId());
        itemStockDO.setStock(itemModel.getStock());
        itemStockDOMapper.insertSelective(itemStockDO);

        return getItemById(itemDO.getId());
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemModel itemModel = new ItemModel();

        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }
        try {
            BeanUtils.copyProperties(itemModel, itemDO);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        itemModel.setPrice(BigDecimal.valueOf(itemDO.getPrice()));

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        itemModel.setStock(itemStockDO.getStock());

        PromoModel promoModel = promoService.getPromoByItemId(itemDO.getId());
        if (promoModel.getStatus() != 0 && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemModel itemModel = new ItemModel();

            try {
                BeanUtils.copyProperties(itemModel, itemDO);
            } catch (Exception e) {
                LOGGER.error("Copy properties failure", e);
            }
            itemModel.setPrice(BigDecimal.valueOf(itemDO.getPrice()));

            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            itemModel.setStock(itemStockDO.getStock());

            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    //    @Transactional
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) {
//        int affectedCount = itemStockDOMapper.decreaseStock(itemId, amount);
        long remaining = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, -1 * amount);
//        if (remaining >= 0) {
        /**
         * （1）如果 createOrder() 方法抛出异常，已经执行的 asyncDecrease() 方法无法回滚，因此在 createOrder() 方法 commit 之后，
         * 再执行 asyncDecrease() 方法
         */
//            boolean result = stockProducer.asyncDecrease(itemId, amount);
//            if (!result) {
//                redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount);
//                return false;
//            }
//            return true;
        if (remaining > 0) {
            return true;
        } else if (remaining == 0) {
            // 库存售罄
            redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId, "true");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        return stockProducer.asyncDecrease(itemId, amount);
    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount);
        return true;
    }

    @Transactional
    @Override
    public void increaseSales(Integer itemId, Integer amount) {
        itemDOMapper.increaseSales(itemId, amount);
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        String key = ITEM_VALIDATE_PREFIX + id;
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get(key);
        if (itemModel == null) {
            itemModel = getItemById(id);
            redisTemplate.opsForValue().set(key, itemModel);
            redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Transactional
    @Override
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-", ""));
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStatus(1);
        stockLogDOMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }

}
