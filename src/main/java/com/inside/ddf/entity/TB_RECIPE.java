package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.inside.ddf.code.CategoryMenu;
import com.inside.ddf.code.CategoryTime;

import lombok.*;

@Entity
@Table(name = "tb_recipe",
       indexes = {
           @Index(name = "idx_tb_recipe_user", columnList = "user_id"),
           @Index(name = "idx_tb_recipe_nm", columnList = "rcp_nm")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_RECIPE {

    @Id
    @Column(name = "rcp_id")
    private String rcpId;

    // User 참조 (작성자)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_tb_recipe_user"))
    private TB_USER user;

    @NotNull
    @Size(max = 100)
    @Column(name = "rcp_nm", length = 200, nullable = false)
    private String rcpNm;

    @Column(name = "desc_txt", length = 600)
    private String descTxt;
    
    @Column(name = "portion")
    private String portion;
    
    @Column(name = "time")
    private String time;
    
    @Column(name = "level")
    private String level;

    @Column(name = "cal_val")
    private Integer calVal;

    @Column(name = "gi_val")
    private Integer giVal;

    @Column(name = "ch_val")
    private Integer chVal;

    @Column(name = "pr_val")
    private Integer prVal;

    @Column(name = "fat_val")
    private Integer fatVal;

    @Column(name = "ic_val")
    private Integer icVal;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_time", length = 1)
    private CategoryTime categoryTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_menu", length = 1)
    private CategoryMenu categoryMenu;

    @Size(max = 300)
    @Column(name = "tip", length = 500)
    private String tip;
    
    @Column(name = "main_img", length = 255)
    private String mainImg;    // 단계 이미지 경로
}