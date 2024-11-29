package com.sin.sb1101;

import com.sin.sb1101.dto.Notice;
import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.NoticeRepository;
import com.sin.sb1101.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@SpringBootApplication
public class Sb1101Application {
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private SignRepository signRepository;

@Autowired
    private PasswordEncoder passwordEncoder;



    public static void main(String[] args) {
        SpringApplication.run(Sb1101Application.class, args);
    }

//    @PostConstruct
//    public void init() {
//        Notice notice1 = Notice.builder()
//                .category("[공지]")
//                .title("2024년 1분기 업데이트")
//                .date(LocalDateTime.now())
//                .content("대규모 업데이트 기대...")
//                .build();
//        Notice notice2 = Notice.builder()
//                .category("[이벤트]")
//                .title("게임 윈터 이벤트 안내")
//                .date(LocalDateTime.now())
//                .content("겨울속에서 피어나는 하나의 꽃처럼...")
//                .build();
//        Notice notice3 = Notice.builder()
//                .category("[긴급]")
//                .title("서버 점검")
//                .date(LocalDateTime.now())
//                .content("금일 1시부터 13시30분까지 점검이 있겠습니다...")
//                .build();
//
//        noticeRepository.save(notice1);
//        noticeRepository.save(notice2);
//        noticeRepository.save(notice3);
//    }
//    @PostConstruct
//    public void init2() {
//
//        String encodedPassword = passwordEncoder.encode("123");
//
//        Sign sign1 = Sign.builder()
//                .username("군주")
//                .email("admin@admin.com")
//                .password(encodedPassword)   // 암호화된 비밀번호 저장
//                .confirmPassword(encodedPassword)
//                .level(10)
//                .department("천계")
//                .role("admin")
//                .build();
//
//        signRepository.save(sign1);
//    }

}


