package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Comment;
import com.sin.sb1101.dto.Fnotice;
import com.sin.sb1101.dto.Notice;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.CommentRepository;
import com.sin.sb1101.repository.FnoticeRepository;
import com.sin.sb1101.repository.SignRepository;
import com.sin.sb1101.service.CommentService;
import com.sin.sb1101.service.FnoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class FnoticeController {

    @Autowired
    private FnoticeRepository fnoticeRepository;

    @Autowired
    private SignRepository signRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private FnoticeService fnoticeService;
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/fnotice")
    public String fnoticeList(
            @RequestParam(required = false, defaultValue = "All") String department,
            Model model,
            HttpSession session) {

        List<Fnotice> fnoticeList;

        try {
            if ("All".equals(department)) {
                fnoticeList = fnoticeRepository.findAllByOrderByIdDesc();
            } else {
                fnoticeList = fnoticeRepository.findByDepartment(department);
            }

            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = date.format(formatter);

            model.addAttribute("formattedDate", formattedDate);
            model.addAttribute("fnotice", fnoticeList);
            model.addAttribute("department", department);

        } catch (Exception e) {
            log.error("게시글 목록 조회 실패", e);
            model.addAttribute("error", "게시글 목록을 불러오는데 실패했습니다.");
        }

        return "view/fnoticeList";
    }

    @GetMapping("/fnoticewrite")
    public String fnoticewrite() {
        return "view/fnoticeWrite";
    }


    @PostMapping("/fnInsert")
    public String fnInsert(@ModelAttribute Fnotice fnotice,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        Sign user = (Sign) session.getAttribute("user");

        if (user == null) {
            redirectAttributes.addFlashAttribute("loginMessage", "로그인이 필요합니다. 로그인 후 다시 시도해주세요.");
            return "redirect:/login";
        }

        try {
            // 기본값 설정
            if (fnotice.getDepartment() == null) {
                fnotice.setDepartment("all");
            }

            // 작성자 정보 설정
            fnotice.setUser(user);  // Sign 객체 설정
            fnotice.setUsername(user.getUsername());

            // 초기값 설정
            fnotice.setHitCnt(0);
            fnotice.setCreatedAt(LocalDateTime.now());  // 직접 LocalDateTime 설정

            // 게시글 저장
            fnoticeRepository.save(fnotice);

            return "redirect:fnotice";
        } catch (Exception e) {
            log.error("게시글 저장 실패: ", e);
            redirectAttributes.addFlashAttribute("error", "게시글 저장에 실패했습니다.");
            return "redirect:fnotice";
        }
    }


    @GetMapping("/fnotice/{id}")
    public String fnoticeDetail(@PathVariable("id") Long id, Model model, HttpSession session) {
        Fnotice fnotice = fnoticeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post Id"));
        fnotice.setHitCnt(fnotice.getHitCnt() + 1);
        fnoticeRepository.save(fnotice);

        List<Comment> comments = commentService.getCommentsByFnoticeId(id);

        Sign user = (Sign) session.getAttribute("user");

        if (user != null) {
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
        }
// 세션 값 확인하기 (컨트롤러에서)
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        LocalDateTime createdAt = fnotice.getCreatedAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = createdAt.format(formatter);
        model.addAttribute("formattedDate", formattedDate);

        model.addAttribute("comments", comments);
        model.addAttribute("fnotice", fnotice);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        return "view/fnoticeDetail";
    }


    @GetMapping("/fnotice/delete/{id}")  // URL 패턴 변경
    public String deleteFnotice(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Sign user = (Sign) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }

            Optional<Fnotice> fnoticeOpt = fnoticeRepository.findById(id);
            if (!fnoticeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
                return "redirect:/fnotice";
            }

            Fnotice fnotice = fnoticeOpt.get();

            // 작성자나 관리자만 삭제 가능
            if (user.getUsername().equals(fnotice.getUsername()) || "admin".equals(user.getRole())) {
                // 댓글 먼저 삭제
                commentRepository.deleteAllByFnoticeId(id);
                // 게시글 삭제
                fnoticeRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
            }
        } catch (Exception e) {
            log.error("게시글 삭제 실패: ", e);
            redirectAttributes.addFlashAttribute("error", "게시글 삭제에 실패했습니다.");
        }

        return "redirect:/fnotice";
    }


    // 수정 처리
    @PostMapping("/fnotice/{id}/edit")
    public String updateFnotice(@PathVariable Long id, @ModelAttribute Fnotice fnotice,
                                HttpSession session) {
        Sign user = (Sign) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Fnotice existingFnotice = fnoticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fnotice Id:" + id));

        if (!user.getUsername().equals(existingFnotice.getUsername()) && !"admin".equals(user.getRole())) {
            return "redirect:/fnotice";
        }

        existingFnotice.setTitle(fnotice.getTitle());
        existingFnotice.setContent(fnotice.getContent());
        existingFnotice.setDepartment(fnotice.getDepartment());

        fnoticeRepository.save(existingFnotice);

        return "redirect:/fnotice/" + id;
    }

    @GetMapping("/fnotice/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        Sign user = (Sign) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Fnotice> fnoticeOpt = fnoticeRepository.findById(id);
        if (!fnoticeOpt.isPresent()) {
            return "redirect:/error";
        }

        Fnotice fnotice = fnoticeOpt.get();

        // 작성자나 관리자만 수정 가능
        if (!user.getUsername().equals(fnotice.getUsername()) && !"admin".equals(user.getRole())) {
            return "redirect:/fnotice";
        }

        model.addAttribute("fnotice", fnotice);
        model.addAttribute("formattedDate",
                fnotice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        return "view/fnoticeReWrite";
    }
}
