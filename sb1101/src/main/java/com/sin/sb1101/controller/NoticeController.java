package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Notice;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.NoticeRepository;
import com.sin.sb1101.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class NoticeController {
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private NoticeRepository noticeRepository;

    @GetMapping("/notices")
    public String getNotices(Model model, HttpSession session) {
        Object userObj = session.getAttribute("user");
        String role = (userObj != null) ? ((Sign) userObj).getRole() : null;

        boolean isAdmin = "admin".equals(role);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("noticeList", noticeService.getAllNotices());

        model.addAttribute("noticeList",noticeService.getAllNotices());
        return "view/notice";  // notices.html로 데이터를 넘깁니다.
    }
    @GetMapping("/notice/read")
    public String readNotice(@RequestParam Long id, Model model) {
        // 데이터베이스에서 id로 Notice 객체 조회
        Notice notice = noticeRepository.findById(id).orElse(null);

        if (notice != null) {
            model.addAttribute("notice", notice);  // 조회한 공지사항을 모델에 추가
        } else {
            // 만약 공지사항을 찾지 못하면, 오류 메시지를 모델에 추가하거나, 다른 페이지로 리다이렉트
            model.addAttribute("error", "공지사항을 찾을 수 없습니다.");
            return "error-page"; // 에러 페이지로 리다이렉트
        }

        return "/view/notice-detail"; // 공지사항 상세 페이지로 이동
    }
    @GetMapping("/notice/create")
    public String createNoticeForm(Model model, HttpSession session) {
        // 로그인된 사용자 정보 확인
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // 로그인 안 되어 있으면 로그인 페이지로 리다이렉트
        }

        // 관리자만 글 작성 가능
        Object user = session.getAttribute("user");
        if (user != null && "admin".equals(((Sign) user).getRole())) {
            model.addAttribute("notice", new Notice());
            return "view/notice-write";  // 글 작성 페이지
        } else {
            return "redirect:/";  // 관리자가 아니면 홈 페이지로 리다이렉트
        }
    }

    // 글 작성 처리
    @PostMapping("/notice/create")
    public String saveNotice(Notice notice, HttpSession session) {
        // 로그인 확인
        if (session.getAttribute("user") == null) {
            return "redirect:/login";  // 로그인 안 되어 있으면 로그인 페이지로 리다이렉트
        }

        // 현재 시간을 설정
        notice.setDate(LocalDateTime.now());

        // 공지사항 저장
        noticeRepository.save(notice);

        return "redirect:/notices";  // 글 작성 후 공지사항 리스트 페이지로 리다이렉트
    }
}


