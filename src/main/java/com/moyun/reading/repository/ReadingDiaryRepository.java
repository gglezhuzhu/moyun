package com.moyun.reading.repository;

import com.moyun.reading.entity.diary.ReadingDiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 读书日志仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface ReadingDiaryRepository extends JpaRepository<ReadingDiary, Long> {

        /**
         * 查找公开的日志
         */
        Page<ReadingDiary> findByDeletedFalseAndPrivacy(ReadingDiary.DiaryPrivacy privacy, Pageable pageable);

        /**
         * 查找某个用户的日志
         */
        Page<ReadingDiary> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

        /**
         * 查找某本书的相关日志
         */
        Page<ReadingDiary> findByBookIdAndDeletedFalseAndPrivacy(
                        Long bookId,
                        ReadingDiary.DiaryPrivacy privacy,
                        Pageable pageable);

        /**
         * 查找指定心情的日志
         */
        Page<ReadingDiary> findByDeletedFalseAndMoodAndPrivacy(
                        ReadingDiary.ReadingMood mood,
                        ReadingDiary.DiaryPrivacy privacy,
                        Pageable pageable);

        /**
         * 搜索日志（按标题和内容）
         */
        @Query("SELECT d FROM ReadingDiary d WHERE d.deleted = false " +
                        "AND d.privacy = :privacy " +
                        "AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<ReadingDiary> searchDiaries(
                        @Param("keyword") String keyword,
                        @Param("privacy") ReadingDiary.DiaryPrivacy privacy,
                        Pageable pageable);

        /**
         * 获取热门日志（按浏览数排序）
         */
        @Query("SELECT d FROM ReadingDiary d WHERE d.deleted = false " +
                        "AND d.privacy = :privacy " +
                        "ORDER BY d.viewCount DESC")
        Page<ReadingDiary> findPopularDiaries(@Param("privacy") ReadingDiary.DiaryPrivacy privacy, Pageable pageable);

        /**
         * 获取某用户的某本书的日志
         */
        Page<ReadingDiary> findByUserIdAndBookIdAndDeletedFalse(Long userId, Long bookId, Pageable pageable);
}