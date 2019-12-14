package com.seckill.service.impl;

import com.seckill.dao.ItemDOMapper;
import com.seckill.dao.ItemStockDOMapper;
import com.seckill.dataobject.ItemDO;
import com.seckill.dataobject.ItemStockDO;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.ItemModel;
import com.seckill.service.ItemService;
import com.seckill.validator.CustomValidator;
import com.seckill.validator.ValidationResult;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private CustomValidator validator;

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
        try {
            BeanUtils.copyProperties(itemModel, itemDO);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        itemModel.setPrice(BigDecimal.valueOf(itemDO.getPrice()));

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        itemModel.setStock(itemStockDO.getStock());

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

}
