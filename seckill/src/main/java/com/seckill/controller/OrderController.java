package com.seckill.controller;

import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.UserModel;
import com.seckill.response.CommonReturnType;
import com.seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/order")
public class OrderController extends BaseController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount) {
        Boolean isLogin = (Boolean) request.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(BusinessErrorEnum.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel) request.getSession().getAttribute("LOGIN_USER");

        orderService.createOrder(userModel.getId(), itemId, amount);
        return CommonReturnType.create("Order created");
    }

}
