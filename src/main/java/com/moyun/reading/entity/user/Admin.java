package com.moyun.reading.entity.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员实体类 - 继承导师
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Teacher {

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level")
    private AdminLevel adminLevel = AdminLevel.NORMAL;

    @Size(max = 200, message = "管理权限描述长度不能超过200个字符")
    @Column(name = "permissions", length = 200)
    private String permissions;

    @Column(name = "can_delete_users")
    private Boolean canDeleteUsers = false;

    @Column(name = "can_manage_system")
    private Boolean canManageSystem = false;

    @Override
    public UserRole getUserRole() {
        return UserRole.ADMIN;
    }

    /**
     * 管理员级别枚举
     */
    public enum AdminLevel {
        NORMAL("普通管理员"),
        SENIOR("高级管理员"),
        SUPER("超级管理员");

        private final String displayName;

        AdminLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return AdminLevel.SUPER.equals(adminLevel);
    }

    /**
     * 是否有删除权限
     */
    public boolean hasDeletePermission() {
        return canDeleteUsers || isSuperAdmin();
    }

    /**
     * 是否有系统管理权限
     */
    public boolean hasSystemManagePermission() {
        return canManageSystem || isSuperAdmin();
    }
}