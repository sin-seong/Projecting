package com.sin.sb1101.repository;

import com.sin.sb1101.dto.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByFnoticeId(Long fnoticeId);
    // 게시글 ID로 관련 댓글들 삭제
    void deleteByFnoticeId(Long fnoticeId);

    // 트랜잭션 처리를 위한 어노테이션 추가
    @Transactional
    @Modifying
    void deleteAllByFnoticeId(Long fnoticeId);
}
