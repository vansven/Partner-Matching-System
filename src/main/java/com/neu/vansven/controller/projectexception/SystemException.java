package com.neu.vansven.controller.projectexception;

import com.neu.vansven.domain.errortype.ErrorType;

// 异常中的属性也是定义好的无法进行修改，所以不必使用 @Data 注解，手动提供get方法即可
public class SystemException extends RuntimeException{
    private int code; //这里定义状态码是为了能返回给前端时，将状态码提取出来
    private String description;

    public SystemException(ErrorType errorType, String description) {
        super(errorType.getMessage());
        this.code = errorType.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
