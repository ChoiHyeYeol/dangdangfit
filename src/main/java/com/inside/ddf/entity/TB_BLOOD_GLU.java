package com.inside.ddf.entity;


import jakarta.persistence.*;
import javax.validation.constraints.*;

import com.inside.ddf.code.GluTypeCode;

import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_blood_glu",
       indexes = {
           @Index(name = "idx_tb_blood_glu_user_id", columnList = "user_id"),
           @Index(name = "idx_tb_blood_glu_meas_dt", columnList = "meas_dt")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TB_BLOOD_GLU {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "glu_id")
    private Integer gluId;

    // FK → TB_USER.USER_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_tb_blood_glu_user"))
    private TB_USER user;

    @NotNull
    @Column(name = "meas_dt", nullable = false)
    private LocalDate measDt;   // datetime → LocalDateTime 매핑

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "glu_type_cd", length = 1, nullable = false)
    private GluTypeCode gluTypeCd;       // 공복/식후 구분 코드 등

    @NotNull
    @Column(name = "glu_val", nullable = false)
    private Integer gluVal;         // 혈당 수치
}