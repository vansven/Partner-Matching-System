package com.neu.vansven.domain.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String account;
    private String password;
}
