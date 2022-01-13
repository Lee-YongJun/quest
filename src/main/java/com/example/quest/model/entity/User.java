package com.example.quest.model.entity;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
//scott_users에 유니크 제약조건 설정.
@Table(name = "scott_users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username")
        })
public class User extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    //null 과 "" 과 " " 모두 허용하지 않는다.
    @NotBlank
    //최대사이즈 20
    @Size(max = 20)
    private String username;

    private String name;

    @NotBlank
    @Size(max = 120)
    private String password;

    private String email;

    private String postCode; // 우편번호

    private String address; // 주소1

    private String detailAddress; // 주소2(상세주소)

    private String phone;

    //다대다 지연로딩.(양방향연관관계설정)
    @ManyToMany(fetch = FetchType.LAZY)
    //joinColumns:현재 엔티티를 참조하는 외래키
    //inverseJoinColumns :반대방향 엔티티를 참조하는 외래키
    @JoinTable(  name = "scott_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username,String name,String password,String email,String phone,String postCode,String address,String detailAddress) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.postCode = postCode;
        this.address = address;
        this.detailAddress = detailAddress;
    }
}
