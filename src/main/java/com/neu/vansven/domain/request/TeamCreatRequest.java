package com.neu.vansven.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamCreatRequest {

    private String name;

    private String description;

    private Integer status;

    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",locale = "zh",timezone = "GMT+8")
    private Date expireTime;


}
