package com.itheima.reggie.common;

public class BaseContext {
    private static final ThreadLocal<Long> THREAD_LOCAL_USERID = new ThreadLocal<>();
    public static Long getCurrentId(){
        return THREAD_LOCAL_USERID.get();
    }
    public static void setCurrentId(Long currentId){
        THREAD_LOCAL_USERID.set(currentId);
    }
}
