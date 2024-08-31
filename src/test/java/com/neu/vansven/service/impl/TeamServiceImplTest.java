package com.neu.vansven.service.impl;

import com.neu.vansven.domain.Team;
import com.neu.vansven.service.TeamService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
class TeamServiceImplTest {

    @Autowired
    private TeamService teamService;

    @Test
    void teamInsert() throws ParseException {
        Team team = new Team();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
        Date date =  simpleDateFormat.parse("2024-5-1 17:30:00");
        team.setName("秋招冲刺");
        team.setDescription("测试队伍");
        team.setExpireTime(date);
        boolean save = teamService.save(team);
        Assertions.assertTrue(save);

    }

}