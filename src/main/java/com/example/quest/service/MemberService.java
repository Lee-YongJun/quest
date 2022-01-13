package com.example.quest.service;

import com.example.quest.model.entity.User;
import com.example.quest.model.network.Header;
import com.example.quest.model.network.Pagination;
import com.example.quest.model.network.request.MemberRequest;
import com.example.quest.model.network.response.MemberResponse;
import com.example.quest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService extends BaseService<MemberRequest, MemberResponse, User> {

    @Autowired
    UserRepository userRepository;

    @Override
    public Header<MemberResponse> selectRead(Long id) {
        return null;
    }

    @Override
    public Header<List<MemberResponse>> allRead() {
        return null;
    }

    @Override
    public Header<MemberResponse> create(Header<MemberRequest> request) {
        return null;
    }

    @Override
    public Header<List<MemberResponse>> pagingRead(Pageable pageable) {
        Page<User> page = baseRepo.findAll(pageable);

        List<MemberResponse> memberResList = page.stream()
                .map(user -> response(user))
                .collect(Collectors.toList());
        Pagination pagination = Pagination.builder()
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .currentElements(page.getNumberOfElements())
                .build();
        return Header.OK(memberResList, pagination);
    }

    //회원수정
    @Override
    public Header<MemberResponse> update(Header<MemberRequest> request) {
        MemberRequest memberRequest = request.getData();
        Optional<User> optional = userRepository.findByUsername(memberRequest.getUsername());
        return optional
                .map(user -> {
                    user.setUsername(memberRequest.getUsername());
                    user.setPassword(memberRequest.getPassword());
                    user.setName(memberRequest.getName());
                    user.setEmail(memberRequest.getEmail());
                    user.setPhone(memberRequest.getPhone());
                    user.setPostCode(memberRequest.getPostCode());
                    user.setAddress(memberRequest.getAddress());
                    user.setDetailAddress(memberRequest.getDetailAddress());
                    user.setUpdatedAt(LocalDate.now());

                    return user;
                })
                .map(user -> userRepository.save(user))
                .map(user -> response(user))
                .map(user -> Header.OK(user))
                .orElseGet(() -> Header.ERROR("업데이트할 데이터가 없습니다."));
    }

    @Override
    public Header delete(Long id) {
        Optional<User> optional = baseRepo.findById(id);

        return optional
                .map(user -> {
                    baseRepo.delete(user);
                    return Header.OK();
                })
                .orElseGet(() -> Header.ERROR("삭제할 데이터가 없습니다."));
    }

    public MemberResponse response(User user) {
        MemberResponse res = MemberResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .password(user.getPassword())
                .email(user.getEmail())
                .phone(user.getPhone())
                .postCode(user.getPostCode())
                .address(user.getAddress())
                .detailAddress(user.getDetailAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        return res;
    }
}
