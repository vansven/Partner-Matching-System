package com.neu.vansven.controller;

import com.neu.vansven.controller.projectexception.BusinessException;
import com.neu.vansven.controller.projectexception.SystemException;
import com.neu.vansven.domain.User;
import com.neu.vansven.domain.request.UserLoginRequest;
import com.neu.vansven.domain.request.UserRegisterRequest;
import com.neu.vansven.domain.response.BaseResponse;
import com.neu.vansven.domain.errortype.ErrorType;
import com.neu.vansven.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user") // @RequestMapping注解 是类中所有标注 RequestMapping 注解的 HTTP服务起始端点
@Slf4j
public class UserController {

    @Autowired
    private UserServiceImpl userServiceImpl;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        String account = userRegisterRequest.getAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if(StringUtils.isAnyBlank(account, password, checkPassword)){
            return new BaseResponse<>(ErrorType.PARAM_ERROR.getCode(),
                    null,
                    ErrorType.PARAM_ERROR.getMessage(),
                    "账户密码非空"); // done 参数异常 账户密码非空
        }

        long registerID = userServiceImpl.userRegister(account, password, checkPassword);
        return new BaseResponse<>(0,registerID,"ok");
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();

        if(StringUtils.isAnyBlank(account, password)){
            return new BaseResponse<>(ErrorType.PARAM_ERROR.getCode(),
                    null,
                    ErrorType.PARAM_ERROR.getMessage(),
                    "账户密码非空"); // done 参数异常 账户密码非空
        }

        User userInfo = userServiceImpl.userLogin(account, password, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                userInfo,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "登录成功"
        );
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> uesrSearchByUserAccount(String useAccount, HttpServletRequest request){
        if(useAccount == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"请输入查询账户名");
        }
        if(request.getSession().getAttribute("userlogin") == null){
            throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录");
        }
        List<User> userList = userServiceImpl.searchUser(useAccount, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                userList,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "查询成功"
        );
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteUserById(@PathVariable int id, HttpServletRequest request){
        if(request.getSession().getAttribute("userlogin") == null){
            throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录");
        }
        boolean isDeleted = userServiceImpl.deleteUser(id, request);
        return new BaseResponse<Boolean>(0,isDeleted,"OK","根据ID删除");
    }

    @PostMapping
    public BaseResponse<String> userLogout(HttpServletRequest request){
        if(request == null){
            return new BaseResponse<>(ErrorType.REGISTER_ERROR.getCode(),
                    null,
                    ErrorType.PARAM_ERROR.getMessage(),
                    "账户密码非空"); // done 请求异常  HTTP请求错误
        }
        userServiceImpl.userLogout(request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                null,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "注销成功"
        );
    }

    // 不加 @RequestParam 注解：
    // --> http://localhost:8080/vansven/user/search/tags?tagList=java%2Cc%2B%2B，但是会报500错误，服务器端肯定没解析成功
    // 加 @RequestParam 注解
    // --> http://localhost:8080/vansven/user/search/tags?tagList=java%2Cc%2B%2B，响应成功，服务器端肯定解析成功了
    @GetMapping("/search/tags/{pageNum}/{pageSize}")
    public BaseResponse<List<User>> userSearchByTags(@PathVariable Long pageNum,
                                                     @PathVariable Long pageSize,
                                                     @RequestParam List<String> tagList,
                                                     HttpServletRequest request) {

        if (tagList.size() == 0) {
            throw new BusinessException(ErrorType.PARAM_ERROR, "标签名非空");
        }
        if (request.getSession().getAttribute("userlogin") == null) {
            throw new BusinessException(ErrorType.AUTH_ERROR, "请先登录");
        }
        List<User> userList = userServiceImpl.searchUserByTags(tagList, pageNum, pageSize, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                userList,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "查询成功"
        );
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest request){
        if(request.getSession().getAttribute("userlogin") == null){
            throw new SystemException(ErrorType.AUTH_ERROR, "请先登录");
        }
        if(user == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"未选择用户");
        }
        boolean isUpdate = userServiceImpl.updateById(user, request);
        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                null,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                "更新成功"
        );
    }

    @GetMapping("/search/{pageNum}/{pageSize}")
    public BaseResponse<List<User>> userSearch(@PathVariable Long pageNum,
                                              @PathVariable Long pageSize,
                                              HttpServletRequest request){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        if(request.getSession().getAttribute("userlogin") == null){
            throw new BusinessException(ErrorType.AUTH_ERROR,"请先登录");
        }

        // redis 缓存
        String redisKey = "partner:use:searchdata:%d";
        User userlogin = (User)request.getSession().getAttribute("userlogin");
        Long userloginId = userlogin.getId();
        String user = String.format(redisKey, userloginId); // key 存储某个用户id
        ValueOperations<String, List<User>> ops = redisTemplate.opsForValue();
        List<User> userList;
        if(ops.get(user) == null){

            userList = userServiceImpl.searchAll(pageNum,pageSize,request); // 该用户id频繁访问的热点数据，比如第一页的20条数据
            // k 使用string序列化
            // v 使用的是json序列化，将list集合中的所有元素进行json序列化后存储，这可以在rdm上看出是json格式存储的
            ops.set(user, userList, 30, TimeUnit.SECONDS);
        }else {
            // 在获取redis缓存中的数据时，会先将redis中的 k v 分别进行string反序列化 json反序列化（成list集合返回），并和给定的 k 关联
            userList = ops.get(user);
        }

        stopWatch.stop();

        return new BaseResponse<>(
                ErrorType.SUCCESS_REQUEST.getCode(),
                userList,
                ErrorType.SUCCESS_REQUEST.getMessage(),
                // 未用缓存查询第一页 30000 条数据耗时 1406 ms 注意设置了过期时间
                // 使用缓存查询第一页 30000 条数据耗时 506 ms 注意设置了过期时间
                "查询成功，耗时为" + stopWatch.getTotalTimeMillis());
    }
}
