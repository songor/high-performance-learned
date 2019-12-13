package com.seckill.controller;

import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.UserModel;
import com.seckill.response.CommonReturnType;
import com.seckill.service.UserService;
import com.seckill.viewobject.UserVO;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/otp")
    public CommonReturnType getOtp(@RequestParam("telephone") String telephone) {
        String otpCode = String.valueOf(new Random().nextInt(99999));
        LOGGER.info("telephone: " + telephone + ", otpCode: " + otpCode);
        // mock send otp
        request.getSession().setAttribute(telephone, otpCode);
        return CommonReturnType.create("The otp code already sent");
    }

    @PostMapping("/register")
    public CommonReturnType register(@RequestParam(name = "telephone") String telephone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "password") String password,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") Integer age) {
        String exceptedOptCode = String.valueOf(request.getSession().getAttribute(telephone));
        if (!StringUtils.equals(exceptedOptCode, otpCode)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "验证码错误");
        }

        UserModel userModel = new UserModel();
        userModel.setTelephone(telephone);
        userModel.setName(name);
        userModel.setGender(Byte.valueOf(String.valueOf(gender)));
        userModel.setAge(age);
        userModel.setEncryptPassword(DigestUtils.md5Hex(password));
        userModel.setRegisterMode("telephone");
        userService.register(userModel);

        return CommonReturnType.create("User is registered");
    }

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
