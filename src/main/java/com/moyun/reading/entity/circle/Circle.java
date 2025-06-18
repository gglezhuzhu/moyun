package com.moyun.reading.entity.circle;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 圈子实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "circles")
public class Circle extends BaseEntity {

    @NotBlank(message = "圈子名称不能为空")
    @Size(max = 100, message = "圈子名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "圈子描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CircleType type = CircleType.READING_GROUP;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    private CirclePrivacy privacy = CirclePrivacy.PUBLIC;

    @Size(max = 1000, message = "圈子规则长度不能超过1000个字符")
    @Column(name = "rules", length = 1000)
    private String rules;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CircleStatus status = CircleStatus.ACTIVE;

    @Column(name = "max_members")
    private Integer maxMembers = 50;

    @OneToMany(mappedBy = "circle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CircleMember> members;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;

    /**
     * 圈子状态枚举
     */
    public enum CircleStatus {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        ARCHIVED("已归档");

        private final String displayName;

        CircleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 圈子类型枚举
     */
    public enum CircleType {
        READING_GROUP("读书小组"),
        BOOK_CLUB("图书俱乐部"),
        STUDY_GROUP("学习小组"),
        DISCUSSION_GROUP("讨论小组"),
        OTHER("其他");

        private final String displayName;

        CircleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 圈子隐私设置枚举
     */
    public enum CirclePrivacy {
        PUBLIC("公开"),
        PRIVATE("私密"),
        INVITE_ONLY("邀请制");

        private final String displayName;

        CirclePrivacy(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 是否活跃
     */
    public boolean isActive() {
        return CircleStatus.ACTIVE.equals(status);
    }

    /**
     * 增加成员数量
     */
    public void incrementMemberCount() {
        this.memberCount++;
    }

    /**
     * 减少成员数量
     */
    public void decrementMemberCount() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }
    }

    /**
     * 是否可以加入新成员
     */
    public boolean canAddNewMember() {
        return isActive() && (maxMembers == null || memberCount < maxMembers);
    }

    /**
     * 获取圈子信息描述
     */
    public String getCircleInfo() {
        return String.format("%s - %d名成员", name, memberCount);
    }
}