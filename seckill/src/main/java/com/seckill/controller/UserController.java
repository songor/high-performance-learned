package com.seckill.controller;

import com.seckill.model.UserModel;
import com.seckill.response.CommonReturnType;
import com.seckill.service.UserService;
import com.seckill.viewobject.UserVO;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @GetMapping("/get")
    public CommonReturnType getUser(@RequestParam("id") Integer id) {
        UserModel userModel = userService.getUserById(id);
        UserVO userVO = convert(userModel);
        return CommonReturnType.create(userVO);
    }

    private UserVO convert(UserModel userModel) {
        UserVO userVO = new UserVO();
        try {
            BeanUtils.copyProperties(userVO, userModel);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return userVO;
    }

}
