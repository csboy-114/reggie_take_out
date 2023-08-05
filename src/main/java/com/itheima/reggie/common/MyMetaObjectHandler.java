package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill ...."); // 4个字段，createUser,updateUser,createTime,updateTime
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject,"createUser",Long.class,BaseContext.getCurrentId());
        strictInsertFill(metaObject,"updateUser",Long.class,BaseContext.getCurrentId());


    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("start update fill ...."); // 2个字段,updateUser updateTime
        log.info("线程id：{}",Thread.currentThread().getId());
        log.info("BaseContext.getCurrentId():{}",BaseContext.getCurrentId());
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject,"updateUser",Long.class,BaseContext.getCurrentId());
    }
}
