package com.seckill.controller;

import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.UserModel;
import com.seckill.response.CommonReturnType;
import com.seckill.rocketmq.StockTransactionProducer;
import com.seckill.service.ItemService;
import com.seckill.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.HttpServletRequest;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/order")
public class OrderController {

//    @Autowired
//    private HttpServletRequest request;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StockTransactionProducer producer;

    @Autowired
    private ItemService itemService;

    @PostMapping("/create")
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam(value = "promoId", required = false) Integer promoId,
                                        @RequestParam("token") String token) {
//        Boolean isLogin = (Boolean) request.getSession().getAttribute("IS_LOGIN");
//        if (isLogin == null || !isLogin.booleanValue()) {
//            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
//        }
//        UserModel userModel = (UserModel) request.getSession().getAttribute("LOGIN_USER");

        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            throw new BusinessException(BusinessErrorEnum.STOCK_NON_ENOUGH);
        }

        // 库存流水
        String stockLogId = itemService.initStockLog(itemId, amount);

//        orderService.createOrder(userModel.getId(), itemId, amount, promoId);
        boolean result = producer.transactionAsyncDecreaseStock(userModel.getId(), itemId, amount, promoId, stockLogId);
        if (!result) {
            throw new BusinessException(BusinessErrorEnum.CREATE_ORDER_FAIL, "未知错误");
        }
        return CommonReturnType.create("Order created");
    }

}
