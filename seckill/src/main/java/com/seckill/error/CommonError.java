package com.seckill.error;

public interface CommonError {

    int getErrorCode();

    String getErrorMessage();

    CommonError setErrorMessage(String errorMessage);

}
