package com.seckill.service.impl;

import com.seckill.dao.OrderDOMapper;
import com.seckill.dao.SequenceDOMapper;
import com.seckill.dao.StockLogDOMapper;
import com.seckill.dataobject.OrderDO;
import com.seckill.dataobject.SequenceDO;
import com.seckill.dataobject.StockLogDO;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.ItemModel;
import com.seckill.model.OrderModel;
import com.seckill.service.ItemService;
import com.seckill.service.OrderService;
import com.seckill.service.PromoService;
import com.seckill.service.UserService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

//import com.seckill.model.UserModel;
//import org.springframework.transaction.support.TransactionSynchronizationAdapter;
//import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Transactional
    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount, Integer promoId, String stockLogId) {
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "购买数量不合法");
        }
//        UserModel userModel = userService.getUserById(userId);
        /**
         * 前置到 {@link PromoService#generateSecKillToken}
         */
//        UserModel userModel = userService.getUserByIdInCache(userId);
//        if (userModel == null) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户不存在");
//        }
//        ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        /**
         * 前置到 {@link PromoService#generateSecKillToken}
         */
//        if (itemModel == null) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "商品不存在");
//        }
//        if (promoId != null) {
//            if (itemModel.getPromoModel() == null || promoId != itemModel.getPromoModel().getId()) {
//                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "秒杀不存在");
//            }
//            if (itemModel.getPromoModel().getStatus() != 2) {
//                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "秒杀未开始");
//            }
//        }

        if (!itemService.decreaseStock(itemId, amount)) {
            throw new BusinessException(BusinessErrorEnum.STOCK_NON_ENOUGH);
        }

        OrderModel orderModel = new OrderModel();
        orderModel.setId(generateOrderId());
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getItemPrice());
            orderModel.setPromoId(promoId);
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setAmount(amount);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(BigDecimal.valueOf(amount)));

        OrderDO orderDO = new OrderDO();
        try {
            BeanUtils.copyProperties(orderDO, orderModel);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());

        orderDOMapper.insertSelective(orderDO);

        itemService.increaseSales(itemId, amount);

        /**
         * （2）在方法最后发送异步更新库存 -> Spring Transaction 在 return 后才提交，如果 commit 失败，仍然会超扣库存
         */
//        boolean result = itemService.asyncDecreaseStock(itemId, amount);
//        if (!result) {
//            itemService.increaseStock(itemId, amount);
//            throw new BusinessException(BusinessErrorEnum.ASYNC_STOCK_FAIL);
//        }

        /**
         * （3）异步更新库存可能会失败，造成不一致，因此引入 Rocket MQ Transaction 事务
         */
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                boolean result = itemService.asyncDecreaseStock(itemId, amount);
//                if (!result) {
//                    itemService.increaseStock(itemId, amount);
//                    throw new BusinessException(BusinessErrorEnum.ASYNC_STOCK_FAIL);
//                }
//            }
//        });

        // 设置库存流水状态
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderId() {
        StringBuilder orderId = new StringBuilder();

        LocalDate now = LocalDate.now();
        String date = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        orderId.append(date);

        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        Integer currentValue = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(currentValue + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

        String sequence = String.valueOf(currentValue);
        for (int i = 0; i < 6 - sequence.length(); i++) {
            orderId.append(0);
        }
        orderId.append(sequence);

        orderId.append("00");

        return orderId.toString();
    }

}
