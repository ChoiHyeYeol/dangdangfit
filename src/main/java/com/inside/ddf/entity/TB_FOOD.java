package com.inside.ddf.entity;


import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "tb_food",
       indexes = {
           @Index(name = "idx_tb_food_nm", columnList = "food_nm")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_FOOD {

    @Id
    @Column(name = "food_id")
    private String foodId;

    @NotNull
    @Size(max = 100)
    @Column(name = "food_nm", length = 100, nullable = false)
    private String foodNm; // 음식명 (예: 바나나, 딸기 등)

    @Column(name = "desc_txt")
    private String descTxt; // 설명

    @Column(name = "cal_val")
    private Double calVal; // 칼로리

    @Column(name = "gi_val")
    private Double giVal;  // GI 지수

    @Column(name = "ch_val")
    private Double chVal;  // 탄수화물(g)

    @Column(name = "pr_val")
    private Double prVal;  // 단백질(g)

    @Column(name = "fat_val")
    private Double fatVal; // 지방(g)

    @Column(name = "ic_val")
    private Double icVal;  // 철분(mg)
}
