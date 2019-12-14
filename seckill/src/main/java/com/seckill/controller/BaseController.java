package com.seckill.controller;

import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.response.CommonReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

public class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Object handleException(Exception e) {
        Map<String, Object> data = new HashMap<>();
        if (e instanceof BusinessException) {
            LOGGER.error("Business exception: ", e);
            BusinessException exception = (BusinessException) e;
            data.put("errorCode", exception.getErrorCode());
            data.put("errorMessage", exception.getErrorMessage());
        } else {
            LOGGER.error("Unknown exception: ", e);
            data.put("errorCode", BusinessErrorEnum.UNKNOWN_ERROR.getErrorCode());
            data.put("errorMessage", BusinessErrorEnum.UNKNOWN_ERROR.getErrorMessage());
        }
        return CommonReturnType.create("failure", data);
    }

}
