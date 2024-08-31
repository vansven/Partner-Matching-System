package com.neu.vansven.mapper;

import com.neu.vansven.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author vansven
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-04-15 08:43:37
* @Entity com.neu.vansven.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}



