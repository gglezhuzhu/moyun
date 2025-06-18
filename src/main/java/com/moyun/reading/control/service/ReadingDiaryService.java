package com.moyun.reading.control.service;

import com.moyun.reading.entity.diary.ReadingDiary;
import com.moyun.reading.entity.diary.DiaryComment;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.repository.ReadingDiaryRepository;
import com.moyun.reading.repository.DiaryCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读书日志服务类 - 控制类
 * 
 * @author Moyun Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReadingDiaryService {

    private final ReadingDiaryRepository readingDiaryRepository;
    private final DiaryCommentRepository diaryCommentRepository;

    /**
     * 获取最新公开日志
     */
    @Transactional(readOnly = true)
    public Page<ReadingDiary> findLatestPublicDiaries(int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return readingDiaryRepository.findByDeletedFalseAndPrivacy(ReadingDiary.DiaryPrivacy.PUBLIC, pageable);
    }

    /**
     * 获取所有公开日志（分页）
     */
    @Transactional(readOnly = true)
    public Page<ReadingDiary> findAllPublicDiaries(Pageable pageable) {
        return readingDiaryRepository.findByDeletedFalseAndPrivacy(ReadingDiary.DiaryPrivacy.PUBLIC, pageable);
    }

    /**
     * 获取热门日志
     */
    @Transactional(readOnly = true)
    public Page<ReadingDiary> findPopularDiaries(Pageable pageable) {
        return readingDiaryRepository.findPopularDiaries(ReadingDiary.DiaryPrivacy.PUBLIC, pageable);
    }

    /**
     * 搜索公开日志
     */
    @Transactional(readOnly = true)
    public Page<ReadingDiary> searchPublicDiaries(String keyword, Pageable pageable) {
        return readingDiaryRepository.searchDiaries(keyword, ReadingDiary.DiaryPrivacy.PUBLIC, pageable);
    }

    /**
     * 获取某本书的相关日志
     */
    @Transactional(readOnly = true)
    public Page<ReadingDiary> findBookDiaries(Long bookId, Pageable pageable) {
        return readingDiaryRepository.findByBookIdAndDeletedFalseAndPrivacy(
                bookId, ReadingDiary.DiaryPrivacy.PUBLIC, pageable);
    }

    /**
     * 通过ID查找日志
     */
    @Transactional(readOnly = true)
    public ReadingDiary findById(Long id) {
        return readingDiaryRepository.findById(id).orElse(null);
    }

    /**
     * 创建新日志
     */
    public ReadingDiary createDiary(ReadingDiary diary) {
        log.info("创建新日志: {}", diary.getTitle());
        return readingDiaryRepository.save(diary);
    }

    /**
     * 更新日志
     */
    public ReadingDiary updateDiary(ReadingDiary diary) {
        log.info("更新日志: {}", diary.getId());
        return readingDiaryRepository.save(diary);
    }

    /**
     * 删除日志（软删除）
     */
    public void deleteDiary(Long id, User currentUser) {
        ReadingDiary diary = findById(id);
        if (diary != null && canDeleteDiary(diary, currentUser)) {
            diary.delete();
            readingDiaryRepository.save(diary);
            log.info("删除日志: {}", id);
        }
    }

    /**
     * 查看日志（增加浏览次数）
     */
    public ReadingDiary viewDiary(Long id) {
        ReadingDiary diary = findById(id);
        if (diary != null) {
            diary.incrementViewCount();
            readingDiaryRepository.save(diary);
        }
        return diary;
    }

    /**
     * 获取用户的日志
     */
    @Transactional(readOnly = true)
    public Page<ReadingDiary> findUserDiaries(Long userId, Pageable pageable) {
        return readingDiaryRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    /**
     * 获取用户某本书的日志
     */
    @Transactional(readOnly = true)
    public Page<ReadingDiary> findUserBookDiaries(Long userId, Long bookId, Pageable pageable) {
        return readingDiaryRepository.findByUserIdAndBookIdAndDeletedFalse(userId, bookId, pageable);
    }

    /**
     * 获取日志的评论
     */
    @Transactional(readOnly = true)
    public Page<DiaryComment> findDiaryComments(Long diaryId, Pageable pageable) {
        Page<DiaryComment> comments = diaryCommentRepository.findByDiaryIdAndDeletedFalse(diaryId, pageable);
        log.info("查询日志 {} 的评论，总数: {}, 当前页: {}, 每页大小: {}, 当前页评论数: {}",
                diaryId, comments.getTotalElements(), comments.getNumber(), comments.getSize(),
                comments.getNumberOfElements());
        return comments;
    }

    /**
     * 获取日志的评论数量
     */
    @Transactional(readOnly = true)
    public long getDiaryCommentCount(Long diaryId) {
        long count = diaryCommentRepository.countByDiaryIdAndDeletedFalse(diaryId);
        log.info("日志 {} 的评论总数: {}", diaryId, count);
        return count;
    }

    /**
     * 创建评论
     */
    public DiaryComment createComment(DiaryComment comment) {
        log.info("开始创建日志评论: 日志ID={}, 用户ID={}, 内容={}",
                comment.getDiary().getId(), comment.getUser().getId(), comment.getContent());

        // 确保评论对象没有ID（避免更新现有记录）
        comment.setId(null);

        DiaryComment savedComment = diaryCommentRepository.save(comment);
        log.info("评论创建成功: 评论ID={}, 内容={}", savedComment.getId(), savedComment.getContent());

        // 验证评论是否真的被保存
        long totalComments = diaryCommentRepository.countByDiaryIdAndDeletedFalse(comment.getDiary().getId());
        log.info("日志 {} 现在总共有 {} 条评论", comment.getDiary().getId(), totalComments);

        return savedComment;
    }

    /**
     * 删除评论
     */
    public void deleteComment(Long commentId, User currentUser) {
        DiaryComment comment = diaryCommentRepository.findById(commentId).orElse(null);
        if (comment != null && canDeleteComment(comment, currentUser)) {
            comment.delete();
            diaryCommentRepository.save(comment);
            log.info("删除日志评论: {}", commentId);
        }
    }

    /**
     * 检查是否可以删除日志
     */
    private boolean canDeleteDiary(ReadingDiary diary, User currentUser) {
        // 用户可以删除自己的日志，管理员可以删除任何日志
        return diary.getUser().getId().equals(currentUser.getId()) ||
                isAdmin(currentUser);
    }

    /**
     * 检查是否可以删除评论
     */
    private boolean canDeleteComment(DiaryComment comment, User currentUser) {
        // 用户可以删除自己的评论，管理员可以删除任何评论
        return comment.getUser().getId().equals(currentUser.getId()) ||
                isAdmin(currentUser);
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getUserRole().name());
    }

    /**
     * 检查日志访问权限
     */
    public boolean canAccessDiary(ReadingDiary diary, User currentUser) {
        if (diary == null) {
            return false;
        }

        // 公开日志任何人都可以访问
        if (diary.isPublic()) {
            return true;
        }

        // 私密日志只有作者可以访问
        if (diary.isPrivate()) {
            return currentUser != null && diary.getUser().getId().equals(currentUser.getId());
        }

        // 仅好友可见的日志（这里简化处理，后续可以添加好友关系检查）
        return currentUser != null &&
                (diary.getUser().getId().equals(currentUser.getId()) || isAdmin(currentUser));
    }
}