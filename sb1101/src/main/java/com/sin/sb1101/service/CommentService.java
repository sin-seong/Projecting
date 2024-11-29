package com.sin.sb1101.service;

import com.sin.sb1101.dto.Comment;
import com.sin.sb1101.dto.Fnotice;
import com.sin.sb1101.repository.CommentRepository;
import com.sin.sb1101.repository.FnoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private FnoticeRepository fnoticeRepository;


    public Comment addComment(String content, Fnotice fnotice, String username) {
        Comment comment = new Comment();
        comment.setFnotice(fnotice);  // fnoticeId로 Fnotice 객체 찾아서 세팅
        comment.setUsername(username);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }


    public List<Comment> getCommentsByFnoticeId(Long fnoticeId) {
        return commentRepository.findByFnoticeId(fnoticeId);
    }

    // 댓글 삭제 메서드 (선택사항)
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

}

