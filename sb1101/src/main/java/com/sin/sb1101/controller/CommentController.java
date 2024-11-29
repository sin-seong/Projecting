package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Comment;
import com.sin.sb1101.dto.Fnotice;
import com.sin.sb1101.dto.Notification;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.CommentRepository;
import com.sin.sb1101.repository.FnoticeRepository;
import com.sin.sb1101.repository.NotificationRepository;
import com.sin.sb1101.service.CommentService;
import com.sin.sb1101.service.FnoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private FnoticeService fnoticeService;
    @Autowired
    private FnoticeRepository fnoticeRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CommentRepository commentRepository;


    // 댓글 추가
    @PostMapping("/fnotice/{id}")
    public String addComment(@PathVariable("id") Long id,
                             @RequestParam("content") String content,
                             HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        try {
            Fnotice fnotice = fnoticeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
            Comment comment = commentService.addComment(content, fnotice, username);

            // 게시글 작성자에게 알림 생성 (자신의 댓글은 제외)
            if (!username.equals(fnotice.getUsername())) {
                Notification notification = new Notification();
                notification.setUser(fnotice.getUser());
                notification.setMessage(username + "님이 회원님의 게시글에 댓글을 남겼습니다: "
                        + content.substring(0, Math.min(content.length(), 20))
                        + (content.length() > 20 ? "..." : ""));
                notification.setLink("/fnotice/" + id);
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }

            return "redirect:/fnotice/" + id;
        } catch (Exception e) {
            log.error("댓글 추가 실패: ", e);
            return "redirect:/error";
        }
    }

    //    @PostMapping("/fnotice/{id}/comment")
//    public String addComment(@PathVariable Long id, @ModelAttribute Comment comment,
//                             HttpSession session) {
//        Sign user = (Sign) session.getAttribute("user");
//        if(user == null) {
//            return "redirect:/login";
//        }
//        Fnotice fnotice = fnoticeRepository.findById(id).orElseThrow();
//
//
//    }
    // 댓글 삭제
    @PostMapping("/fnotice/{fnoticeId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long fnoticeId,
                                @PathVariable Long commentId,
                                HttpSession session) {
        Sign user = (Sign) session.getAttribute("user");
        String username = user != null ? user.getUsername() : null;
        String role = user != null ? user.getRole() : null;

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 권한 체크
        if (username != null &&
                (username.equals(comment.getUsername()) || "admin".equals(role))) {
            commentRepository.delete(comment);

            // 게시글 작성자에게 알림 생성
            if (!username.equals(comment.getFnotice().getUsername())) {
                Notification notification = new Notification();
                notification.setUser(comment.getFnotice().getUser());
                notification.setMessage(username + "님이 댓글을 삭제했습니다.");
                notification.setLink("/fnotice/" + fnoticeId);
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        }

        return "redirect:/fnotice/" + fnoticeId;
    }
}