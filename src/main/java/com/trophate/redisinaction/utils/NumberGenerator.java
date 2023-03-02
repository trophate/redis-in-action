package com.trophate.redisinaction.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NumberGenerator {

    private final RedisTemplate<Object, Object> redisTemplateBean;
    private static RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    public NumberGenerator(RedisTemplate<Object, Object> redisTemplate) {
        redisTemplateBean = redisTemplate;
    }

    @PostConstruct
    public void init() {
        NumberGenerator.redisTemplate = this.redisTemplateBean;
    }

    public static int getArticleId() {
        return NumberGenerator.redisTemplate.opsForValue().increment("articleId").intValue();
    }
}
