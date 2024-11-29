package com.sin.sb1101.service;

import com.sin.sb1101.dto.Comment;
import com.sin.sb1101.dto.Fnotice;
import com.sin.sb1101.dto.Notice;
import com.sin.sb1101.repository.CommentRepository;
import com.sin.sb1101.repository.FnoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FnoticeService {

    @Autowired
    FnoticeRepository fnoticeRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;

    public Fnotice savaNotice(String title, String content, String username) {
        Fnotice fnotice = new Fnotice();
        fnotice.setTitle(title);
        fnotice.setContent(content);
        fnotice.setUsername(username);
        fnotice.setCreatedAt(java.time.LocalDateTime.now());
        fnotice.setHitCnt(0);


        return fnoticeRepository.save(fnotice);

    }
    public List<Fnotice> getAllFnotices() {
        return fnoticeRepository.findAll();
    }
    public Fnotice getFnoticeById(Long id) {
        return fnoticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public void addComment(Long fnoticeId, Comment comment) {
        Fnotice fnotice = fnoticeRepository.findById(fnoticeId)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을수 없음"));

        comment.setFnotice(fnotice);

        commentRepository.save(comment);

        fnoticeRepository.save(fnotice);
    }
    public void updatePost(Fnotice fnotice) {
        fnoticeRepository.save(fnotice); // JPA를 이용한 업데이트
    }

}
