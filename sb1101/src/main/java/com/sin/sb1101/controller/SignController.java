package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.service.SignService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
public class SignController {

    @Autowired
    private SignService signService;



    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("sign", new Sign());
        return "view/signup";  // signup.html 렌더링
    }

    @PostMapping("/signup")  // '/signup' 경로에 대한 POST 요청을 처리
    public String handleSignup(@Valid @ModelAttribute Sign sign, Model model, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "view/signup";
        }

        try {
            signService.register(sign);
            return "redirect:login";  // 회원가입 후 로그인 페이지로 리디렉션
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "view/signup";  // 오류 발생 시 다시 폼으로 돌아감
        }

    }
}
