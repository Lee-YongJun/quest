package com.example.quest.model.network.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberRequest {

    private Long id; // 회원번호(pk)

    private String username; //아이디

    private String name; //이름

    private String password;//패스워드

    private String email; // 이메일

    private String phone; // 전화번호

    private String postCode; // 우편번호

    private String address; // 주소1

    private String detailAddress; // 주소2(상세주소)
}
