package com.neu.vansven.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.vansven.constant.Constant;
import com.neu.vansven.controller.projectexception.BusinessException;
import com.neu.vansven.controller.projectexception.SystemException;
import com.neu.vansven.domain.Team;
import com.neu.vansven.domain.User;
import com.neu.vansven.domain.UserTeam;
import com.neu.vansven.domain.VO.TeamVO;
import com.neu.vansven.domain.errortype.ErrorType;
import com.neu.vansven.domain.request.TeamJoinRequest;
import com.neu.vansven.domain.request.TeamQuaryRequset;
import com.neu.vansven.domain.request.TeamUpdateRequest;
import com.neu.vansven.domain.statustype.TeamStatusEnum;
import com.neu.vansven.mapper.TeamMapper;
import com.neu.vansven.service.TeamService;
import com.neu.vansven.service.UserTeamService;
import com.neu.vansven.utils.Encrypt;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
* @author vansven
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2024-04-30 17:00:54
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private UserTeamService userTeamService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private Encrypt encrypt;

    @Override
    @Transactional
    public boolean createTeam(Team team, HttpServletRequest request){
        if(team == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"参数不能为空");
        }
        User userLogin = (User) request.getSession().getAttribute("userlogin");
        if (userLogin == null){
            throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录");
        }
        String name = team.getName();
        if(StringUtils.length(name) > 20){
            throw new BusinessException(ErrorType.PARAM_ERROR,"队伍名称不超过20个字");
        }
        String description = team.getDescription();
        if(StringUtils.length(description) > 512){
            throw new BusinessException(ErrorType.PARAM_ERROR,"队伍描述不超过512个字");
        }
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        if(status == TeamStatusEnum.ENCRYPT.getStatus()){
            String password = team.getPassword();
            if(StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorType.PARAM_ERROR,"密码非空且不超过32位");
            }
            team.setPassword(encrypt.add(password));
        }

        // 一个用户创建或加入的队伍数量 <= 5
        // get userId from request
        // select count(*) from user_team where user_id = userId
        Long userId = userLogin.getId();
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.eq("create_id", userId);
        long memberCreateTeam = teamMapper.selectCount(wrapper);
        if(memberCreateTeam >= 5){
            throw new BusinessException(ErrorType.PARAM_ERROR,"每个用户最多创建5个队伍");
        }
        //插入队伍信息 ==> 队伍表，加密后的以及id自选 加入已有或者新建
        team.setCreateId(userId);
        int saveTeam = teamMapper.insert(team);
        if(saveTeam == 0){
            throw new SystemException(ErrorType.INSERT_ERROR,"创建队伍失败");
        }
        // 插入 用户 ==》 队伍关系表中
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(team.getId());
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        boolean saveUserTeam = userTeamService.save(userTeam);
        if(!saveUserTeam){
            throw new SystemException(ErrorType.INSERT_ERROR,"创建用户队伍关系表失败");
        }
        return true;
    }

    @Override
    public List<TeamVO> searchByTeamQuary(TeamQuaryRequset teamQuaryRequest, HttpServletRequest request){ // todo 返回所有列表业务逻辑设计

        // select * from team where team_id = id and name = name and ... and ....
        // 如果请求参数存在就作为查询条件
        if(teamQuaryRequest == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"查询条件为空");
        }
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        Long teamId = teamQuaryRequest.getTeamId();
        Long l = Optional.ofNullable(teamId).orElse(0L);
        if(l != 0){
            wrapper.eq("team_id", l);
        }
        String keyWord = teamQuaryRequest.getKeyWord();
        if(StringUtils.isNotBlank(keyWord)){ // 通过某个关键词对名称或者描述进行查询
            // select * from .. and (name = '%keyWord%' or description = '%keyWord%') and ..
            wrapper.and(i -> i.like("name",keyWord).or().like("description",keyWord));
        }
        String name = teamQuaryRequest.getName();
        if(StringUtils.isNotBlank(name)){
            wrapper.eq("name",name);
        }
        String description = teamQuaryRequest.getDescription();
        if(StringUtils.isNotBlank(description)){
            wrapper.eq("description",description);
        }
        Integer status = teamQuaryRequest.getStatus();
        // 只有管理员才能查询 非公开和加密队伍信息
        if(userServiceImpl.isAdmin(request)){
            if(status != TeamStatusEnum.PUBLIC.getStatus()){
                wrapper.eq("status",status);
            }
        }
        if(status == TeamStatusEnum.PUBLIC.getStatus()){
            wrapper.eq("status",status);
        }
        Date expireTime = teamQuaryRequest.getExpireTime();
        if(expireTime != null){
            wrapper.eq("expire_time", expireTime);
        }
        // 分页展示数据
        Long currentPage = teamQuaryRequest.getCurrentPage();
        Long pageSize = teamQuaryRequest.getPageSize();

        if(pageSize != null && currentPage != null){
            if (currentPage < 0 ){
                throw new BusinessException(ErrorType.PARAM_ERROR,"请输入有效的查询页数");
            }
            if(pageSize < 0){
                throw new BusinessException(ErrorType.PARAM_ERROR,"请输入有效查询的数据条数");
            }
        }
        Page<Team> teamPage = new Page<>(currentPage, pageSize);
        List<Team> teamList = teamMapper.selectPage(teamPage, wrapper).getRecords(); // 搜索出team表中符合条件的所有队伍
        if(teamList == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"查询队伍不存在");
        }
        QueryWrapper<UserTeam> wrapperRelate = new QueryWrapper<UserTeam>();
        ArrayList<TeamVO> teamVOList = new ArrayList<>();
        for(Team team:teamList){
            if(new Date().after(team.getExpireTime())){ //信息中不展示已过期的队伍
                continue;
            }
            // 关联查询创建人信息
            // get createId from team
            // select * from user where id = createId  创建人信息
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team,teamVO);
            Long userId = team.getCreateId();
            User userOfCreatTeam = userServiceImpl.getById(userId); // 用户数据要脱敏
            User safetyUserOfCreatTeam = userServiceImpl.getSafetyUser(userOfCreatTeam);
            teamVO.setUser(safetyUserOfCreatTeam); // 创建队伍的用户信息

            // 关联查询加入队伍的所有用户信息  先查出队伍有哪些用户id 再查用户表
            // get team 的 createId  teamId
            // select * from user_team where teamId =teamId  加入该队伍有哪些用户id
            // select * from user where id = userId
            // team1 id1
            // team2 id1
            // team3 id1
            // team4 id2
            // team1 id2
            // team1 id3
            wrapperRelate.eq("team_id",team.getId());
            List<UserTeam> userTeamList = userTeamService.list(wrapperRelate); // 根据当前teamId搜索user_team表中所有是teamid队伍
            ArrayList<User> usersOfJoin = new ArrayList<>(); // 创建一个列表存储队伍中的所有用户信息
            for(UserTeam userTeam:userTeamList){
                if(userTeam.getUserId() != userId){ // 关联除创建人之外的其他用户信息
                    User userOfTeam = userServiceImpl.getById(userTeam.getUserId());
                    User safetyUserOfTeam = userServiceImpl.getSafetyUser(userOfTeam);
                    usersOfJoin.add(safetyUserOfTeam);
                }
            }
            teamVO.setUserList(usersOfJoin); // 加入队伍的所有用户信息
            teamVOList.add(teamVO);
        }
        return teamVOList;
    }

    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request){ // done 幂等性 --> 分布式锁解决 在并发场景下重复插入
        RLock lock = redissonClient.getLock("partner:team:join");
        System.out.println("正在执行加入队伍的线程为：" + Thread.currentThread().getName());
        try {
            lock.lock();
            User userLogin = (User) request.getSession().getAttribute(Constant.LOGIN_STATUS);
            if(userLogin == null){
                throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录再操作");
            }
            if(teamJoinRequest == null){
                throw new BusinessException(ErrorType.PARAM_ERROR,"参数不能为空");
            }
            Long teamId = teamJoinRequest.getTeamId();
            if(teamId <= 0){
                throw new BusinessException(ErrorType.PARAM_ERROR,"请输入有效队伍ID");
            }
            // select count from team where team_id = teamId
            Team team = teamMapper.selectById(teamId);
            if(team == null){
                throw new BusinessException(ErrorType.PARAM_ERROR, "加入的队伍不存在");
            }
            if(new Date().after(team.getExpireTime())){
                throw new BusinessException(ErrorType.PARAM_ERROR,"当前加入队伍时间已过期");
            }
            if(team.getStatus() == TeamStatusEnum.PERSONAL.getStatus()){
                throw new BusinessException(ErrorType.PARAM_ERROR,"禁止加入私有队伍");
            }
            if(team.getStatus() == TeamStatusEnum.ENCRYPT.getStatus()){
                String password = teamJoinRequest.getPassword();
                if(StringUtils.isBlank(password)){
                    throw new BusinessException(ErrorType.PARAM_ERROR,"队伍为加密队伍请输入密码");
                }
                if(!StringUtils.equals(encrypt.add(password),team.getPassword())){
                    throw new BusinessException(ErrorType.PARAM_ERROR,"密码匹配错误，请重新输入密码");
                }
            }
            // 队伍中加入的人数不能超过5个(这里包括了创建和加入的)
            QueryWrapper<UserTeam> wrapper;
            wrapper= new QueryWrapper<>();
            wrapper.eq("team_id",teamId);
            long count = userTeamService.count(wrapper);
            if(count >= 5){
                throw new BusinessException(ErrorType.PARAM_ERROR,"该队伍人数名额已满5人无法加入");
            }

            // id1 team1
            // id1 team2
            // id1 team3
            // id7 team1
            // id1 team1  用户id1 不能加入自己创建的队伍
            // id7 team1  用户id17 不能重复加入已加入的队伍
            Long userLoginId = userLogin.getId();
            wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userLoginId);
            // 根据登录用户id查询队伍关系表中是否存在记录
            // 如果存在 要么搜索到的所有数据中存在自己创建的，要么就是自己已经加入过的
            List<UserTeam> userTeamList = userTeamService.list(wrapper);
            if(userTeamList != null){
                for(UserTeam userTeam:userTeamList){
                    if(teamId == userTeam.getTeamId()){
                        // 比较查询出来的userTeam记录中循环遍历所有数据判断 是否存在与当前登入的id相同的数据记录
                        throw new BusinessException(ErrorType.PARAM_ERROR,"用户已在队伍中无法加入");
                    }
                }
            }
            // 插入
            UserTeam addUserTeam = new UserTeam();
            addUserTeam.setTeamId(teamId);
            addUserTeam.setUserId(userLoginId);
            boolean save = userTeamService.save(addUserTeam);
            if(!save){
                throw new SystemException(ErrorType.INSERT_ERROR,"sql插入数据异常");
            }
            return save;
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

    /**
     * 更新队伍信息 是幂等的
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"修改参数为空");
        }
        User userLogin = (User) request.getSession().getAttribute(Constant.LOGIN_STATUS);
        if(userLogin == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"请先登录");
        }
        Long teamId = teamUpdateRequest.getId();

        Team team = teamMapper.selectById(teamId);
        if(team == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"队伍不存在");
        }
        // 只有管理员和队伍创建者才能更改队伍信息
        Long createId = team.getCreateId();
        // Long 类型数据是对象进行值比较不能用  == > < 来比较，需要用 Long.equals() 方法
        if (!createId.equals(userLogin.getId()) && (!userServiceImpl.isAdmin(request))) {
            // 是创建者 不是管理员 不进来  false true - false
            // 不是创建者 但是管理员 不进来 true false - false
            // 不是创建者 不是管理员 才进来 true true - true
            // 是创建者 是管理员 不进来 false false - false
            throw new BusinessException(ErrorType.AUTH_ERROR, "非创建者和管理员才能更改队伍信息");
        }
        Integer teamStatus = team.getStatus();
        if(teamStatus == TeamStatusEnum.ENCRYPT.getStatus()){
            String password = teamUpdateRequest.getPassword();
            String md5 = encrypt.add(password);
            if(StringUtils.isBlank(password)){
                throw new BusinessException(ErrorType.PARAM_ERROR,"加密队伍必须提供密码");
            }
            if(!StringUtils.equals(md5,team.getPassword())){
                throw new BusinessException(ErrorType.PARAM_ERROR,"密码匹配错误");
            }
        }
        Integer status = teamUpdateRequest.getStatus();
        String description = teamUpdateRequest.getDescription();
        Date expireTime = teamUpdateRequest.getExpireTime();
        String name = teamUpdateRequest.getName();
        if(status == teamStatus && description.equals(team.getDescription()) &&
                expireTime.equals(team.getExpireTime()) && name.equals(team.getName())){
            Date time = team.getExpireTime();
            System.out.println(time);
            log.debug("数据未变化无效更新"); // 用户传入的新值和老值一致，就不用update了，降低sql的使用
        }else{
            Team updateTeam = new Team();
            BeanUtils.copyProperties(teamUpdateRequest, updateTeam, "password"); // 属性复制的时候密码又改为加密前的密码了
            int i = teamMapper.updateById(updateTeam);
            if(i == 0){
                throw new SystemException(ErrorType.UPDATE_ERROR,"sql更新语句异常");
            }
        }
        return true;
    }





}




