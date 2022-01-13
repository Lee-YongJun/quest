package com.example.quest.model.network.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeResponse {

    public Long id; //공지사항 번호

    private String title; //공지사항 제목

    private String content; //공지사항 내용

    private String writer; //공지사항 작성자

    private LocalDate createdAt;// 작성날짜

}
