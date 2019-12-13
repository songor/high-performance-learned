package com.seckill.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserModel {

    private Integer id;

    private String name;

    private Byte gender;

    private Integer age;

    private String telephone;

    private String registerMode;

    private String thirdPartyId;

    private String encryptPassword;

}
