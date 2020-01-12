package com.seckill.service;

import com.seckill.model.ItemModel;

import java.util.List;

public interface ItemService {

    ItemModel createItem(ItemModel itemModel);

    ItemModel getItemById(Integer id);

    List<ItemModel> listItem();

    boolean decreaseStock(Integer itemId, Integer amount);

    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    boolean increaseStock(Integer itemId, Integer amount);

    void increaseSales(Integer itemId, Integer amount);

    ItemModel getItemByIdInCache(Integer id);

    String initStockLog(Integer itemId, Integer amount);

}
