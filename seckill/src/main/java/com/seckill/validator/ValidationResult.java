package com.seckill.validator;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ValidationResult {

    private boolean hasErrors = false;

    private Map<String, String> errorMessageMap = new HashMap<>();

    public String getErrorMessage() {
        return StringUtils.join(errorMessageMap.values().toArray(), " | ");
    }

}
