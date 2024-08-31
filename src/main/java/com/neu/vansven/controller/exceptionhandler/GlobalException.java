package com.neu.vansven.controller.exceptionhandler;

import com.neu.vansven.controller.projectexception.BusinessException;
import com.neu.vansven.controller.projectexception.SystemException;
import com.neu.vansven.domain.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse doBusinessHandler(BusinessException e){
        log.error("businessException：" + e.getMessage());
        int code = e.getCode();
        String message = e.getMessage(); // done 已测试一下获取到的是自己写的message
        String description = e.getDescription();
        return new BaseResponse<>(code, null, message,description);
    }

    @ExceptionHandler(SystemException.class)
    public BaseResponse doSystemBusiness(SystemException e){
        log.error("systemEXception：" + e.getMessage());
        int code = e.getCode();
        String message = e.getMessage();
        String description = e.getDescription();
        return new BaseResponse<>(code, null, message,description);
    }

}
