package com.sin.sb1101.controller;

import com.sin.sb1101.dto.PlayerScore;
import com.sin.sb1101.dto.ScoreRequest;
import com.sin.sb1101.dto.ScoreResponse;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.service.CsvService;
import com.opencsv.exceptions.CsvException;
import com.sin.sb1101.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class GameListController {
    private static final int PAGE_SIZE = 5;

    @Autowired
    private CsvService csvService;
    @Autowired
    private ScoreService scoreService;

    @GetMapping("/gamelist")
    public String gamelist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchText,
            Model model,
            HttpSession session) {

        Sign sign = (Sign) session.getAttribute("user");
        log.info("Current session user in gamelist: {}", sign);

        try {
            Resource resource = new ClassPathResource("static/csv/gamesList5.csv");
            List<String[]> allGames = csvService.readCsvFile(resource.getFile().getAbsolutePath());

            // 헤더 행 제거 (필요한 경우)
            if (!allGames.isEmpty()) {
                allGames = allGames.subList(1, allGames.size());
            }

            // 검색 로직
            List<String[]> filteredGames = filterGames(allGames, searchType, searchText);

            // 페이징 로직
            int totalGames = filteredGames.size();
            int totalPages = (int) Math.ceil((double) totalGames / PAGE_SIZE);
            page = Math.max(1, Math.min(page, totalPages));

            int start = (page - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, totalGames);

            if (start < totalGames) {
                List<String[]> pagedGames = filteredGames.subList(start, end);
                model.addAttribute("games", pagedGames);
            }

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("searchType", searchType);
            model.addAttribute("searchText", searchText);

        } catch (IOException | CsvException e) {
            log.error("CSV 파일 읽기 오류", e);
            model.addAttribute("error", "파일을 읽는 중 오류가 발생했습니다.");
        }

        return "view/gamelist";
    }


    @PostMapping("/api/scores/register")
    @ResponseBody
    public ResponseEntity<?> registerScore(@RequestBody ScoreRequest request, HttpSession session) {
        try {
            Sign sign = (Sign) session.getAttribute("user");
            log.info("Session user object: {}", sign);

            if (sign == null) {
                log.warn("User not found in session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("로그인이 필요합니다.");
            }

            // 이메일 값 확인
            log.info("User email from session: {}", sign.getEmail());

            scoreService.saveScore(request.getScore(), sign.getEmail());
            return ResponseEntity.ok().body("점수가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            log.error("Error registering score", e, e.getMessage());
            return ResponseEntity.badRequest().body("점수 등록에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/api/scores/ranking")
    @ResponseBody
    public ResponseEntity<List<ScoreResponse>> getRanking() {
        try {
            List<PlayerScore> topScores = scoreService.getTopScores(10);
            List<ScoreResponse> response = topScores.stream()
                    .map(score -> new ScoreResponse(
                            score.getSign().getUsername(),
                            score.getScore(),
                            score.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/api/check-session")
    @ResponseBody
    public ResponseEntity<?> checkSession(HttpSession session) {
        Sign sign = (Sign) session.getAttribute("user");
        log.info("Checking session. User: {}", sign);

        if (sign == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("세션이 없습니다.");
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/user/current")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Sign sign = (Sign) session.getAttribute("user");
        if (sign == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(sign);
    }

    @GetMapping("/ranking")
    public String showRanking(Model model) {
        try {
            List<PlayerScore> topScores = scoreService.getTopScores(10);
            List<ScoreResponse> scores = topScores.stream()
                    .map(score -> new ScoreResponse(
                            score.getSign().getUsername(),
                            score.getScore(),
                            score.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            model.addAttribute("scores", scores);
            return "view/ranking";  // ranking.html의 위치에 맞게 경로 조정
        } catch (Exception e) {
            log.error("Error fetching rankings", e);
            model.addAttribute("error", "랭킹을 불러오는데 실패했습니다.");
            return "view/ranking";
        }
    }
    private List<String[]> filterGames(List<String[]> games, String searchType, String searchText) {
        if (searchType == null || searchText == null || searchText.isEmpty()) {
            return games;
        }

        return games.stream()
                .filter(game -> {
                    switch (searchType) {
                        case "title":
                            return game[1].toLowerCase().contains(searchText.toLowerCase());
                        case "genre":
                            return game[2].toLowerCase().contains(searchText.toLowerCase());
                        case "platform":
                            return game[4].toLowerCase().contains(searchText.toLowerCase());
                        case "developer":
                            return game[5].toLowerCase().contains(searchText.toLowerCase());
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }
}