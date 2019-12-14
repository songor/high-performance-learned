package com.seckill.error;

public class BusinessException extends RuntimeException implements CommonError {

    private CommonError commonError;

    public BusinessException(CommonError commonError) {
        super(commonError.getErrorMessage());
        this.commonError = commonError;
    }

    public BusinessException(CommonError commonError, String errorMessage) {
        super(errorMessage);
        this.commonError = commonError;
        this.commonError.setErrorMessage(errorMessage);
    }

    @Override
    public int getErrorCode() {
        return this.commonError.getErrorCode();
    }

    @Override
    public String getErrorMessage() {
        return this.commonError.getErrorMessage();
    }

    @Override
    public CommonError setErrorMessage(String errorMessage) {
        this.commonError.setErrorMessage(errorMessage);
        return this;
    }

}
