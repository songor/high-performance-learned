package com.seckill.controller;

import com.seckill.error.BusinessErrorEnum;
import com.seckill.error.BusinessException;
import com.seckill.response.CommonReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object handleException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        Map<String, Object> data = new HashMap<>();
        if (e instanceof BusinessException) {
            LOGGER.error("Business exception: ", e);
            BusinessException exception = (BusinessException) e;
            data.put("errorCode", exception.getErrorCode());
            data.put("errorMessage", exception.getErrorMessage());
        } else if (e instanceof ServletRequestBindingException) {
            LOGGER.error(e.getMessage(), e);
            data.put("errorCode", BusinessErrorEnum.UNKNOWN_ERROR);
            data.put("errorMessage", "Servlet Request Binding Error");
        } else if (e instanceof NoHandlerFoundException) {
            LOGGER.error(e.getMessage(), e);
            data.put("errorCode", BusinessErrorEnum.UNKNOWN_ERROR);
            data.put("errorMessage", "No Handler Found");
        } else {
            LOGGER.error("Unknown exception: ", e);
            data.put("errorCode", BusinessErrorEnum.UNKNOWN_ERROR.getErrorCode());
            data.put("errorMessage", BusinessErrorEnum.UNKNOWN_ERROR.getErrorMessage());
        }
        return CommonReturnType.create("failure", data);
    }

}
