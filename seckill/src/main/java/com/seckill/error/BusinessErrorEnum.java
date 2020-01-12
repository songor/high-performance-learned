package com.seckill.error;

public enum BusinessErrorEnum implements CommonError {

    UNKNOWN_ERROR(10001, "未知错误"),
    PARAMETER_VALIDATION_ERROR(10002, "参数不合法"),
    USER_NON_EXIST(20001, "用户不存在"),
    USER_LOGIN_FAIL(20002, "手机号或密码错误"),
    USER_NOT_LOGIN(20003, "用户未登陆"),
    ITEM_NON_EXIST(30001, "商品不存在"),
    STOCK_NON_ENOUGH(30002, "库存不足"),
    ASYNC_STOCK_FAIL(40001, "异步同步库存失败"),
    CREATE_ORDER_FAIL(40002, "下单失败");

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
