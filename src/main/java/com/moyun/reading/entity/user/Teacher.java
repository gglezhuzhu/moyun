package com.moyun.reading.entity.user;

import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 导师实体类 - 继承普通用户
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("TEACHER")
public class Teacher extends User {

    @Size(max = 100, message = "职位名称长度不能超过100个字符")
    @Column(name = "title", length = 100)
    private String title;

    @Size(max = 100, message = "工作单位长度不能超过100个字符")
    @Column(name = "organization", length = 100)
    private String organization;

    @Size(max = 200, message = "专业领域长度不能超过200个字符")
    @Column(name = "specialization", length = 200)
    private String specialization;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Size(max = 1000, message = "教学经历长度不能超过1000个字符")
    @Column(name = "teaching_experience", length = 1000)
    private String teachingExperience;

    @Column(name = "accepting_students")
    private Boolean acceptingStudents = true;

    @Column(name = "max_students")
    private Integer maxStudents;

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Student> students;

    /**
     * 临时字段：申请状态（不持久化到数据库）
     * 用于在页面显示学生对该导师的申请状态
     */
    @Transient
    private TeacherStudentApplication applicationStatus;

    @Override
    public UserRole getUserRole() {
        return UserRole.TEACHER;
    }

    /**
     * 获取当前学生数量
     */
    public int getCurrentStudentCount() {
        return students != null ? students.size() : 0;
    }

    /**
     * 是否可以接收新学生
     */
    public boolean canAcceptNewStudent() {
        // 如果acceptingStudents为null或false，则不接收新学生
        if (acceptingStudents == null || !acceptingStudents) {
            return false;
        }
        if (maxStudents == null) {
            return true;
        }
        return getCurrentStudentCount() < maxStudents;
    }

    /**
     * 获取导师信息描述
     */
    public String getTeacherInfo() {
        StringBuilder info = new StringBuilder();
        if (title != null)
            info.append(title);
        if (organization != null) {
            if (info.length() > 0)
                info.append(" - ");
            info.append(organization);
        }
        if (specialization != null) {
            if (info.length() > 0)
                info.append(" | ");
            info.append("专业领域: " + specialization);
        }
        return info.toString();
    }

    /**
     * 重写toString方法，避免访问懒加载的students集合
     */
    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", realName='" + getRealName() + '\'' +
                ", title='" + title + '\'' +
                ", organization='" + organization + '\'' +
                ", specialization='" + specialization + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                ", acceptingStudents=" + acceptingStudents +
                ", maxStudents=" + maxStudents +
                '}';
    }
}