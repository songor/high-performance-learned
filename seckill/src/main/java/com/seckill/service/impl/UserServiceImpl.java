package com.seckill.service.impl;

import com.seckill.dao.UserDOMapper;
import com.seckill.dao.UserPasswordDOMapper;
import com.seckill.dataobject.UserDO;
import com.seckill.dataobject.UserPasswordDO;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.UserModel;
import com.seckill.service.UserService;
import com.seckill.validator.CustomValidator;
import com.seckill.validator.ValidationResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private CustomValidator validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
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

    @Transactional
    @Override
    public void register(UserModel userModel) {
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, result.getErrorMessage());
        }
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

    @Override
    public UserModel validateLogin(String telephone, String encryptPassword) {
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if (userDO != null) {
            UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
            if (StringUtils.equals(userPasswordDO.getEncryptPassword(), encryptPassword)) {
                UserModel userModel = new UserModel();
                try {
                    BeanUtils.copyProperties(userModel, userDO);
                } catch (Exception e) {
                    LOGGER.error("Copy properties failure", e);
                }
                userModel.setEncryptPassword(userPasswordDO.getEncryptPassword());
                return userModel;
            } else {
                throw new BusinessException(BusinessErrorEnum.USER_LOGIN_FAIL);
            }
        } else {
            throw new BusinessException(BusinessErrorEnum.USER_LOGIN_FAIL);
        }
    }

}
