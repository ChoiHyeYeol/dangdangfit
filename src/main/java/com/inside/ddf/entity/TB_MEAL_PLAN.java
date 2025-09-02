package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.*;

import com.inside.ddf.code.CategoryTime;
import com.inside.ddf.code.ImplementFlag;

import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_meal_plan",
       indexes = {
           @Index(name = "idx_tb_meal_plan_user_id", columnList = "user_id"),
           @Index(name = "idx_tb_meal_plan_date_type", columnList = "meal_date, meal_type")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TB_MEAL_PLAN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Integer mealId;

    // FK → TB_USER.USER_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_tb_meal_plan_user"))
    private TB_USER user;

    @NotNull
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", length = 1, nullable = false)
    private CategoryTime mealType; // 아침/점심/저녁/간식 구분 코드

    @Column(name = "total_cal")
    private Integer totalCal;   // 총 칼로리

    @Column(name = "meal_gi_val")
    private Integer mealGiVal;  // 당류(g)

    @Column(name = "meal_ch_val")
    private Integer mealChVal;  // 탄수화물(g)

    @Column(name = "meal_pr_val")
    private Integer mealPrVal;  // 단백질(g)

    @Column(name = "meal_fat_val")
    private Integer mealFatVal; // 지방(g)

    @Column(name = "meal_ic_val")
    private Integer mealIcVal;  // 철분(mg)

    // FK → TB_BLOOD_GLU.GLU_ID (식전 혈당)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "glu_id_bef",
                foreignKey = @ForeignKey(name = "fk_tb_meal_plan_glu_bef"))
    private TB_BLOOD_GLU bloodGluBefore;

    // FK → TB_BLOOD_GLU.GLU_ID (식후 혈당)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "glu_id_aft",
                foreignKey = @ForeignKey(name = "fk_tb_meal_plan_glu_aft"))
    private TB_BLOOD_GLU bloodGluAfter;

    @Size(max = 255)
    @Column(name = "meal_img", length = 255)
    private String mealImg;  // 식단 레시피 이미지 경로

    @Enumerated(EnumType.STRING) // 'Y','N' 그대로 저장
    @Column(name = "meal_implement", length = 1)
    private ImplementFlag mealImplement;

    @Size(max = 255)
    @Column(name = "auth_img", length = 255)
    private String authImg;  // 인증 이미지 경로
}
