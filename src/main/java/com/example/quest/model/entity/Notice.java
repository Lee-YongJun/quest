package com.example.quest.model.entity;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "scott_notice")
public class Notice extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ScottNoticeSequenceGenerator")
    @SequenceGenerator(name="ScottNoticeSequenceGenerator", sequenceName = "ScottNoticeSequence", initialValue = 1, allocationSize = 1)
    private Long id;

    private String title;

    private String content;

    private String writer;

}
