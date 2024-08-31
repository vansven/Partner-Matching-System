package com.neu.vansven.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 队伍表
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    /**
     * 主键，作为队伍表的唯一标识
     */
    @TableId(type = IdType.AUTO)
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
    private Date expireTime;

    /**
     * 队伍状态信息 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 队伍加密密码
     */
    private String password;

    /**
     * 创建时间
     */
    private Date creatTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     *  逻辑删除 0 - 未删除 1 - 删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}