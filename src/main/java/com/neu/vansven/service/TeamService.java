package com.neu.vansven.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.vansven.domain.Team;
import com.neu.vansven.domain.VO.TeamVO;
import com.neu.vansven.domain.request.TeamJoinRequest;
import com.neu.vansven.domain.request.TeamQuaryRequset;
import com.neu.vansven.domain.request.TeamUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author vansven
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-04-30 17:00:54
*/
public interface TeamService extends IService<Team> {

    /**
     * 用户创建队伍
     * @param team
     * @param request
     * @return
     */
    boolean createTeam(Team team, HttpServletRequest request);


    List<TeamVO> searchByTeamQuary(TeamQuaryRequset teamQuaryRequest, HttpServletRequest request);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);
}
