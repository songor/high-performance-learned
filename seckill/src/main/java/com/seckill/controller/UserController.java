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
import java.util.Random;

/**
 * 跨域感知 Session 需要解决两个问题，第一个是解决跨域问题，第二个是解决跨域 Cookie 传输问题
 * （1）跨域问题
 * Spring Boot @CrossOrigin(origins = {"*"}, allowedHeaders = "*")
 * （2）跨域 Cookie 传输问题
 * 前端 xhrFields: {withCredentials: true}
 * Spring Boot @CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
 * 当设置了 allowCredentials = "true" 的时候 origins = {"*"} 就失效了
 * 在 Spring Boot 返回的 allow origin 取 request 内的 origin，这样就可以做到在哪个 origin 上使用跨域就允许哪个 origin
 */
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/get")
    public CommonReturnType getUser(@RequestParam("id") Integer id) {
        UserModel userModel = userService.getUserById(id);
        if (userModel == null) {
            throw new BusinessException(BusinessErrorEnum.USER_NON_EXIST);
        }
        UserVO userVO = new UserVO();
        try {
            BeanUtils.copyProperties(userVO, userModel);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        return CommonReturnType.create(userVO);
    }

    @PostMapping("/otp")
    public CommonReturnType getOtp(@RequestParam("telephone") String telephone) {
        if (StringUtils.isEmpty(telephone)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        String otpCode = String.valueOf(new Random().nextInt(99999));
        LOGGER.info("telephone: " + telephone + ", otpCode: " + otpCode);
        // mock send otp
        request.getSession().setAttribute(telephone, otpCode);
        return CommonReturnType.create("The otp code already sent");
    }

    @PostMapping("/register")
    public CommonReturnType register(@RequestParam("telephone") String telephone,
                                     @RequestParam("otpCode") String otpCode,
                                     @RequestParam("name") String name,
                                     @RequestParam("password") String password,
                                     @RequestParam("gender") Integer gender,
                                     @RequestParam("age") Integer age) {
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

    @PostMapping("/login")
    public CommonReturnType login(@RequestParam("telephone") String telephone,
                                  @RequestParam("password") String password) {
        if (StringUtils.isEmpty(telephone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        UserModel userModel = userService.validateLogin(telephone, DigestUtils.md5Hex(password));
        request.getSession().setAttribute("IS_LOGIN", true);
        request.getSession().setAttribute("LOGIN_USER", userModel);
        return CommonReturnType.create("User login success");
    }

}
