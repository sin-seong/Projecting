package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Sign;

import com.sin.sb1101.repository.SignRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
@Slf4j
@Controller
public class LoginController {

    @Autowired
    private SignRepository signRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        Model model,
                        HttpSession session) {
        Sign sign = signRepository.findByEmail(email);

        if (sign != null && passwordEncoder.matches(password, sign.getPassword())) {
            // 세션에 사용자 정보 저장 시 로깅 추가
            log.info("Setting user in session: {}", sign);
            session.setAttribute("user", sign);
            log.info("Session ID: {}", session.getId());

            // 세션 검증
            Sign sessionUser = (Sign) session.getAttribute("user");
            log.info("Verified session user: {}", sessionUser);

            return "redirect:/";
        } else {
            model.addAttribute("errorMessage", "잘못된 이메일 또는 비밀번호입니다.");
            return "view/login";
        }
    }
    @GetMapping("/userInfo")
    public String userInfo(Model model, HttpSession session) {
        Sign sign = (Sign) session.getAttribute("user");
        if (sign != null) {
            model.addAttribute("user", sign);
            return "view/userInfo";
        }else {
            return "redirect:/login";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 초기화
        return "redirect:/login";  // 로그인 페이지로 리다이렉트
    }

}
