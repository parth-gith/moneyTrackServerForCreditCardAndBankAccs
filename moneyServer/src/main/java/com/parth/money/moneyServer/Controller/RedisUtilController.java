package com.parth.money.moneyServer.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/moneyServer/RedisUtil")
public class RedisUtilController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/getAllCacheData")
    public HashMap<String,String> getAllCache(){
        Set<String> keyss = redisTemplate.keys("*");
        if (keyss == null || keyss.isEmpty()) {
            return new HashMap<>();
        }
        List<String> vals = redisTemplate.opsForValue().multiGet(keyss);
        int i = 0;
        HashMap<String,String> data = new HashMap<>();
        for(String key : keyss){
            data.put(key,vals.get(i++));
        }
        return data;
    }
}
