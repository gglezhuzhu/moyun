package com.moyun.reading.entity.user;

import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生实体类 - 继承普通用户
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    @Size(max = 100, message = "学校名称长度不能超过100个字符")
    @Column(name = "school", length = 100)
    private String school;

    @Size(max = 50, message = "专业名称长度不能超过50个字符")
    @Column(name = "major", length = 50)
    private String major;

    @Size(max = 20, message = "年级长度不能超过20个字符")
    @Column(name = "grade", length = 20)
    private String grade;

    @Size(max = 50, message = "学号长度不能超过50个字符")
    @Column(name = "student_id", length = 50)
    private String studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @Override
    public UserRole getUserRole() {
        return UserRole.STUDENT;
    }

    /**
     * 获取完整的学生信息描述
     */
    public String getStudentInfo() {
        StringBuilder info = new StringBuilder();
        if (school != null)
            info.append(school);
        if (major != null) {
            if (info.length() > 0)
                info.append(" - ");
            info.append(major);
        }
        if (grade != null) {
            if (info.length() > 0)
                info.append(" - ");
            info.append(grade);
        }
        return info.toString();
    }

    /**
     * 是否已选择导师
     */
    public boolean hasTeacher() {
        return teacher != null;
    }

    /**
     * 重写toString方法，避免访问懒加载的teacher对象
     */
    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", realName='" + getRealName() + '\'' +
                ", school='" + school + '\'' +
                ", major='" + major + '\'' +
                ", grade='" + grade + '\'' +
                ", studentId='" + studentId + '\'' +
                '}';
    }
}