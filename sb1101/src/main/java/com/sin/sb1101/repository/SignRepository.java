package com.sin.sb1101.repository;

import com.sin.sb1101.dto.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignRepository extends JpaRepository<Sign, Long> {
    Sign findByEmail(String email);
    Optional<Sign> findByUsername(String username);

}
