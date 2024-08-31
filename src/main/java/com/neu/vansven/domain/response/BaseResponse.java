package com.neu.vansven.domain.response;

import lombok.Data;

@Data
public class BaseResponse<T>{
    /**
     * 状态码
     */
    private int code;
    /**
     * 要返回给前端的通用类型数据
     */
    private T data;
    /**
     * 状态码描述
     */
    private String message;
    /**
     * 其他描述
     */
    private String description;

    public BaseResponse(){}
    public BaseResponse(int code, T data, String message, String description){
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }
    public BaseResponse(int code, T data, String message){
        this.code = code;
        this.data = data;
        this.message = message;
    }



}
