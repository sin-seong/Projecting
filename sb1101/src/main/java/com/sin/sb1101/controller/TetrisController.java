package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.dto.TetrisScore;

import com.sin.sb1101.repository.SignRepository;
import com.sin.sb1101.repository.TetrisScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Controller
public class TetrisController {

    @Autowired
    private TetrisScoreRepository tetrisScoreRepository;

    @Autowired
    private SignRepository signRepository;

    @GetMapping("/tetris")
    public String tetris() {
        return "games/tetris";
    }

    @GetMapping("/tetris/ranking")
    public String rankings(Model model) {
        Page<TetrisScore> scores = tetrisScoreRepository.findAllByOrderByScoreDesc(
                PageRequest.of(0, 10)
        );
        model.addAttribute("scores", scores.getContent());
        return "games/tetris-ranking";
    }

    @PostMapping("/tetris/save-score")
    @ResponseBody
    public ResponseEntity<?> saveScore(@RequestBody Map<String, Integer> request, HttpSession session) {
        Sign user = (Sign) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            TetrisScore score = new TetrisScore();
            score.setScore(request.get("score"));
            score.setUser(user);
            score.setCreatedAt(LocalDateTime.now());
            tetrisScoreRepository.save(score);

            return ResponseEntity.ok().body("점수가 저장되었습니다.");
        } catch (Exception e) {
            log.error("점수 저장 실패", e);
            return ResponseEntity.badRequest()
                    .body("점수 저장에 실패했습니다: " + e.getMessage());
        }
    }
}