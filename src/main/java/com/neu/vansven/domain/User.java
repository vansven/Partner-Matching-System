package com.neu.vansven.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable { // 需要实现Serializable类否则返回数据时会报序列化错误

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 0 - 男 1- 女
     */
    private Integer gender;

    /**
     * 密码
     */
    private String password;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除 0-存在 1- 不存在
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;
    /**
     * 标签列表
     */
    private String tags;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}