package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import javassist.bytecode.SignatureAttribute;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    //切点表达式
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    //前置通知--公共属性自动复制
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //在这里需要完成对update,insert类型的操作的updateUser,createUser,updateTime等字段的属性赋值
        log.info("进行公共属性赋值....");
        /*
        首先需要获取到被拦截的方法的数据库操作类型(insert/update)
         */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取到该方法上的AutoFill注解对象，这样就可以知道数据库操作类型
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();
        //获取到数据库操作类型之后，需要获取insert/update方法中的参数对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];
        //赋值
        LocalDateTime now = LocalDateTime.now();
        Long currentID = BaseContext.getCurrentId();
        //区别不同的操作类型
        //insert操作，需要为4个属性赋值,通过反射调用setter方法
        if(operationType == OperationType.INSERT){
            Method setCreateTime = entity.getClass().
                    getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().
                    getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().
                    getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().
                    getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //属性赋值
            setCreateTime.invoke(entity,now);
            setCreateUser.invoke(entity,currentID);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentID);
        }else if(operationType == OperationType.UPDATE){
            Method setUpdateTime = entity.getClass().
                    getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().
                    getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //属性赋值
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentID);
        }
    }
}
