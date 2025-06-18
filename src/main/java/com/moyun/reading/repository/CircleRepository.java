package com.moyun.reading.repository;

import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.entity.circle.CircleMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 圈子仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface CircleRepository extends JpaRepository<Circle, Long> {

        /**
         * 查找活跃的圈子
         */
        Page<Circle> findByDeletedFalseAndStatus(Circle.CircleStatus status, Pageable pageable);

        /**
         * 查找所有未删除的圈子（管理员用）
         */
        Page<Circle> findByDeletedFalse(Pageable pageable);

        /**
         * 查找用户创建的圈子
         */
        Page<Circle> findByCreatorIdAndDeletedFalse(Long creatorId, Pageable pageable);

        /**
         * 查找用户创建的圈子（不分页）
         */
        List<Circle> findByCreatorIdAndDeletedFalse(Long creatorId);

        /**
         * 搜索圈子
         */
        @Query("SELECT c FROM Circle c WHERE c.deleted = false " +
                        "AND c.status = :status " +
                        "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Circle> searchCircles(@Param("keyword") String keyword,
                        @Param("status") Circle.CircleStatus status,
                        Pageable pageable);

        /**
         * 查找用户参与的圈子
         */
        @Query("SELECT DISTINCT c FROM Circle c JOIN CircleMember m ON c.id = m.circle.id " +
                        "WHERE c.deleted = false AND c.status = :status " +
                        "AND m.user.id = :userId AND m.deleted = false " +
                        "AND m.status = :memberStatus")
        Page<Circle> findUserCircles(@Param("userId") Long userId,
                        @Param("status") Circle.CircleStatus status,
                        @Param("memberStatus") CircleMember.MemberStatus memberStatus,
                        Pageable pageable);

        /**
         * 统计用户创建的圈子数量
         */
        long countByCreatorIdAndDeletedFalse(Long creatorId);

        /**
         * 检查圈子名称是否存在
         */
        boolean existsByNameAndDeletedFalse(String name);
}