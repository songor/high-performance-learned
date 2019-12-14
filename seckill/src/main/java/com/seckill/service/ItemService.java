package com.seckill.service;

import com.seckill.model.ItemModel;

public interface ItemService {

    ItemModel createItem(ItemModel itemModel);

    ItemModel getItemById(Integer id);

}
