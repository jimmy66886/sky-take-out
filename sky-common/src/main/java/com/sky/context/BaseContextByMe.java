package com.sky.context;

/**
 * @author zzmr
 * @create 2023-08-27 10:33
 */
public class BaseContextByMe {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
