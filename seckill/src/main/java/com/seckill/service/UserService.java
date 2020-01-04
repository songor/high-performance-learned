package com.seckill.service;

import com.seckill.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);

    void register(UserModel userModel);

    UserModel validateLogin(String telephone, String encryptPassword);

    UserModel getUserByIdInCache(Integer id);

}
