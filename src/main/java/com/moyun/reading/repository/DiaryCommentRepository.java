package com.moyun.reading.repository;

import com.moyun.reading.entity.diary.DiaryComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 日志评论仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {

    /**
     * 查找指定日志的评论
     */
    Page<DiaryComment> findByDiaryIdAndDeletedFalse(Long diaryId, Pageable pageable);

    /**
     * 统计指定日志的评论数量
     */
    long countByDiaryIdAndDeletedFalse(Long diaryId);

    /**
     * 查找指定用户的评论
     */
    Page<DiaryComment> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /**
     * 查找某个评论的子评论
     */
    Page<DiaryComment> findByParentIdAndDeletedFalse(Long parentId, Pageable pageable);

    /**
     * 查找顶级评论
     */
    Page<DiaryComment> findByDiaryIdAndDeletedFalseAndParentIsNull(Long diaryId, Pageable pageable);
}