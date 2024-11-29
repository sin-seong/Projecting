package com.sin.sb1101.controller;

import com.sin.sb1101.dto.ScoreDTO;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.service.ScoreService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RpgController {
    @Autowired
    private ScoreService scoreService;

    @PostMapping("/rpg/save-score")
    @ResponseBody
    public String saveScore(@RequestBody ScoreDTO scoreDTO, @RequestParam String userId) {
        if (userId == null) {
            return "로그인이 필요합니다.";
        }

        // 점수 저장 로직
        scoreService.saveScore(scoreDTO.getScore(), "RPG");
        return "점수가 저장되었습니다.";
    }

    @GetMapping("/game3")
    public String game3(Model model) {
        model.addAttribute("score", new ScoreDTO());
        return "/games/rpg";
    }
}
