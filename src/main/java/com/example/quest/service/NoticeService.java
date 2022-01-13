package com.example.quest.service;

import com.example.quest.model.entity.Notice;
import com.example.quest.model.entity.User;
import com.example.quest.model.network.Pagination;
import com.example.quest.model.network.request.NoticeRequest;
import com.example.quest.model.network.response.MemberResponse;
import com.example.quest.model.network.response.NoticeResponse;
import com.example.quest.model.network.Header;

import com.example.quest.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService extends BaseService<NoticeRequest, NoticeResponse, Notice> {
    @Autowired
    NoticeRepository noticeRepository;

    //추가
    @Override
    public Header<NoticeResponse> create(Header<NoticeRequest> request) {
        NoticeRequest noticeRequest = request.getData();
        Notice notice = Notice.builder()
                .title(noticeRequest.getTitle())
                .content(noticeRequest.getContent())
                .writer(noticeRequest.getWriter())
                .build();

        Notice newNotice = baseRepo.save(notice);
        return Header.OK(response(newNotice));
    }

    @Override
    public Header<NoticeResponse> update(Header<NoticeRequest> request) {
        return null;
    }

    @Override
    public Header delete(Long id) {
        return null;
    }

    @Override
    public Header<NoticeResponse> selectRead(Long id) {
        return null;
    }

    //전체조회
    @Override
    public Header<List<NoticeResponse>> allRead() {
        List<Notice> noticeList = baseRepo.findAll();

        List<NoticeResponse> resNoticeList = noticeList.stream()
                .map(notice -> response(notice))
                .sorted((a, b) -> (int) (b.id - a.id))
                .collect(Collectors.toList());

        return Header.OK(resNoticeList);
    }

    //페이징 조회
    @Override
    public Header<List<NoticeResponse>> pagingRead(Pageable pageable) {
        Page<Notice> page = baseRepo.findAll(pageable);
        List<NoticeResponse> noticeResList = page.stream()
                .map(notice -> response(notice))
                .collect(Collectors.toList());

        Pagination pagination = Pagination.builder()
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .currentElements(page.getNumberOfElements())
                .build();
        return Header.OK(noticeResList, pagination);
    }

    public NoticeResponse response(Notice notice) {
        NoticeResponse res = NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .writer(notice.getWriter())
                .createdAt(notice.getCreatedAt())
                .build();
        return res;
    }
}
