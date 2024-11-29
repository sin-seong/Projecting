package com.sin.sb1101.dto;

import com.sin.sb1101.dto.Sign;
import lombok.*;
import org.checkerframework.checker.units.qual.A;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Sign user;  // 알림을 받을 사용자

    @Column(nullable = false)
    private String message;  // 알림 메시지

    @Column(nullable = false)
    private String link;  // 알림 클릭시 이동할 링크

    @Column(nullable = false)
    private boolean isRead = false;  // 읽음 여부

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}