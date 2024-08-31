package com.neu.vansven.service.impl;
import java.util.Date;

import com.neu.vansven.domain.UserTeam;
import com.neu.vansven.service.UserTeamService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserTeamServiceImplTest {

    @Autowired
    private UserTeamService userTeamService;

    @Test
    void userTeamTest(){
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId((long)1);
        userTeam.setUserId((long)1);
        boolean save = userTeamService.save(userTeam);
        Assertions.assertTrue(save);
    }

}