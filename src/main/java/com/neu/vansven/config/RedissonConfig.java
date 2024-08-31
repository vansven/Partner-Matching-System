package com.neu.vansven.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")  // 读取yml文件中的配置参数使得程序更灵活
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;


    @Bean
    public RedissonClient getRedissClient(){
        //创建配置
        Config config = new Config();
        String str = "redis://%s:%s";
        String redisAddress = String.format(str, host, port);
        config.useSingleServer().setAddress(redisAddress);
        config.useSingleServer().setDatabase(1);
        config.useSingleServer().setPassword(password);
        //创建redis客户端实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
