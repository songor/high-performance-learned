package com.seckill.service.impl;

import com.seckill.dao.ItemDOMapper;
import com.seckill.dao.ItemStockDOMapper;
import com.seckill.dataobject.ItemDO;
import com.seckill.dataobject.ItemStockDO;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.ItemModel;
import com.seckill.model.PromoModel;
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

    @Transactional
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int affectedCount = itemStockDOMapper.decreaseStock(itemId, amount);
        if (affectedCount > 0) {
            return true;
        } else {
            return false;
        }
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

}
