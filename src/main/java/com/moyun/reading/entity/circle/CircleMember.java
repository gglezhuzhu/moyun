package com.moyun.reading.entity.circle;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 圈子成员实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "circle_members", uniqueConstraints = @UniqueConstraint(columnNames = { "circle_id", "user_id" }))
public class CircleMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circle_id", nullable = false)
    private Circle circle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role = MemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    /**
     * 成员角色枚举
     */
    public enum MemberRole {
        CREATOR("创建者"),
        ADMIN("管理员"),
        MEMBER("成员");

        private final String displayName;

        MemberRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 成员状态枚举
     */
    public enum MemberStatus {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        BANNED("已禁用");

        private final String displayName;

        MemberStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 是否为创建者
     */
    public boolean isCreator() {
        return MemberRole.CREATOR.equals(role);
    }

    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return MemberRole.ADMIN.equals(role) || isCreator();
    }

    /**
     * 是否活跃
     */
    public boolean isActive() {
        return MemberStatus.ACTIVE.equals(status);
    }

    /**
     * 是否可以管理圈子
     */
    public boolean canManageCircle() {
        return isActive() && isAdmin();
    }

    /**
     * 是否可以发帖
     */
    public boolean canPost() {
        return isActive();
    }
}