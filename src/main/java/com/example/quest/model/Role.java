package com.example.quest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
//객체매핑
@Entity
//생성자매핑(파라미터없는 생성자 생성)
@NoArgsConstructor
//생성자매핑(모든필드값 생성자 자동생성)
@AllArgsConstructor
//Getter,setter매핑
@Data
//table매핑
@Table(name = "scott_roles")
public class Role {
    //기본키 매핑
    //Id와 GeneratedValue같이사용
    //IDENTITY : 데이터베이스에 위임
    //Auto_Increment
    //SEQUENCE : 데이터베이스 시퀀스 오브젝트 사용(ORACLE)
    //SequenceGenerator 필요
    //TABLE : 키 생성용 테이블 사용, 모든 DB에서 사용
    //TableGenerator 필요
    //AUTO : 자동 지정, 기본값
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // ORDINAL, Enum의 선언된 순서를 Integer 값으로 변환하여 DB 컬럼에 넣어준다.즉, Enum 내부에 선언된 상수들의 순서가 매우 중요하다. DB 컬럼은 numeric 타입이다.
    // STRING, Enum의 선언된 상수의 이름을 String 클래스 타입으로 변환하여 DB에 넣어준다. 즉, DB 클래스 타입은 String이다.
    @Enumerated(EnumType.STRING)
    //컬럼길이
    @Column(length = 20)
    private ERole name;

}
