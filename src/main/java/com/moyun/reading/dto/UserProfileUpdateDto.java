package com.moyun.reading.dto;

import com.moyun.reading.entity.base.User;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户个人信息更新DTO
 * 
 * @author Moyun Team
 */
@Data
public class UserProfileUpdateDto {

    private Long id;

    private String username;

    private String email;

    @Size(max = 100, message = "真实姓名长度不能超过100个字符")
    private String realName;

    @Size(max = 20, message = "手机号码长度不能超过20个字符")
    private String phone;

    private LocalDate birthDate;

    private User.Gender gender;

    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    /**
     * 从User实体创建DTO
     */
    public static UserProfileUpdateDto fromUser(User user) {
        UserProfileUpdateDto dto = new UserProfileUpdateDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setBirthDate(user.getBirthDate());
        dto.setGender(user.getGender());
        dto.setBio(user.getBio());
        return dto;
    }

    /**
     * 将DTO数据应用到User实体
     */
    public void applyToUser(User user) {
        user.setRealName(this.realName);
        user.setPhone(this.phone);
        user.setBirthDate(this.birthDate);
        user.setGender(this.gender);
        user.setBio(this.bio);
    }
}