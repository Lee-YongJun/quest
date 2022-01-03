package com.example.quest.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
//scott_users에 유니크 제약조건 설정.
@Table(name = "scott_users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    //null 과 "" 과 " " 모두 허용하지 않는다.
    @NotBlank
    //최대사이즈 20
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 120)
    private String password;

    //다대다 지연로딩.(양방향연관관계설정)
    @ManyToMany(fetch = FetchType.LAZY)
    //joinColumns:현재 엔티티를 참조하는 외래키
    //inverseJoinColumns :반대방향 엔티티를 참조하는 외래키
    @JoinTable(  name = "scott_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username,String password) {
        this.username = username;
        this.password = password;
    }
}
