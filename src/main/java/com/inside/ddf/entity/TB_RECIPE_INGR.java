package com.inside.ddf.entity;


import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
    name = "tb_recipe_ingr",
    indexes = {
        @Index(name = "idx_tb_recipe_ingr_rcp", columnList = "rcp_id"),
        @Index(name = "idx_tb_recipe_ingr_name", columnList = "ingr_name")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_RECIPE_INGR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingr_id")
    private Integer ingrId;

    // FK → TB_RECIPE.RCP_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "rcp_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tb_recipe_ingr_rcp")
    )
    private TB_RECIPE recipe;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "ingr_name", length = 200, nullable = false)
    private String ingrName;   // 재료 이름 (예: 바나나, 딸기 등)

    @Size(max = 50)
    @Column(name = "ingr_cnt", length = 50)
    private String ingrCnt;    // 계량 값 (예: "2개", "100g", "1컵")
}