package com.neu.vansven.domain.statustype;

public enum TeamStatusEnum {

    PUBLIC(0),
    PERSONAL(1),
    ENCRYPT(2);

    public Integer getStatus() {
        return status;
    }

    private Integer status;
    TeamStatusEnum(Integer status){
        this.status = status;
    }
}
