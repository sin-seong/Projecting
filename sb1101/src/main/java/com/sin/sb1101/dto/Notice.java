package com.sin.sb1101.dto;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
@Setter
public class Notice {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;


    private String category;
    private String title;
    private String content;
    private LocalDateTime date;

    // 날짜 포맷팅 메소드
    public String dateFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return this.date.format(formatter);
    }
}
