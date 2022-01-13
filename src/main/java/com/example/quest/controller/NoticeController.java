package com.example.quest.controller;

import com.example.quest.model.entity.Notice;
import com.example.quest.model.network.Header;
import com.example.quest.model.network.request.NoticeRequest;
import com.example.quest.model.network.response.NoticeResponse;

import com.example.quest.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//로그남기기 쉽게
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/quest/notice")
public class NoticeController extends BaseController<NoticeRequest, NoticeResponse, Notice> {

    @Autowired
    NoticeService noticeService;

    //공지사항 조회
    @Override
    //권한별로 접근통제 USER 또는 ADMIN
    @GetMapping("/paging") //http://localhost:8080/quest/notice/paging?page=0
    public Header<List<NoticeResponse>> pagingRead(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {
        log.info("{}", pageable);
        return baseService.pagingRead(pageable);
    }

    @GetMapping("/all")
    public Header<List<NoticeResponse>> allRead() {
        return baseService.allRead();
    }

    //등록
    @PostMapping("/reg")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Header<NoticeResponse> create(@RequestBody NoticeRequest request) {
        Header<NoticeRequest> result = new Header<NoticeRequest>();
        result.setData(request);

        return baseService.create(result);
    }

}

