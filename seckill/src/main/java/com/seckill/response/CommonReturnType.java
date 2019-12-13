package com.seckill.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonReturnType {

    private String status;

    private Object data;

    public static CommonReturnType create(Object data) {
        return create("success", data);
    }

    public static CommonReturnType create(String status, Object data) {
        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setStatus(status);
        commonReturnType.setData(data);
        return commonReturnType;
    }

}
