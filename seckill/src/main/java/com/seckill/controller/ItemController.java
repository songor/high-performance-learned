package com.seckill.controller;

import com.seckill.model.ItemModel;
import com.seckill.response.CommonReturnType;
import com.seckill.service.ItemService;
import com.seckill.viewobject.ItemVO;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/item")
public class ItemController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

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
        ItemModel itemModel = itemService.getItemById(id);

        ItemVO itemVO = new ItemVO();
        try {
            BeanUtils.copyProperties(itemVO, itemModel);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }

        return CommonReturnType.create(itemVO);
    }

}
