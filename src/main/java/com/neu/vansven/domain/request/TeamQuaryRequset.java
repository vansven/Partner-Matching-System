package com.neu.vansven.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@Data
public class TeamQuaryRequset {
    /**
     * 主键，作为队伍表的唯一标识
     */
    private Long teamId;

    /**
     * 队伍名称可重复
     */
    private String name;

    /**
     * 通过关键词查询
     */
    private String keyWord;

    /**
     * 分页查询时指定当前页数
     */
    private Long currentPage;

    /**
     * 分页查询是指定页的数据大小
     */
    private Long pageSize;

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
     * 队伍加密密码
     */
    private String password;

}