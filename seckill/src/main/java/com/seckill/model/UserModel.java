package com.seckill.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class UserModel {

    private Integer id;

    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotNull(message = "性别不能为空")
    private Byte gender;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不合法")
    @Max(value = 150, message = "年龄不合法")
    private Integer age;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1(3|4|5|7|8)\\d{9}$", message = "手机号不合法")
    private String telephone;

    private String registerMode;

    private String thirdPartyId;

    @NotBlank(message = "密码不能为空")
    private String encryptPassword;

}
