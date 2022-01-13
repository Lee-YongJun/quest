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
public class MemberResponse {

    private Long id; // 회원번호(pk)

    private String username; //아이디

    private String name; //이름

    private String password; // 비밀번호

    private String email; // 이메일

    private String phone; // 전화번호

    private String postCode; // 우편번호

    private String address; // 주소1

    private String detailAddress; // 주소2(상세주소)

    private LocalDate createdAt;

    private LocalDate updatedAt;

}
