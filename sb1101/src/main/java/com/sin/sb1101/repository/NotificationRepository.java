package com.sin.sb1101.repository;


import com.sin.sb1101.dto.Notification;
import com.sin.sb1101.dto.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(Sign user, boolean isRead);
    int countByUserAndIsRead(Sign user, boolean isRead);

    // 최근 5개의 읽지 않은 알림을 가져오는 메서드
    List<Notification> findTop5ByUserAndIsReadOrderByCreatedAtDesc(Sign user, boolean isRead);
}