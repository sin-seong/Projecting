package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Notification;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpSession session) {
        try {
            Sign user = (Sign) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));

            if (notification.getUser().getId().equals(user.getId())) {
                notification.setRead(true);
                notificationRepository.save(notification);

                // 읽지 않은 알림 수 업데이트
                int unreadCount = notificationRepository.countByUserAndIsRead(user, false);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("unreadCount", unreadCount);

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}