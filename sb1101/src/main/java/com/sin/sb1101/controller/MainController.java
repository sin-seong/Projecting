package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Fnotice;
import com.sin.sb1101.dto.Notice;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.service.FnoticeService;
import com.sin.sb1101.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller

public class MainController {
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private FnoticeService fnoticeService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        List<Notice> noticeList =noticeService.getAllNotices();
        List<Fnotice> fnoticeList = fnoticeService.getAllFnotices();
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("fnoticeList", fnoticeList);
        Sign sign = (Sign) session.getAttribute("user");  // 세션에서 user 정보 가져오기
        if (sign != null) {
            model.addAttribute("user", sign);  // 세션에 사용자 정보가 있으면 모델에 담기
            model.addAttribute("role", sign.getRole());
        }
        return "/main";
    }
 @GetMapping("/main")
    public String main(Model model) {


        return "redirect:/";
 }
 @GetMapping("/games")
    public String games() {
        return "/view/games";
 }
 @GetMapping("/news")
    public String news() {
        return "/view/news";
 }
 @GetMapping("/about")
    public String about() {
        return "/view/about";
 }
 @GetMapping("login")
    public String login() {
        return "view/login";
 }

    @GetMapping("/game2")
    public String flatform(HttpSession session, Model model) {
        // 세션에서 로그인된 사용자 정보 가져오기
        Sign sign = (Sign) session.getAttribute("user");

        // 사용자 정보가 있을 경우
        if (sign != null) {
            model.addAttribute("username", sign.getUsername());  // 모델에 사용자 정보 추가
        }

        return "/games/formPlatmer";
    }

 @GetMapping("/adminCenter")
    public String adminCenter() {
        return "/view/adminCenter";
 }

 @GetMapping("/apigame")
    public String apigame() {
        return "/api/apiData";
 }

}
