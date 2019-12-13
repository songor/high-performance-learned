package com.seckill.service.impl;

import com.seckill.dao.UserDOMapper;
import com.seckill.dao.UserPasswordDOMapper;
import com.seckill.dataobject.UserDO;
import com.seckill.dataobject.UserPasswordDO;
import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.model.UserModel;
import com.seckill.service.UserService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;

@Service
public class UserServiceImpl implements UserService {

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
        return convert(userDO, userPasswordDO);
    }

    private UserModel convert(UserDO userDO, UserPasswordDO userPasswordDO) {
        UserModel userModel = new UserModel();
        try {
            BeanUtils.copyProperties(userModel, userDO);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        userModel.setEncryptPassword(userPasswordDO.getEncryptPassword());
        return userModel;
    }

}
