package com.sin.sb1101.repository;

import com.sin.sb1101.dto.Fnotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FnoticeRepository extends JpaRepository<Fnotice, Long> {
    List<Fnotice> findByDepartment(String department);
    List<Fnotice> findAllByOrderByIdDesc();  // 최신글 순으로 정렬
}