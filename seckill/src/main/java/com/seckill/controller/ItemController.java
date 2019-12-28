package com.seckill.controller;

import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.ItemModel;
import com.seckill.response.CommonReturnType;
import com.seckill.service.CacheService;
import com.seckill.service.ItemService;
import com.seckill.viewobject.ItemVO;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/item")
public class ItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

    private static final String ITEM_PREFIX = "item_";

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @PostMapping("/create")
    public CommonReturnType createItem(@RequestParam("title") String title,
                                       @RequestParam("price") BigDecimal price,
                                       @RequestParam("stock") Integer stock,
                                       @RequestParam("description") String description,
                                       @RequestParam("imageUrl") String imageUrl) {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImageUrl(imageUrl);
        ItemModel createdItem = itemService.createItem(itemModel);

        ItemVO itemVO = new ItemVO();
        try {
            BeanUtils.copyProperties(itemVO, createdItem);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }

        return CommonReturnType.create(itemVO);
    }

    @GetMapping("/get")
    public CommonReturnType getItem(@RequestParam("id") Integer id) {
        String itemKey = ITEM_PREFIX + id;
        ItemModel itemModel = (ItemModel) cacheService.get(itemKey);
        if (itemModel == null) {
            itemModel = (ItemModel) redisTemplate.opsForValue().get(itemKey);
            if (itemModel == null) {
                itemModel = itemService.getItemById(id);
                if (itemModel == null) {
                    throw new BusinessException(BusinessErrorEnum.ITEM_NON_EXIST);
                }
                redisTemplate.opsForValue().set(itemKey, itemModel);
                redisTemplate.expire(itemKey, 10, TimeUnit.MINUTES);
            }
            cacheService.put(itemKey, itemModel);
        }

        ItemVO itemVO = new ItemVO();
        try {
            BeanUtils.copyProperties(itemVO, itemModel);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        if (itemModel.getPromoModel() != null) {
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getItemPrice());
            itemVO.setPromoStartDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .format(LocalDateTime.ofInstant(itemModel.getPromoModel().getStartDate().toInstant(), ZoneId.systemDefault())));
            itemVO.setPromoEndDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .format(LocalDateTime.ofInstant(itemModel.getPromoModel().getEndDate().toInstant(), ZoneId.systemDefault())));
        }

        return CommonReturnType.create(itemVO);
    }

    @GetMapping("/list")
    public CommonReturnType listItem() {
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = new ItemVO();
            try {
                BeanUtils.copyProperties(itemVO, itemModel);
            } catch (Exception e) {
                LOGGER.error("Copy properties failure", e);
            }
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

}
