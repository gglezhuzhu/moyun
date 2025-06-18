package com.moyun.reading.control.service;

import com.moyun.reading.boundary.dto.UserRegistrationDto;
import com.moyun.reading.dto.UserProfileUpdateDto;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.user.Admin;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import com.moyun.reading.entity.user.UserRole;
import com.moyun.reading.repository.UserRepository;
import com.moyun.reading.utility.security.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类 - 控制类
 * 实现用户管理的核心业务逻辑
 * 
 * @author Moyun Team
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    /**
     * 用户注册
     */
    public User registerUser(UserRegistrationDto registrationDto) {
        // 验证密码一致性
        if (!registrationDto.isPasswordMatching()) {
            throw new IllegalArgumentException("密码和确认密码不一致");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 根据角色创建不同类型的用户
        User user = createUserByRole(registrationDto);

        // 加密密码
        user.setPassword(passwordUtil.encode(registrationDto.getPassword()));

        return userRepository.save(user);
    }

    /**
     * 根据角色创建用户
     */
    private User createUserByRole(UserRegistrationDto dto) {
        User user;

        switch (dto.getUserRole()) {
            case STUDENT:
                Student student = new Student();
                student.setSchool(dto.getSchool());
                student.setMajor(dto.getMajor());
                student.setGrade(dto.getGrade());
                student.setStudentId(dto.getStudentId());
                user = student;
                break;

            case TEACHER:
                Teacher teacher = new Teacher();
                teacher.setTitle(dto.getTitle());
                teacher.setOrganization(dto.getOrganization());
                teacher.setSpecialization(dto.getSpecialization());
                user = teacher;
                break;

            case ADMIN:
                Admin admin = new Admin();
                admin.setTitle(dto.getTitle());
                admin.setOrganization(dto.getOrganization());
                admin.setSpecialization(dto.getSpecialization());
                user = admin;
                break;

            default:
                user = new User();
                break;
        }

        // 设置通用字段
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());

        return user;
    }

    /**
     * 通过用户名查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 通过邮箱查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 通过ID查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 更新用户个人信息（使用DTO）
     */
    public User updateUserProfile(Long userId, UserProfileUpdateDto userDto) {
        log.info("开始更新用户信息: userId={}, realName={}", userId, userDto.getRealName());

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        log.info("找到现有用户: id={}, username={}, realName={}",
                existingUser.getId(), existingUser.getUsername(), existingUser.getRealName());

        // 使用DTO的applyToUser方法更新用户信息
        userDto.applyToUser(existingUser);

        log.info("准备保存用户信息: realName={}", existingUser.getRealName());

        User savedUser = userRepository.save(existingUser);

        log.info("用户信息保存完成: id={}, realName={}", savedUser.getId(), savedUser.getRealName());

        return savedUser;
    }

    /**
     * 更新用户个人信息
     */
    public User updateUserProfile(Long userId, User updatedUser) {
        log.info("开始更新用户信息: userId={}, realName={}", userId, updatedUser.getRealName());

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        log.info("找到现有用户: id={}, username={}, realName={}",
                existingUser.getId(), existingUser.getUsername(), existingUser.getRealName());

        // 更新允许修改的字段
        existingUser.setRealName(updatedUser.getRealName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        existingUser.setGender(updatedUser.getGender());
        existingUser.setBio(updatedUser.getBio());

        log.info("准备保存用户信息: realName={}", existingUser.getRealName());

        User savedUser = userRepository.save(existingUser);

        log.info("用户信息保存完成: id={}, realName={}", savedUser.getId(), savedUser.getRealName());

        return savedUser;
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 验证当前密码
        if (!passwordUtil.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }

        // 设置新密码
        user.setPassword(passwordUtil.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 获取用户的图书列表
     */
    @Transactional(readOnly = true)
    public List<Book> getUserBooks(Long userId) {
        return userRepository.findUserBooks(userId);
    }

    /**
     * 获取所有用户（分页，支持搜索）
     */
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return searchUsers(search.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    /**
     * 搜索用户
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingOrRealNameContaining(
                keyword, keyword, pageable);
    }

    /**
     * 更新用户角色（管理员功能）
     */
    @Transactional
    public void updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 如果角色相同，直接返回
        if (user.getUserRole() == newRole) {
            return;
        }

        try {
            // 使用原生SQL更新用户类型，避免删除再插入
            String userType = getUserTypeByRole(newRole);
            userRepository.updateUserType(userId, userType);

            // 清空可能不适用于新角色的特定字段
            clearRoleSpecificFields(userId, newRole);

        } catch (Exception e) {
            throw new RuntimeException("更新用户角色失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据角色获取用户类型字符串
     */
    private String getUserTypeByRole(UserRole role) {
        switch (role) {
            case STUDENT:
                return "STUDENT";
            case TEACHER:
                return "TEACHER";
            case ADMIN:
                return "ADMIN";
            default:
                return "USER";
        }
    }

    /**
     * 清空角色特定字段
     */
    private void clearRoleSpecificFields(Long userId, UserRole newRole) {
        // 根据新角色，清空不相关的字段
        switch (newRole) {
            case USER:
                // 清空所有特殊字段
                userRepository.clearStudentFields(userId);
                userRepository.clearTeacherFields(userId);
                userRepository.clearAdminFields(userId);
                break;
            case STUDENT:
                // 清空导师和管理员字段
                userRepository.clearTeacherFields(userId);
                userRepository.clearAdminFields(userId);
                break;
            case TEACHER:
                // 清空学生和管理员字段
                userRepository.clearStudentFields(userId);
                userRepository.clearAdminFields(userId);
                break;
            case ADMIN:
                // 清空学生字段，保留导师字段（因为Admin继承Teacher）
                userRepository.clearStudentFields(userId);
                break;
        }
    }

    /**
     * 切换用户启用状态
     */
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }

    /**
     * 禁用用户
     */
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    /**
     * 启用用户
     */
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * 删除用户（软删除）
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.delete();
        userRepository.save(user);
    }

    /**
     * 验证用户登录
     */
    @Transactional(readOnly = true)
    public boolean validateUser(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.isAccountAvailable() &&
                    passwordUtil.matches(password, user.getPassword());
        }
        return false;
    }

    /**
     * 处理忘记密码请求
     */
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("该邮箱未注册"));

        // 生成重置token
        String resetToken = generateResetToken();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(24)); // 24小时有效期
        userRepository.save(user);

        // 这里可以添加发送邮件的逻辑
        // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        log.info("为用户 {} 生成密码重置token: {}", email, resetToken);
    }

    /**
     * 验证重置token
     */
    public void validateResetToken(String token) {
        User user = userRepository.findByResetTokenAndDeletedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("重置链接无效"));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("重置链接已过期");
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetTokenAndDeletedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("重置链接无效"));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("重置链接已过期");
        }

        // 更新密码
        user.setPassword(passwordUtil.encode(newPassword));
        // 清除重置token
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("用户 {} 密码重置成功", user.getEmail());
    }

    /**
     * 验证用户身份用于密码重置（用户名+邮箱验证）
     */
    public User validateUserForPasswordReset(String username, String email) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new IllegalArgumentException("用户名不存在"));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("邮箱地址与用户名不匹配");
        }

        if (!user.isAccountAvailable()) {
            throw new IllegalArgumentException("账户不可用，请联系管理员");
        }

        log.info("用户 {} 身份验证成功，准备重置密码", username);
        return user;
    }

    /**
     * 直接重置密码（不需要token）
     */
    public void resetPasswordDirect(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 更新密码
        user.setPassword(passwordUtil.encode(newPassword));
        userRepository.save(user);

        log.info("用户 {} 密码重置成功", user.getUsername());
    }

    /**
     * 生成重置token
     */
    private String generateResetToken() {
        return java.util.UUID.randomUUID().toString();
    }
}