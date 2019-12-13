package com.seckill.error;

public enum BusinessErrorEnum implements CommonError {

    UNKNOWN_ERROR(10001, "未知错误"),
    USER_NON_EXIST(20001, "用户不存在");

    private int errorCode;

    private String errorMessage;

    BusinessErrorEnum(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public CommonError setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
