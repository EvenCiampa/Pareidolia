package com.pareidolia.configuration.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingControllerDecorator {
    private static final Logger logger = LoggerFactory.getLogger(LoggingControllerDecorator.class);

    @Around("execution(* com.pareidolia.controller..*.*(..))")
    public Object logControllerCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        logger.info("Starting {} in {}", methodName, className);
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            logger.info("Completed {} in {} ms", methodName, (endTime - startTime));
            return result;
        } catch (Exception e) {
            logger.error("Error in {} - {}", methodName, e.getMessage());
            throw e;
        }
    }
} 