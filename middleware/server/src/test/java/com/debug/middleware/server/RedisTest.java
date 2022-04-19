package com.debug.middleware.server;

import com.debug.middleware.server.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//单元测试类
@SpringBootTest
public class RedisTest {
    //定义日志
    private static final Logger log = LoggerFactory.getLogger(RedisTest.class);

    //由于之前已经自定义注入RedisTemplate组件，故而在此可以直接自动配置
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //定义StringRedisTemplate操作组件
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //定义JSON序列化与反序列化框架类
    @Autowired
    private ObjectMapper objectMapper;


    //采用RedisTemplate将一字符串信息写入缓存，并读取出来
    @Test
    public void one() {
        log.info("-----开始RedisTemplate操作组件实战-----");
        //定义字符串内容以及存入缓存的Key
        final String content = "RedisTemplate实战字符串信息";
        final String key = "redis:template:one:string";
        //Redis通用的操作组件
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //将字符串信息写入缓存
        log.info("写入缓存内容：{} ", content);
        valueOperations.set(key, content);
        //从缓存中读取内容
        Object result = valueOperations.get(key);
        log.info("读取出来的内容：{} ", result);
    }

    //采用RedisTemplate将对象信息序列化为JSON格式字符串后写入缓存
    //然后将其读取出来，最后反序列化解析其中内容并输出到控制台
    @Test
    public void two() throws JsonProcessingException {
        log.info("-----开始RedisTemplate操作组件实战-----");
        //构造对象信息
        User user = new User(1,"debug", "阿修罗");
        //Redis通用的操作组件
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //将序列化后的信息写入缓存
        final String key = "redis:template:two:object";
        final String content = objectMapper.writeValueAsString(user);
        valueOperations.set(key, content);
        log.info("写入缓存对象的信息， {}", user);
        //从缓存中读取内容
        Object result = valueOperations.get(key);
        if (result != null) {
            User resultUser = objectMapper.readValue(result.toString(), User.class);
            log.info("读取缓存内容并反序列化后的结果：{}", resultUser);
        }
    }

    //采用StringRedisTemplate将一字符串信息写入缓存，并读取出来
    @Test
    public void three() {
        log.info("-----开始RedisTemplate操作组件实战----");
        //定义字符串内容并存入缓存的key
        final String content = "StringRedisTemplate实战字符串信息";
        final String key = "redis:three";
        //Redis通用操作组件
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        //将字符串信息写入缓存
        log.info("写入缓存的内容：{} ", content);
        valueOperations.set(key, content);
        //从缓存中读取内容
        String result = valueOperations.get(key);
        log.info("读取出来的内容：{} ", result);
    }

    //采用StringRedisTemplate将一对象信息序列化为JSON格式字符串后写入缓存
    //然后读取出来，最后反序列化解析其中的内容并输出到控制台
    @Test
    public void four() throws JsonProcessingException {
        log.info("----开始StringRedisTemplate操作组件实战-----");
        //构造对象信息
        User user = new User(2, "SteadyJack", "阿修罗");
        //Redis通用组件
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        //将序列化的信息写入缓存
        final String key = "redis:four";
        final String content  = objectMapper.writeValueAsString(user);
        valueOperations.set(key, content);
        log.info("写入缓存信息：{} ", content);
        //从缓存中读取内容
        String result = valueOperations.get(key);
        if (result != null) {
            User resultUser = objectMapper.readValue(result, User.class);
            log.info("读取缓存内容并反序列化后的结果：{} ", resultUser);
        }
    }
}
