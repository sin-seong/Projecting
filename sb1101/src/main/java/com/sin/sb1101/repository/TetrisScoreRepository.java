package com.sin.sb1101.repository;


import com.sin.sb1101.dto.TetrisScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TetrisScoreRepository extends JpaRepository<TetrisScore, Long> {
    Page<TetrisScore> findAllByOrderByScoreDesc(Pageable pageable);
}