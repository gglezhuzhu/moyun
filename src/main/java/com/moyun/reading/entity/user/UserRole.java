package com.moyun.reading.entity.user;

/**
 * 用户角色枚举
 * 
 * @author Moyun Team
 */
public enum UserRole {
    USER("普通用户", "ROLE_USER"),
    STUDENT("学生", "ROLE_STUDENT"),
    TEACHER("导师", "ROLE_TEACHER"),
    ADMIN("管理员", "ROLE_ADMIN");

    private final String displayName;
    private final String authority;

    UserRole(String displayName, String authority) {
        this.displayName = displayName;
        this.authority = authority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthority() {
        return authority;
    }
}