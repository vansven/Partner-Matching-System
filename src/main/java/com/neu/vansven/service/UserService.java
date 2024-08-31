package com.neu.vansven.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.vansven.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author vansven
* @description 针对表【user】的数据库操作Service
* @createDate 2024-04-05 20:49:53
*/
public interface UserService extends IService<User> {

    long userRegister(String account, String password, String checkPassword);

    User userLogin(String account, String password, HttpServletRequest request);

    List<User> searchUser(String userName, HttpServletRequest request);

    boolean deleteUser(long id, HttpServletRequest request);

    void userLogout(HttpServletRequest request);

    List<User> searchUserByTags(List<String> tagList, Long current, Long size, HttpServletRequest request);

    @Deprecated
    List<User> searchUserByTagsBySQL(List<String> listTags, HttpServletRequest request);

    boolean updateById(User user, HttpServletRequest request);

    List<User> searchAll(Long currentPage, Long pageSize, HttpServletRequest request);
}
