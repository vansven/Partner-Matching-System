package com.neu.vansven.domain.errortype;

//枚举类定义的各种类型必须使用逗号隔开
//枚举类中定义的类型只能有get方法，无set方法，不能进行修改，所以这里不要使用@Data注解自动生成get/set方法

public enum ErrorType {
    //客户端异常类型 : 状态码 和 状态码描述一一对应
    SUCCESS_REQUEST(0,"正常响应"),
    PARAM_ERROR(40401,"参数异常"),
    AUTH_ERROR(40402,"权限异常"),
    LOGIIN_ERROR(40403,"登录异常"),
    REQUEST_ERROR(40404,"请求异常"),
    // 服务器异常类型 : 状态码 和 状态码描述一一对应
    REGISTER_ERROR(50001,"注册异常"),
    SEARCH_ERROR(50002,"查询异常"),
    DELETE_ERROR(50003,"删除异常"),
    INSERT_ERROR(50004,"插入异常"),
    UPDATE_ERROR(50005,"更新异常");

    /**
     * 状态码
     */
    private int code;
    /**
     * 状态码描述
     */
    private String message;
    /**
     * 详细描述
     */

    ErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
