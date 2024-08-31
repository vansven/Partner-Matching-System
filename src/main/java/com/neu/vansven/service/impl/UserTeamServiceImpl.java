package com.neu.vansven.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.vansven.domain.UserTeam;
import com.neu.vansven.service.UserTeamService;
import com.neu.vansven.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author vansven
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2024-04-30 17:22:34
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




