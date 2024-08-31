package com.neu.vansven.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.vansven.controller.projectexception.BusinessException;
import com.neu.vansven.controller.projectexception.SystemException;
import com.neu.vansven.domain.User;
import com.neu.vansven.domain.errortype.ErrorType;
import com.neu.vansven.mapper.UserMapper;
import com.neu.vansven.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* @author vansven
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-04-05 20:49:53
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;


    public long userRegister(String account, String password, String checkPassword){
        //1、校验
        if(StringUtils.isAnyEmpty(account,password,checkPassword)){
            throw new BusinessException(ErrorType.PARAM_ERROR,"账户密码非空");  // done 参数异常 账户密码非空
        }
        if(account.length() < 4 || password.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorType.PARAM_ERROR,"账户密码长度过短"); // done 参数异常 账户密码长度过短
        }

        //账户不包含特殊字符，只允许数字、字母、下划线
        // 1、先定义一个String类型的正则表达式包括所有特殊字符
        // 2、再用java中的Pattern和Matcher进行匹配字符串，
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(account);
        if(matcher.find()){
            throw new BusinessException(ErrorType.PARAM_ERROR, "账户不允许包括特殊字符"); // done 参数异常 账户不允许包括特殊字符
        }
        //密码和校验密码要保持一致
        if(!StringUtils.equals(password,checkPassword)){
            throw new BusinessException(ErrorType.PARAM_ERROR,"密码不一致异常");  // done 参数异常 密码不一致异常
        }

        //账户不能重复：1、按指定账户名查询所有用户如果结果不为空就存在相同的
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like(" user_account",account);
        long count = userMapper.selectCount(wrapper);
        if(count > 0){
            throw new BusinessException(ErrorType.PARAM_ERROR,"账户不允许重复");  // done 参数异常 账户不允许重复
        }

        //2、密码加密 相同命名空间总是生成相同的UUID
        String salt = UUID.nameUUIDFromBytes("vansven".getBytes()).toString().replaceAll("-","");
        String md5Password = DigestUtils.md5DigestAsHex((salt + password).getBytes());

        //3、向数据库插入数据
        User user = new User();
        user.setUserAccount(account);
        user.setPassword(md5Password);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new SystemException(ErrorType.REGISTER_ERROR,"数据库插入失败"); // done 注册异常 数据库插入失败
        }
        return user.getId();
    }

    public User userLogin(String account, String password, HttpServletRequest request){

        //1、校验账户和密码
        if(StringUtils.isAnyEmpty(account, password)){ // 只要有一个不为空
            throw new BusinessException(ErrorType.PARAM_ERROR,"账户密码非空");  // done 参数异常 账户密码非空
        }
        if(account.length() < 4 || password.length() < 8){
            throw new BusinessException(ErrorType.PARAM_ERROR,"账户密码长度过短"); // done 参数异常 账户密码长度过短
        }

        //账户不包含特殊字符，只允许数字、字母、下划线
        // 1、先定义一个String类型的正则表达式包括所有特殊字符
        // 2、再用java中的Pattern和Matcher进行匹配字符串，
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(account);
        if(matcher.find()){
            throw new BusinessException(ErrorType.PARAM_ERROR,"账户不允许包括特殊字符");  // done 参数异常 账户不允许包括特殊字符
        }

        String salt = UUID.nameUUIDFromBytes("vansven".getBytes()).toString().replaceAll("-","");
        String md5 = DigestUtils.md5DigestAsHex((salt + password).getBytes());

        // 2、根据账户和校验密码查询是否存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        // select count(*) from User where account = ?
        wrapper.eq("user_account", account);
        wrapper.eq("password",md5);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"账户未注册"); // done 参数异常 未注册无法登录
        }

        User userSafty = getSafetyUser(user);

        //3、记录用户的登录态(就是保存返回给前端的信息)
        HttpSession session = request.getSession();
        session.setAttribute("userlogin",userSafty);

        return userSafty;



    }

    public List<User> searchUser(String userName, HttpServletRequest request){
        // 用户鉴权
        if(!isAdmin(request)){
            throw new BusinessException(ErrorType.AUTH_ERROR,"非管理员禁止查询用户"); // done 权限异常 非管理员禁止查询用户
        }

        // 查询：select * from user where user_name like %userName%
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account",userName);
        List<User> orignalUserList = userMapper.selectList(wrapper);
        if(orignalUserList.size() == 0){
            throw new BusinessException(ErrorType.PARAM_ERROR,"无法匹配到该用户"); // done 参数异常 无法匹配到该用户
        }

        // 数据脱敏
        List<User> users = orignalUserList.stream().map((User element) -> {
            return getSafetyUser(element);
        }).collect(Collectors.toList());

        return users;
    }

    public boolean deleteUser(long id, HttpServletRequest request){
        // 用户鉴权
        if(!isAdmin(request)){
            throw new BusinessException(ErrorType.AUTH_ERROR,"非管理员禁止删除用户"); // done 权限异常 非管理员禁止删除用户
        }
        if(id < 0){
            throw new BusinessException(ErrorType.PARAM_ERROR, "不存在id小于0的用户"); // done 参数异常 不存在id小于0的用户
        }
        //判断是否存在该用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id);
        List<User> users = userMapper.selectList(wrapper);

        Long count = userMapper.selectCount(wrapper);
        if(count <= 0){
            throw new SystemException(ErrorType.DELETE_ERROR, "不存在该用户"); // done 删除异常 不存在该用户
        }

        //删除
        return userMapper.delete(wrapper) != 0;
    }

    public void userLogout(HttpServletRequest request){
         request.getSession().removeAttribute("userlogin");
    }

    /**
     * 数据脱敏
     * @param orignalUser
     * @return
     */
    public User getSafetyUser(User orignalUser){
        User userSafty = new User();
        userSafty.setId(orignalUser.getId());
        userSafty.setUserName(orignalUser.getUserName());
        userSafty.setUserAccount(orignalUser.getUserAccount());
        userSafty.setAvatarUrl(orignalUser.getAvatarUrl());
        userSafty.setGender(orignalUser.getGender());
        userSafty.setPhone(orignalUser.getPhone());
        userSafty.setEmail(orignalUser.getEmail());
        userSafty.setUserRole(orignalUser.getUserRole());
        userSafty.setTags(orignalUser.getTags());
        return userSafty;
    }

    public boolean isAdmin(HttpServletRequest request){
        HttpSession session = request.getSession();
        Object object = session.getAttribute("userlogin");
        User user = (User) object;
        if(user == null || user.getUserRole() != 1){
            return false;
        }
        return true;
    }

    /**
     * 根据标签搜索用户（内存查询）
     * @param tagList
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagList, Long currentPage, Long size, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorType.PARAM_ERROR,"非管理员无法查看"); // done 权限异常 非管理员无法查看
        }
        if(tagList == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"传入标签为空"); // done 参数异常 传入标签为空
        }
        //方案二：内存查询，需要先查询到所有记录存储到内存中，然后在内存中判断
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        //1、先查询所有用户
        List<User> userList = userMapper.selectList(wrapper);
        //2、对所有用户中的tags字段(json字符串)转换为java对象(lis集合对象)
        ObjectMapper mapper = new ObjectMapper();
        Stream<User> filterUser = userList.stream().filter(user -> {
            String tagName = user.getTags();
            if(StringUtils.equals(tagName,"") || StringUtils.isEmpty(tagName)){ //判空
                return false;
            }
            try {
                List<String> tagNameList = mapper.readValue(tagName, List.class); // 将tags字段（json字符串）转换为集合对象
                // 只包含 -- 精确匹配记录tags字段中的 json数组字符串
                if(tagNameList.size() == tagList.size()){ // 1、先粗粒度对比集合大小
                    for (String tag : tagList) {
                        if (!tagNameList.contains(tag)) { // 2、循环遍历传入列表字段只要传入的列表字段中有一个不被包含在tags字段中的标签中就一定不能匹配
                            return false;
                        }
                    }
                    return true; // 3、如果传入的列表字段都包含在tags字段里则返回该数据记录
                }
                return false; // 4、如果两个集合大小都不一样一定不返回该数据记录
            } catch (JsonProcessingException e) {
                throw new SystemException(ErrorType.SEARCH_ERROR,"json序列化错误"); // done json序列化异常
            }
        }).skip((currentPage - 1) * size).limit(size); // fixme java8 stream流实现分页功能
        //3、对符合条件的记录数据脱敏返回，防止信息泄漏
        return filterUser.map(user -> {return getSafetyUser(user);}).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户（SQL查询）
     *
     * @param listTags
     * @return
     */
    @Deprecated
    @Override
    public List<User> searchUserByTagsBySQL(List<String> listTags, HttpServletRequest request) {
        if(isAdmin(request)){
            throw new BusinessException(ErrorType.PARAM_ERROR,"非管理无法查看"); // done 权限异常 非管理员无法查看
        }
        if(listTags == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"传入标签为空"); // done 参数异常 传入标签为空
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        //方案一：SQL查询方式 done 模糊查询并不能精确匹配，如何精确匹配是个问题 --> 可以使用正则表达式匹配
        // select * from user where tags = "java" and tags = "c++" 错误的精确匹配查询方式
        // select * from user where tags like "%java%" and tags like "%c++%" 模糊查询 --> 既有java又有c++，不是只包含关系
        for(String tag:listTags){
            wrapper.like("tags",tag);
        }
        List<User> userList = userMapper.selectList(wrapper); //先模糊查询出所有符合条件的数据记录，再在内存中判断
        return userList.stream().map(user -> {return getSafetyUser(user);}).collect(Collectors.toList());
    }

    @Override
    public boolean updateById(User user, HttpServletRequest request){
        int userId = user.getId().intValue();  // 接收的用户 id
        int loginUserId  = getLoginUser(request).getId().intValue();// 当前登录用户 id
        // 如果不是管理员，用户只能修改自己的信息
        // 如果是管理员，可以修改任意用户信息
        if(!isAdmin(request) && userId != loginUserId){
            throw new BusinessException(ErrorType.PARAM_ERROR,"非管理员只能修改自己信息"); //
        }
        // 管理员修改任意用户信息需要先查询数据库是否有对应的用户才能修改
        User updateUser = userMapper.selectById(userId);
        if(updateUser == null){
            throw new BusinessException(ErrorType.PARAM_ERROR,"用户不存在");
        }
        return userMapper.updateById(user) != 0;
    }


    @Override
    public List<User> searchAll(Long currentPage, Long pageSize, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorType.AUTH_ERROR,"非管理员无法查看");
        }
        Page<User> page = new Page<>(currentPage,pageSize);
        List<User> userList = userMapper.selectPage(page, null).getRecords();
        List<User> safetyUser = userList.stream().map(user -> {
            return getSafetyUser(user);
        }).collect(Collectors.toList());
        return safetyUser;
    }

    private User getLoginUser(HttpServletRequest request){
        HttpSession session = request.getSession();
        Object object = session.getAttribute("userlogin");
        User originalUser = (User) object;
        return getSafetyUser(originalUser);
    }
}




