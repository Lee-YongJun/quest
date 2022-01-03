package com.example.quest.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//웹 페이지의 제한된 자원을 외부 도메인에서 접근을 허용하도록 하는 어노테이션
@CrossOrigin(origins = "*", maxAge = 3600)
//Restuful 웹서비스의 컨트롤러 Response Body생성
@RestController
//들어온 요청을 특정 메서드와 매핑하기 위해 사용
@RequestMapping("/quest/test")
public class TestController {

    //전체
    @GetMapping("/all")
    public String allAccess() {
        return "전체 컨텐츠";
    }

    //일반유저
    @GetMapping("/user")
    //권한별로 접근통제 USER 또는 ADMIN
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String userAccess() {
        return "유저 컨텐츠";
    }

    //관리자
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "관리자 컨텐츠";
    }
}
