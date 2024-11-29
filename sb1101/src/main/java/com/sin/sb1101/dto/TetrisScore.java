package com.sin.sb1101.dto;

import com.sin.sb1101.dto.Sign;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tetris_scores")
@Getter
@Setter
@NoArgsConstructor
public class TetrisScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer score;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Sign user;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}