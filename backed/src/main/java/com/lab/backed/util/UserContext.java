package com.lab.backed.util;

public class UserContext {
    private static final ThreadLocal<Integer> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        userIdHolder.set(userId);
    }

    public static Integer getUserId() {
        return userIdHolder.get();
    }

    public static void setRole(String role) {
        roleHolder.set(role);
    }

    public static String getRole() {
        return roleHolder.get();
    }

    public static void setUsername(String username) {
        usernameHolder.set(username);
    }

    public static String getUsername() {
        return usernameHolder.get();
    }

    /**
     * 清除当前线程的用户信息，防止内存泄漏
     */
    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
        usernameHolder.remove();
    }
}
