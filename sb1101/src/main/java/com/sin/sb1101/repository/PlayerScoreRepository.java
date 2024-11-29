package com.sin.sb1101.repository;

import com.sin.sb1101.dto.PlayerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerScoreRepository extends JpaRepository<PlayerScore, Long> {
    List<PlayerScore> findTop10ByOrderByScoreDesc();
}