package com.debug.middleware.server;

import com.debug.middleware.server.entity.Fruit;
import com.debug.middleware.server.entity.Person;
import com.debug.middleware.server.entity.PhoneUser;
import com.debug.middleware.server.entity.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedisTest2 {
    //定义日志
    private static final Logger log = LoggerFactory.getLogger(RedisTest2.class);

    //定义RedisTemplate操作组件
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //JSON序列化与反序列化框架类
    @Autowired
    private ObjectMapper objectMapper;

    //单元测试方法->String类型
    @Test
    public void one() throws JsonProcessingException {
        //构造用户实体对象
        Person p = new Person(10013, 23, "修罗", "debug", "火星");
        //定义key与即将写入缓存的value
        final String key = "redis:test:1";
        String value = objectMapper.writeValueAsString(p);
        //写入缓存
        log.info("存入缓存中用户实体对象信息为： {} ", p);
        redisTemplate.opsForValue().set(key, value);
        //从缓存中获取用户实体信息
        Object res = redisTemplate.opsForValue().get(key);
        if (res != null) {
            Person resP = objectMapper.readValue(res.toString(), Person.class);
            log.info("从缓存中读取信息：{} ", resP);
        }
    }

    //List类型
    @Test
    public void two() {
        //构造已经排好序的用户对象列表
        List<Person> list = new ArrayList<>();
        list.add(new Person(1, 21, "修罗", "debug", "火星"));
        list.add(new Person(2, 22, "大圣", "jack", "水帘洞"));
        list.add(new Person(3, 23, "盘古", "Lee", "上古"));
        log.info("构造已经排好序的用户对象列表：{} ", list);
        //将列表数据存储至Redis的List中
        final String key = "redis:test:2";
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        for (Person p : list) {
            //往列表中添加数据（从队尾添加）
            listOperations.leftPush(key, p);
        }
        //获取Redis中List的数据（从队头中遍历获取，直到没有元素为止
        log.info("--获取Redis中List的数据-从队头中获取--");
        Object res = listOperations.rightPop(key);
        Person resp;
        while (res != null) {
            resp = (Person) res;
            log.info("当前数据： {} ", resp);
            res = listOperations.rightPop(key);
        }
    }

    //Set类型
    @Test
    public void three() {
        //构造一组用户姓名列表
        List<String> list = new ArrayList<>();
        list.add("debug");
        list.add("jack");
        list.add("修罗");
        list.add("大圣");
        list.add("debug");
        list.add("jack");
        list.add("steadyheart");
        list.add("修罗");
        list.add("大圣");
        log.info("待处理的用户姓名列表：{} ", list);
        //遍历访问，剔除相同姓名的用户并写入集合，最终存入缓存
        final String key = "redis:test:3";
        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        for (String str : list) {
            setOperations.add(key, str);
        }
        //从缓存中获取用户对象集合
        Object res = setOperations.pop(key);
        while (res != null) {
            log.info("从缓存中获取的用户集合-当前用户：{} ", res);
            res = setOperations.pop(key);
        }
    }

    //SortedSet类型
    @Test
    public void four() {
        //构造一组无序的用户手机充值对象列表
        List<PhoneUser> list = new ArrayList<>();
        list.add(new PhoneUser("103", 130.0));
        list.add(new PhoneUser("101", 120.0));
        list.add(new PhoneUser("102", 80.0));
        list.add(new PhoneUser("105", 70.0));
        list.add(new PhoneUser("106", 50.0));
        list.add(new PhoneUser("104", 150.0));
        log.info("构造一组无序的用户充值对象列表： {} ", list);
        //遍历访问充值对象列表，将信息写入Redis的有序集合
        final String key = "redis:test:4";
        redisTemplate.delete(key);
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        for (PhoneUser u : list) {
            zSetOperations.add(key, u, u.getFare());
        }
        Long size = zSetOperations.size(key);
        Set<Object> resSet = zSetOperations.range(key, 0l, size);
        assert resSet != null;
        for (Object o : resSet) {
            log.info("从缓存中读取手机充值记录排序列表，当前记录：{} ",o);
        }
    }

    //Hash类型
    @Test
    public void five() {
        //构造学生对象列表和水果对象列表
        List<Student> students = new ArrayList<>();
        List<Fruit> fruits = new ArrayList<>();
        //往学生集合中添加学生对象
        students.add(new Student("10010", "debug", "大圣"));
        students.add(new Student("10011", "jack", "修罗"));
        students.add(new Student("10012", "sam", "上古"));
        //往水果集合中添加水果对象
        fruits.add(new Fruit("apple", "红色"));
        fruits.add(new Fruit("orange", "橙色"));
        fruits.add(new Fruit("banana", "黄色"));
        //分别遍历不同对象列表，并采用哈希存储至缓存
        final String sKey = "redis:test:5";
        final String fKey = "redis:test:6";
        redisTemplate.delete(sKey);
        redisTemplate.delete(fKey);
        //获取哈希存储操作组件HashOperations,遍历获取集合中对象并添加进缓存
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        for (Student s: students)
            hashOperations.put(sKey, s.getId(), s);
        for (Fruit f : fruits)
            hashOperations.put(fKey, f.getName(), f);
        //获取学生对象列表与水果对象列表
        Map<Object, Object> sMap = hashOperations.entries(sKey);
        log.info("获取学生对象列表：{} ", sMap);
        Map<Object, Object> fMap = hashOperations.entries(fKey);
        log.info("获取水果对象列表：{} ", fMap);
        //获取指定的学生对象
        String sField = "10012";
        Student s = (Student) hashOperations.get(sKey, sField);
        log.info("获取指定的学生对象：{} -> {} ", sField, s);
        //获取指定的水果对象
        String fField = "orange";
        Fruit f = (Fruit) hashOperations.get(fKey, fField);
        log.info("获取指定的水果对象：{} -> {} ", fField, f);

    }
}
