package com.moyun.reading.control.service;

import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.entity.circle.CircleMember;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.user.UserRole;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.repository.CircleRepository;
import com.moyun.reading.repository.CircleMemberRepository;
import com.moyun.reading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 圈子服务类 - 控制类
 * 
 * @author Moyun Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CircleService {

    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserRepository userRepository;

    /**
     * 创建圈子（只有导师和管理员可以创建）
     */
    public Circle createCircle(Circle circle, User creator) {
        if (!canCreateCircle(creator)) {
            throw new IllegalArgumentException("只有导师和管理员可以创建圈子");
        }

        if (circleRepository.existsByNameAndDeletedFalse(circle.getName())) {
            throw new IllegalArgumentException("圈子名称已存在");
        }

        circle.setCreator(creator);
        circle.setStatus(Circle.CircleStatus.ACTIVE);
        circle.setMemberCount(1); // 创建者自动成为成员

        Circle savedCircle = circleRepository.save(circle);

        // 创建者自动成为圈子成员
        CircleMember creatorMember = new CircleMember();
        creatorMember.setCircle(savedCircle);
        creatorMember.setUser(creator);
        creatorMember.setRole(CircleMember.MemberRole.CREATOR);
        creatorMember.setStatus(CircleMember.MemberStatus.ACTIVE);
        circleMemberRepository.save(creatorMember);

        log.info("用户 {} 创建了圈子: {}", creator.getUsername(), circle.getName());
        return savedCircle;
    }

    /**
     * 邀请用户加入圈子
     */
    public void inviteUserToCircle(Long circleId, Long userId, User inviter) {
        Circle circle = findById(circleId);
        if (circle == null) {
            throw new IllegalArgumentException("圈子不存在");
        }

        if (!canManageCircle(circle, inviter)) {
            throw new IllegalArgumentException("您没有权限邀请用户加入此圈子");
        }

        if (!circle.canAddNewMember()) {
            throw new IllegalArgumentException("圈子已满或不活跃，无法添加新成员");
        }

        // 检查用户是否已经是活跃成员
        if (circleMemberRepository.existsByCircleIdAndUserIdAndDeletedFalseAndStatus(
                circleId, userId, CircleMember.MemberStatus.ACTIVE)) {
            throw new IllegalArgumentException("用户已经是圈子成员");
        }

        // 检查是否存在非活跃的成员记录（可能是被禁用或其他状态）
        Optional<CircleMember> existingMember = circleMemberRepository
                .findByCircleIdAndUserIdAndDeletedFalse(circleId, userId);
        if (existingMember.isPresent()) {
            // 如果用户之前是成员但状态不是ACTIVE，重新激活
            CircleMember member = existingMember.get();
            member.setStatus(CircleMember.MemberStatus.ACTIVE);
            member.setInvitedBy(inviter);
            circleMemberRepository.save(member);

            // 更新圈子成员数量
            circle.incrementMemberCount();
            circleRepository.save(circle);

            log.info("用户 {} 重新激活为圈子 {} 的成员", userId, circle.getName());
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 检查是否存在已删除的成员记录
        Optional<CircleMember> deletedMemberOpt = circleMemberRepository.findByCircleIdAndUserId(circleId, userId);
        if (deletedMemberOpt.isPresent()) {
            CircleMember deletedMember = deletedMemberOpt.get();
            if (deletedMember.isDeleted()) {
                // 恢复已删除的记录
                deletedMember.restore();
                deletedMember.setStatus(CircleMember.MemberStatus.ACTIVE);
                deletedMember.setRole(CircleMember.MemberRole.MEMBER);
                deletedMember.setInvitedBy(inviter);
                circleMemberRepository.save(deletedMember);

                // 更新圈子成员数量
                circle.incrementMemberCount();
                circleRepository.save(circle);

                log.info("用户 {} 的成员记录已恢复并重新加入圈子: {}", userId, circle.getName());
                return;
            }
        }

        // 创建新的成员记录
        CircleMember member = new CircleMember();
        member.setCircle(circle);
        member.setUser(user);
        member.setRole(CircleMember.MemberRole.MEMBER);
        member.setStatus(CircleMember.MemberStatus.ACTIVE);
        member.setInvitedBy(inviter);

        circleMemberRepository.save(member);

        // 更新圈子成员数量
        circle.incrementMemberCount();
        circleRepository.save(circle);

        log.info("用户 {} 被邀请加入圈子: {}", userId, circle.getName());
    }

    /**
     * 移除圈子成员
     */
    public void removeMemberFromCircle(Long circleId, Long userId, User remover) {
        Circle circle = findById(circleId);
        if (circle == null) {
            throw new IllegalArgumentException("圈子不存在");
        }

        if (!canManageCircle(circle, remover)) {
            throw new IllegalArgumentException("您没有权限移除此圈子的成员");
        }

        CircleMember member = circleMemberRepository.findByCircleIdAndUserIdAndDeletedFalse(circleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不是圈子成员"));

        if (member.isCreator()) {
            throw new IllegalArgumentException("不能移除圈子创建者");
        }

        member.delete();
        circleMemberRepository.save(member);

        // 更新圈子成员数量
        circle.decrementMemberCount();
        circleRepository.save(circle);

        log.info("用户 {} 被移除出圈子: {}", userId, circle.getName());
    }

    /**
     * 获取所有活跃圈子
     */
    @Transactional(readOnly = true)
    public Page<Circle> findAllActiveCircles(Pageable pageable) {
        return circleRepository.findByDeletedFalseAndStatus(Circle.CircleStatus.ACTIVE, pageable);
    }

    /**
     * 搜索圈子
     */
    @Transactional(readOnly = true)
    public Page<Circle> searchCircles(String keyword, Pageable pageable) {
        return circleRepository.searchCircles(keyword, Circle.CircleStatus.ACTIVE, pageable);
    }

    /**
     * 获取用户参与的圈子
     */
    @Transactional(readOnly = true)
    public Page<Circle> findUserCircles(Long userId, Pageable pageable) {
        return circleRepository.findUserCircles(userId, Circle.CircleStatus.ACTIVE,
                CircleMember.MemberStatus.ACTIVE, pageable);
    }

    /**
     * 获取用户创建的圈子
     */
    @Transactional(readOnly = true)
    public Page<Circle> findUserCreatedCircles(Long userId, Pageable pageable) {
        return circleRepository.findByCreatorIdAndDeletedFalse(userId, pageable);
    }

    /**
     * 获取用户创建的圈子（不分页）
     */
    @Transactional(readOnly = true)
    public List<Circle> findUserCreatedCircles(Long userId) {
        return circleRepository.findByCreatorIdAndDeletedFalse(userId);
    }

    /**
     * 通过ID查找圈子
     */
    @Transactional(readOnly = true)
    public Circle findById(Long id) {
        return circleRepository.findById(id).orElse(null);
    }

    /**
     * 获取圈子成员
     */
    @Transactional(readOnly = true)
    public Page<CircleMember> findCircleMembers(Long circleId, Pageable pageable) {
        return circleMemberRepository.findByCircleIdAndDeletedFalseAndStatus(
                circleId, CircleMember.MemberStatus.ACTIVE, pageable);
    }

    /**
     * 检查用户是否可以创建圈子
     */
    private boolean canCreateCircle(User user) {
        UserRole role = user.getUserRole();
        return role == UserRole.TEACHER || role == UserRole.ADMIN;
    }

    /**
     * 检查用户是否可以管理圈子
     */
    public boolean canManageCircle(Circle circle, User user) {
        if (user.getUserRole() == UserRole.ADMIN) {
            return true;
        }

        return circleMemberRepository.findByCircleIdAndUserIdAndDeletedFalse(circle.getId(), user.getId())
                .map(CircleMember::canManageCircle)
                .orElse(false);
    }

    /**
     * 检查用户是否为圈子成员
     */
    @Transactional(readOnly = true)
    public boolean isCircleMember(Long circleId, Long userId) {
        return circleMemberRepository.existsByCircleIdAndUserIdAndDeletedFalseAndStatus(
                circleId, userId, CircleMember.MemberStatus.ACTIVE);
    }

    /**
     * 获取用户在圈子中的成员信息
     */
    @Transactional(readOnly = true)
    public CircleMember getUserMembership(Long circleId, Long userId) {
        return circleMemberRepository.findByCircleIdAndUserIdAndDeletedFalse(circleId, userId)
                .orElse(null);
    }

    /**
     * 更新圈子信息
     */
    public Circle updateCircle(Circle circle, User updater) {
        Circle existingCircle = findById(circle.getId());
        if (existingCircle == null) {
            throw new IllegalArgumentException("圈子不存在");
        }

        if (!canManageCircle(existingCircle, updater)) {
            throw new IllegalArgumentException("您没有权限修改此圈子");
        }

        existingCircle.setName(circle.getName());
        existingCircle.setDescription(circle.getDescription());
        existingCircle.setType(circle.getType());
        existingCircle.setPrivacy(circle.getPrivacy());
        existingCircle.setMaxMembers(circle.getMaxMembers());
        existingCircle.setRules(circle.getRules());

        Circle updatedCircle = circleRepository.save(existingCircle);
        log.info("圈子 {} 信息已更新", existingCircle.getName());
        return updatedCircle;
    }

    /**
     * 获取可邀请的学生列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableStudentsForInvite(Long circleId, Long teacherId) {
        List<Map<String, Object>> availableStudents = new ArrayList<>();

        // 获取导师的学生列表
        List<Student> teacherStudents = getTeacherStudents(teacherId);

        for (Student student : teacherStudents) {
            // 检查学生是否已经是圈子成员
            if (!circleMemberRepository.existsByCircleIdAndUserIdAndDeletedFalseAndStatus(
                    circleId, student.getId(), CircleMember.MemberStatus.ACTIVE)) {

                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", student.getId());
                studentInfo.put("username", student.getUsername());
                studentInfo.put("realName", student.getRealName());
                studentInfo.put("studentInfo", student.getStudentInfo());
                availableStudents.add(studentInfo);
            }
        }

        return availableStudents;
    }

    /**
     * 获取导师的学生列表
     */
    @Transactional(readOnly = true)
    private List<Student> getTeacherStudents(Long teacherId) {
        // 通过UserRepository查询导师的学生
        return userRepository.findStudentsByTeacherId(teacherId);
    }

    /**
     * 删除圈子
     */
    public void deleteCircle(Long circleId, User deleter) {
        Circle circle = findById(circleId);
        if (circle == null) {
            throw new IllegalArgumentException("圈子不存在");
        }

        if (!canManageCircle(circle, deleter)) {
            throw new IllegalArgumentException("您没有权限删除此圈子");
        }

        circle.delete();
        circleRepository.save(circle);

        log.info("圈子 {} 被删除", circle.getName());
    }
}
