package com.neu.vansven.controller;

import com.neu.vansven.constant.Constant;
import com.neu.vansven.controller.projectexception.BusinessException;
import com.neu.vansven.domain.Team;
import com.neu.vansven.domain.VO.TeamVO;
import com.neu.vansven.domain.errortype.ErrorType;
import com.neu.vansven.domain.request.TeamCreatRequest;
import com.neu.vansven.domain.request.TeamJoinRequest;
import com.neu.vansven.domain.request.TeamQuaryRequset;
import com.neu.vansven.domain.request.TeamUpdateRequest;
import com.neu.vansven.domain.response.BaseResponse;
import com.neu.vansven.service.impl.TeamServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController("/team") // @Controller注解 是类中所有标注 RequestMapping 注解的 HTTP服务起始端点
public class TeamController {

    @Autowired
    private TeamServiceImpl teamService;

    @PostMapping("/creat")
    public BaseResponse<Boolean> creatTeam(@RequestBody TeamCreatRequest teamRequest, HttpServletRequest request) {
        if(teamRequest == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"参数非空");
        }
        if(request.getSession().getAttribute(Constant.LOGIN_STATUS) == null){
            throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamRequest, team);
        boolean flag = teamService.createTeam(team, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                flag,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "创建队伍成功"
        );
    }

    @PostMapping("/searchByTeam")
    public BaseResponse<List<TeamVO>> searchByTeamQuary(@RequestBody TeamQuaryRequset teamQuaryRequset, HttpServletRequest request){
        if(teamQuaryRequset == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"参数非空");
        }
        List<TeamVO> teamVOSList = teamService.searchByTeamQuary(teamQuaryRequset, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                teamVOSList,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "分页展示队伍成功"
        );
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"参数非空");
        }
        if(request.getSession().getAttribute(Constant.LOGIN_STATUS) == null){
            throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录");
        }
        Boolean flag = teamService.joinTeam(teamJoinRequest, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                flag,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "加入队伍成功"
        );
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"参数非空");
        }
        if(request.getSession().getAttribute(Constant.LOGIN_STATUS) == null){
            throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录");
        }
        Boolean flag = teamService.updateTeam(teamUpdateRequest, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                flag,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "更新数据成功"
        );
    }

}
