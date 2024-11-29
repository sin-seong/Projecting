package com.sin.sb1101.service;

import com.sin.sb1101.dto.PlayerScore;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.PlayerScoreRepository;
import com.sin.sb1101.repository.SignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreService {

    private final PlayerScoreRepository scoreRepository;
    private final SignRepository signRepository;

    @Transactional
    public void saveScore(int score, String email) {
        // 디버깅을 위한 로그 추가
        log.info("Attempting to save score for email: {}", email);

        Sign sign = signRepository.findByEmail(email);
        log.info("Found user: {}", sign);  // null인지 확인

        if (sign == null) {
            log.error("User not found for email: {}", email);
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + email);
        }

        PlayerScore scoreEntity = new PlayerScore();
        scoreEntity.setSign(sign);
        scoreEntity.setScore(score);
        scoreEntity.setCreatedAt(LocalDateTime.now());

        PlayerScore savedScore = scoreRepository.save(scoreEntity);
        log.info("Score saved successfully. Score ID: {}", savedScore.getId());
    }

    public List<PlayerScore> getTopScores(int limit) {
        return scoreRepository.findTop10ByOrderByScoreDesc();
    }
}