package com.example.quest.payload.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;
@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    private Set<String> role;

    private String name;		//이름

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @Size(max = 50)
    @Email
    private String email;		//이메일

    private String postCode; 	//우편번호

    private String address; 	//주소1

    private String detailAddress; //주소2(상세주소)

    private String phone;		//전화번호

}
