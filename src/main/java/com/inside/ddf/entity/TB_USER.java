package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.*;

import com.inside.ddf.code.UserType;

import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_user",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_tb_user_nick_nm", columnNames = "nick_nm")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TB_USER {

    @Id
    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @NotNull
    @Size(max = 50)
    @Column(name = "user_nm", length = 50, nullable = false)
    private String userNm;

    @NotNull
    @Size(max = 50)
    @Column(name = "nick_nm", length = 50, nullable = false, unique = true)
    private String nickNm;

    @NotNull
    @Size(max = 255)
    @Column(name = "user_pw", length = 255, nullable = false)
    private String userPw;

    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 1)
    private UserType userType;

    @Column(name = "preg_week")
    private Integer pregWeek;

    @Past
    @Column(name = "birth_dt")
    private LocalDate birthDt;
    
    @Past
    @Column(name = "create_dt", updatable = false)
    private LocalDate createDt;
    
    
   // entity가 생성될 때 실행하는 코드
    @PrePersist
    protected void onCreate() {
       this.createDt = LocalDate.now();
    }
}