package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Notification;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpSession;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    @Autowired
    private NotificationRepository notificationRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        try {
            Sign user = (Sign) session.getAttribute("user");
            if (user != null) {
                log.info("현재 로그인한 사용자: {}", user.getUsername());

                List<Notification> notifications =
                        notificationRepository.findTop5ByUserAndIsReadOrderByCreatedAtDesc(user, false);
                int unreadCount = notificationRepository.countByUserAndIsRead(user, false);

                log.info("알림 개수: {}, 알림 목록: {}", unreadCount, notifications);

                model.addAttribute("notifications", notifications);
                model.addAttribute("unreadCount", unreadCount);
            }
        } catch (Exception e) {
            log.error("알림 데이터 로딩 중 오류", e);
        }
    }
}