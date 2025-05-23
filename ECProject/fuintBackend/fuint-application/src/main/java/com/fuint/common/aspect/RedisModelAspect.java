package com.fuint.common.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RedisModelAspect {
    public static final Logger logger = LoggerFactory.getLogger(RedisModelAspect.class);
}
