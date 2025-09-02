package com.inside.ddf.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_recipe_step")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TB_RECIPE_STEP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;   // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rcp_id", nullable = false)
    private TB_RECIPE recipe; // TB_RECIPE 참조 (다대일 관계)

    @Column(name = "step_ord", nullable = false)
    private Integer stepOrd;   // 단계 순서

    @Column(name = "step_cont", length = 500, nullable = false)
    private String stepCont;   // 단계 설명

    @Column(name = "step_img", length = 255)
    private String stepImg;    // 단계 이미지 경로
}