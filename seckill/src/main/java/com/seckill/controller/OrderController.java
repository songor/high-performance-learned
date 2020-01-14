package com.seckill.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.UserModel;
import com.seckill.response.CommonReturnType;
import com.seckill.rocketmq.StockTransactionProducer;
import com.seckill.service.ItemService;
import com.seckill.service.OrderService;
import com.seckill.service.PromoService;
import com.seckill.util.CodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

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

    @Autowired
    private PromoService promoService;

    private ExecutorService executorService;

    private RateLimiter createOrderRateLimiter;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);
        createOrderRateLimiter = RateLimiter.create(100);
    }

    @PostMapping("/create")
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam(value = "promoId", required = false) Integer promoId,
                                        @RequestParam("token") String token,
                                        @RequestParam(value = "promoToken", required = false) String promoToken) {
//        Boolean isLogin = (Boolean) request.getSession().getAttribute("IS_LOGIN");
//        if (isLogin == null || !isLogin.booleanValue()) {
//            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
//        }
//        UserModel userModel = (UserModel) request.getSession().getAttribute("LOGIN_USER");

        if (!createOrderRateLimiter.tryAcquire()) {
            throw new BusinessException(BusinessErrorEnum.RATE_LIMIT);
        }

        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        if (promoId != null) {
            String promoTokenInRedis = (String) redisTemplate.opsForValue().get("promo_token_" + userModel.getId() + "_" + itemId + "_" + promoId);
            if (promoTokenInRedis == null) {
                throw new BusinessException(BusinessErrorEnum.PROMO_TOKEN_VALIDATION_ERROR);
            }
            if (!StringUtils.equals(promoToken, promoTokenInRedis)) {
                throw new BusinessException(BusinessErrorEnum.PROMO_TOKEN_VALIDATION_ERROR);
            }
        }

        /**
         * 前置到 {@link PromoService#generateSecKillToken}
         */
//        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
//            throw new BusinessException(BusinessErrorEnum.STOCK_NON_ENOUGH);
//        }

        // 队列泄洪
        Future<Boolean> future = executorService.submit(() -> {
            // 库存流水
            String stockLogId = itemService.initStockLog(itemId, amount);
            return producer.transactionAsyncDecreaseStock(userModel.getId(), itemId, amount, promoId, stockLogId);
        });
        try {
            boolean result = future.get();
            if (!result) {
                throw new BusinessException(BusinessErrorEnum.CREATE_ORDER_FAIL, "未知错误");
            }
        } catch (InterruptedException e) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR);
        }

        return CommonReturnType.create("Order created");
    }

    @PostMapping("/token/generate")
    public CommonReturnType generateToken(@RequestParam("itemId") Integer itemId,
                                          @RequestParam(value = "promoId") Integer promoId,
                                          @RequestParam("token") String token,
                                          @RequestParam("verifyCode") String verifyCode) {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        // 验证码
        String code = (String) redisTemplate.opsForValue().get("verify_code_" + userModel.getId());
        if (StringUtils.isEmpty(code)) {
            throw new BusinessException(BusinessErrorEnum.VERIFY_CODE_VALIDATION_ERROR);
        }
        if (!StringUtils.equalsIgnoreCase(code, verifyCode)) {
            throw new BusinessException(BusinessErrorEnum.VERIFY_CODE_VALIDATION_ERROR);
        }

        String promoToken = promoService.generateSecKillToken(userModel.getId(), itemId, promoId);
        if (promoToken == null) {
            throw new BusinessException(BusinessErrorEnum.GENERATE_PROMO_TOKEN_FAIL);
        }

        return CommonReturnType.create(promoToken);
    }

    @GetMapping("/code/generate")
    public void generateVerifyCode(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }

        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        String key = "verify_code_" + userModel.getId();
        redisTemplate.opsForValue().set(key, map.get("code"));
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
    }

}
