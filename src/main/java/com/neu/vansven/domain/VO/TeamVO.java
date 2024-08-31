package com.neu.vansven.domain.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neu.vansven.domain.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamVO {
    /**
     * 主键，作为队伍表的唯一标识
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 创建队伍用户id
     */
    private Long createId;

    /**
     * 队伍信息描述
     */
    private String description;


    /**
     * 指定加入的超时时间，如果超过该时间就不允许加入
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",locale = "zh",timezone = "GMT+8")
    private Date expireTime;

    /**
     * 队伍状态信息 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 加入队伍的用户信息
     */
    List<User> userList;

    /**
     * 创建队伍的信息
     */
    User user;

}
