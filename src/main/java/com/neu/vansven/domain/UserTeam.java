package com.neu.vansven.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户队伍关系表
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam implements Serializable {
    /**
     * 主键 作为关系表的唯一索引
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍表中的唯一标识
     */
    private Long teamId;

    /**
     * 用户表中的唯一标识
     */
    private Long userId;

    /**
     * 加入队伍时间
     */
    private Date joinTime;

    /**
     * 创建时间
     */
    private Date creatTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除 0 - 未删除 1 - 删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}