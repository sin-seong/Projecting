package com.sin.sb1101.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

        @GetMapping("/error")
        public String handleError(Model model, HttpServletRequest request) {
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            String errorMessage = "An error occurred";

            if (status != null) {
                Integer statusCode = Integer.valueOf(status.toString());

                switch(statusCode) {
                    case 404:
                        errorMessage = "페이지를 찾을 수 없습니다.";
                        break;
                    case 403:
                        errorMessage = "접근이 거부되었습니다.";
                        break;
                    case 500:
                        errorMessage = "서버 내부 오류가 발생했습니다.";
                        break;
                    case 400:
                        errorMessage = "잘못된 요청입니다.";
                        break;
                    default:
                        errorMessage = "오류가 발생했습니다.";
                }
            }

            model.addAttribute("error", errorMessage);
            return "error/error";  // templates/error/error.html을 가리킴
        }
    }
