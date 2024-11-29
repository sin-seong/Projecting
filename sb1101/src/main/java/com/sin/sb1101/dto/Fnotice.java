package com.sin.sb1101.dto;


import lombok.*;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Fnotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Sign user;  // 게시글 작성자 정보

    private String title;
    @Lob
    private String content;
    private String username;


    private LocalDateTime createdAt;
    private int hitCnt;
    @Column(name = "department", nullable = true)  // 기존 데이터를 위해 nullable = true
    private String department = "all";  // 기본값 설정


    @OneToMany(mappedBy = "fnotice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();  // 게시글에 달린 댓글 목록

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();  // 생성 시 자동으로 현재 시간 설정
        }
    }
}