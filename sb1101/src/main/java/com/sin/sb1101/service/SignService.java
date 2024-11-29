package com.sin.sb1101.service;

import com.sin.sb1101.dto.Sign;

import com.sin.sb1101.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class SignService {

    @Autowired
    private SignRepository signRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Transactional
    public void register(Sign sign) {
        // 이메일 중복 체크
        Sign existingMember = signRepository.findByEmail(sign.getEmail());
        if (existingMember != null) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        String encryptedPassword = passwordEncoder.encode(sign.getPassword());
        sign.setPassword(encryptedPassword);

//
//        UserInfo userInfo = new UserInfo();
//        userInfo.setEmail(sign.getEmail());
//        userInfo.setPassword(sign.getPassword()); // 비밀번호 그대로 저장 (암호화하지 않음)
//        userInfo.setUsername(sign.getUsername());
//        userInfo.setLevel(sign.getLevel());

        // 회원 저장
        signRepository.save(sign);

    }
    public List<Sign> getSignList() {
        return signRepository.findAll();
    }
}
