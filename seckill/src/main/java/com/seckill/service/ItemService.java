package com.seckill.service;

import com.seckill.model.ItemModel;

import java.util.List;

public interface ItemService {

    ItemModel createItem(ItemModel itemModel);

    ItemModel getItemById(Integer id);

    List<ItemModel> listItem();

}