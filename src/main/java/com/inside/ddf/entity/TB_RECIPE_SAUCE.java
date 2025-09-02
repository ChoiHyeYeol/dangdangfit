package com.inside.ddf.entity;


import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
    name = "tb_recipe_sauce",
    indexes = {
        @Index(name = "idx_tb_recipe_sauce_rcp", columnList = "rcp_id"),
        @Index(name = "idx_tb_recipe_sauce_name", columnList = "sauce_name")
    },
    // 동일 레시피 내 같은 소스명 중복 방지(원치 않으면 제거)
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rcp_sauce_name", columnNames = {"rcp_id", "sauce_name"})
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_RECIPE_SAUCE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sauce_id")
    private Integer sauceId;

    // FK → TB_RECIPE.RCP_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "rcp_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tb_recipe_sauce_rcp")
    )
    private TB_RECIPE recipe;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "sauce_name", length = 200, nullable = false)
    private String sauceName;

    @Size(max = 50)
    @Column(name = "sauce_cnt", length = 50)
    private String sauceCnt; // 예: "1T", "1/2C", "약간" 등
}