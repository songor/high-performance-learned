package com.seckill.error;

public enum BusinessErrorEnum implements CommonError {

    UNKNOWN_ERROR(10001, "未知错误"),
    PARAMETER_VALIDATION_ERROR(10002, "参数不合法"),
    USER_NON_EXIST(20001, "该用户不存在"),
    USER_LOGIN_FAIL(20002, "手机号或密码错误");

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
