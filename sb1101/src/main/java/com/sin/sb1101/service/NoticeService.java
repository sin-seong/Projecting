package com.sin.sb1101.service;

import com.sin.sb1101.dto.Notice;
import com.sin.sb1101.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository repo;


    public List<Notice> getAllNotices() {
        return repo.findAll();
    }
}

