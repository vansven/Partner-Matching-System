package com.neu.vansven.domain.request;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String account;
    private String password;
    private String checkPassword;
}
