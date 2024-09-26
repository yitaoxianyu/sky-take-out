package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import com.sky.handler.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j //日志注解
public class AutoFillAspect
{
    //切面包括切点和通知，其中切点表示要进行代码增强代码的对应位置
    //而通知为增强代码

    //第一个*表示对象第二个表示所有的方法
     @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
     public void autoFillPointCut(){

     }


     @Before("autoFillPointCut()")//连接切点和对应的通知
     public void autoFill(JoinPoint joinPoint)
     //joinPoint拿到对应的注解
     {
        log.info("开始对应字段填充");

        //获取签名
         MethodSignature signature = (MethodSignature) joinPoint.getSignature();

         //获取注解
         AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);

         OperationType OperationType = autoFill.value();

         Object[] args = joinPoint.getArgs();

        if(args == null){
            return ;
        }

         Object arg = args[0];

         LocalDateTime now = LocalDateTime.now();

         Long currentId = BaseContext.getCurrentId();

         //获取当前用户id使用在同一线程中的localThread
         if(OperationType == com.sky.enumeration.OperationType.INSERT) {
             //利用反射拿到类中的方法
            try{
                Method setCreateTime = arg.getClass().getMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = arg.getClass().getMethod("setCreateUser", Long.class);
                Method setUpdateTime = arg.getClass().getMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getMethod("setUpdateUser", Long.class);



                setCreateTime.invoke(arg,now);
                setCreateUser.invoke(arg,currentId);
                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentId);

            }
            catch (Exception e){
                e.printStackTrace();//打印异常信息
            }
         }else if(OperationType == com.sky.enumeration.OperationType.UPDATE){
             try{
                 Method setUpdateTime = arg.getClass().getMethod("setUpdateTime", LocalDateTime.class);
                 Method setUpdateUser = arg.getClass().getMethod("setUpdateUser", Long.class);


                 setUpdateTime.invoke(arg,now);
                 setUpdateUser.invoke(arg,currentId);
             }
             catch(Exception e){
                 e.printStackTrace();
             }
     }
     }

}
