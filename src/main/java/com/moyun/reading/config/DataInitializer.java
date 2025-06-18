package com.moyun.reading.config;

import com.moyun.reading.control.service.UserService;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.user.Admin;
import com.moyun.reading.entity.user.UserRole;
import com.moyun.reading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时初始化默认数据
 * 
 * @author Moyun Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    /**
     * 初始化默认管理员账户
     */
    private void initializeDefaultAdmin() {
        try {
            // 检查是否已存在admin用户
            if (userService.findByUsername("admin").isPresent()) {
                log.info("默认管理员账户已存在，跳过初始化");
                return;
            }

            // 创建默认管理员
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@moyun.com");
            admin.setRealName("系统管理员");
            admin.setEnabled(true);
            admin.setAccountNonExpired(true);
            admin.setAccountNonLocked(true);
            admin.setCredentialsNonExpired(true);
            admin.setGender(User.Gender.OTHER);

            userRepository.save(admin);
            log.info("成功初始化默认管理员账户: admin/admin");

        } catch (Exception e) {
            log.error("初始化默认管理员账户失败", e);
        }
    }
}