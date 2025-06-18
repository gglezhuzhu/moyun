package com.moyun.reading.repository;

import com.moyun.reading.entity.circle.CircleMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 圈子成员仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface CircleMemberRepository extends JpaRepository<CircleMember, Long> {

        /**
         * 查找圈子的所有成员
         */
        Page<CircleMember> findByCircleIdAndDeletedFalse(Long circleId, Pageable pageable);

        /**
         * 查找圈子的活跃成员
         */
        Page<CircleMember> findByCircleIdAndDeletedFalseAndStatus(Long circleId,
                        CircleMember.MemberStatus status,
                        Pageable pageable);

        /**
         * 查找用户在圈子中的成员记录
         */
        Optional<CircleMember> findByCircleIdAndUserIdAndDeletedFalse(Long circleId, Long userId);

        /**
         * 查找用户在圈子中的成员记录（包括已删除的记录）
         */
        Optional<CircleMember> findByCircleIdAndUserId(Long circleId, Long userId);

        /**
         * 查找用户参与的所有圈子
         */
        Page<CircleMember> findByUserIdAndDeletedFalseAndStatus(Long userId,
                        CircleMember.MemberStatus status,
                        Pageable pageable);

        /**
         * 检查用户是否为圈子成员
         */
        boolean existsByCircleIdAndUserIdAndDeletedFalseAndStatus(Long circleId, Long userId,
                        CircleMember.MemberStatus status);

        /**
         * 统计圈子的活跃成员数量
         */
        long countByCircleIdAndDeletedFalseAndStatus(Long circleId, CircleMember.MemberStatus status);

        /**
         * 查找圈子的管理员
         */
        @Query("SELECT m FROM CircleMember m WHERE m.circle.id = :circleId " +
                        "AND m.deleted = false AND m.status = :status " +
                        "AND (m.role = :creatorRole OR m.role = :adminRole)")
        List<CircleMember> findCircleAdmins(@Param("circleId") Long circleId,
                        @Param("status") CircleMember.MemberStatus status,
                        @Param("creatorRole") CircleMember.MemberRole creatorRole,
                        @Param("adminRole") CircleMember.MemberRole adminRole);
}