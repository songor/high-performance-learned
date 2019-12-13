package com.seckill.service.impl;

import com.seckill.controller.UserController;
import com.seckill.dao.UserDOMapper;
import com.seckill.dao.UserPasswordDOMapper;
import com.seckill.dataobject.UserDO;
import com.seckill.dataobject.UserPasswordDO;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.UserModel;
import com.seckill.service.UserService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            throw new BusinessException(BusinessErrorEnum.USER_NON_EXIST);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = new UserModel();
        try {
            BeanUtils.copyProperties(userModel, userDO);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        userModel.setEncryptPassword(userPasswordDO.getEncryptPassword());
        return userModel;
    }

    @Override
    public void register(UserModel userModel) {
        UserDO userDO = new UserDO();
        try {
            BeanUtils.copyProperties(userDO, userModel);
        } catch (Exception e) {
            LOGGER.error("Copy properties failure", e);
        }
        userDOMapper.insertSelective(userDO);
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncryptPassword(userModel.getEncryptPassword());
        userPasswordDO.setUserId(userDO.getId());
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

}
