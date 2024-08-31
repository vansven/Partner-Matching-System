package com.neu.vansven.domain.request;

import lombok.Data;

import java.util.Date;

@Data
public class TeamJoinRequest {
    /**
     * 主键，作为队伍表的唯一标识
     */
    private Long teamId;

    /**
     * 队伍加密密码
     */
    private String password;
}
