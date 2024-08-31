package com.neu.vansven.utils;

import com.neu.vansven.constant.Constant;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@Component
public class Encrypt {

    public String add(String password){
        String salt = UUID.nameUUIDFromBytes(Constant.SALT.getBytes()).toString().replaceAll("-", "");
        return  DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

}
