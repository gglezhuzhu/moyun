package com.moyun.reading.boundary.dto;

import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册数据传输对象 - 边界类
 * 
 * @author Moyun Team
 */
@Data
public class UserRegistrationDto {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为6个字符")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 100, message = "真实姓名长度不能超过100个字符")
    private String realName;

    @Size(max = 20, message = "手机号码长度不能超过20个字符")
    private String phone;

    private User.Gender gender;

    private UserRole userRole = UserRole.USER;

    // 学生专用字段
    @Size(max = 100, message = "学校名称长度不能超过100个字符")
    private String school;

    @Size(max = 50, message = "专业名称长度不能超过50个字符")
    private String major;

    @Size(max = 20, message = "年级长度不能超过20个字符")
    private String grade;

    @Size(max = 50, message = "学号长度不能超过50个字符")
    private String studentId;

    // 导师专用字段
    @Size(max = 100, message = "职位名称长度不能超过100个字符")
    private String title;

    @Size(max = 100, message = "工作单位长度不能超过100个字符")
    private String organization;

    @Size(max = 200, message = "专业领域长度不能超过200个字符")
    private String specialization;

    /**
     * 验证密码是否一致
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}