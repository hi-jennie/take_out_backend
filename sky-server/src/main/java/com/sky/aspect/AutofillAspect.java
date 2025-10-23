package com.sky.aspect;

import com.sky.annotation.Autofill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutofillAspect {

    /**
     * set the pointcuts
     * all methods in mapper package annotated with @Autofill
     */
    @Pointcut("@annotation(com.sky.annotation.Autofill)  && execution(* com.sky.mapper.*.*(..))")
    public void autofillPointcut() {}

    /**
     * advice to be executed before the pointcut
     * JoinPoint: provides access to method information
     */
    @Before("autofillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        // step1: get the method signature and annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // step2: Get the autofill annotation
        Autofill autofill = signature.getMethod().getAnnotation(Autofill.class);

        // step3: Get the value of the annotation, the value is the property defined in the annotation
        OperationType operationType = autofill.value();

        // step4: Get the arguments of the method(), .getArgs() get all arguments as an array
        Object[] args = joinPoint.getArgs();

        if (args == null || args.length == 0) {
            return;
        }
        // get the first argument, which is usually the entity object to be filled
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if(operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
