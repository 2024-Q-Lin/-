package com.sky.aspect;

import com.sky.annotation.AutoFill;
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
public class AutoFillAspect {
    /**
     * 定义切入点表达式
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) " +
            "&& @annotation(com.sky.annotation.AutoFill)")
    private void autoFillPointCut(){}

    /**
     * 在 Spring AOP 中，joinPoint.getSignature()
     * 返回的是 Signature 接口的一个实例，
     * 而 Signature 是一个通用的签名接口，用于描述连接点（JoinPoint）上的签名信息。
     * Signature 接口没有提供获取方法详细信息（如参数、返回类型等）的方法。
     * 所以，当我们希望获取方法级别的详细信息时，就需要将其转换为 MethodSignature。
     * <p>
     * MethodSignature 是一个接口，我们不能直接创建接口的实例。
     * 然而，在使用 Spring AOP 时，当我们调用 joinPoint.getSignature() 时，
     * 实际上 Spring AOP 返回的是 MethodSignature 接口的一个实现类的实例。
     * 这是因为 joinPoint.getSignature() 返回的是 Signature 接口的实例(用多态返回Signature类型)，
     * 而具体的实例可能是其某个实现类（如 MethodSignature）的对象。
     * <p>
     * 然而，Java 支持多态性——在运行时，方法可以返回 Signature 接口的任意实现类的实例。
     * 所以，尽管编译器只知道它是 Signature 类型，但在运行时，它实际是某个实现类的对象。
     */
    @Before("autoFillPointCut()")
    public static void autoFill(JoinPoint joinPoint) {
        log.info("开始公共字段自动填充...");

        /*
        -----获取到当前被拦截的方法上的数据库操作类型
         */
        // 获取到当前被拦截的方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取到当前被拦截的方法
        Method method = signature.getMethod();
        // 获取到当前方法的注解
        AutoFill autoFillAnnotation = method.getAnnotation(AutoFill.class);
        // 获取到当前注解的值
        OperationType operationType = autoFillAnnotation.value();

        /*
        -----获取到当前被拦截的方法参数-实体对象
         */
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];// 默认约定在方法中，传入的实体类放在第一个参数

        /*
        -----准备赋值的数据
         */
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        /*
        -----判断当前不同的操作类型，为对应的属性通过反射来赋值
         */
        if (operationType == OperationType.INSERT) {
            try {
                //这段代码的功能是从 entity 对象的类中获取名为 set--- 的方法，该方法接受一个 LocalDateTime/--- 类型的参数。
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射调用对象的方法
                // setCreateTime.invoke(entity, now)的作用是调用entity对象上的setCreateTime方法，并传入当前时间now作为参数。
                /*
                   Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                我已经通过这个语句得到这是entity这个对象中的一个方法setCreateTime，为什么在反射的时候invoke()中，
                还要传入一个entity对象来声明是entity中的方法setCreateTime，这不是多此一举吗
                --回答：在Java反射中，Method对象表示的是一个类的方法，但它并不直接绑定到任何特定的对象实例。
                     即使你知道setCreateTime方法属于entity对象，Method对象本身并不知道它应该作用于哪个具体的对象实例。
                     Method对象本身只知道方法的签名（名字和参数类型），并不知道具体哪个对象实例会调用这个方法
                     如果另一个类有相同的方法名、相同的传入参数类型，并且这个方法是可以访问的，那么反射调用不会抛出IllegalArgumentException。
                 */
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
