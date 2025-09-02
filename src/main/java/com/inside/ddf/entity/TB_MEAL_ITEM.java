package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "tb_meal_item",
       indexes = {
           @Index(name = "idx_tb_meal_item_meal", columnList = "meal_id"),
           @Index(name = "idx_tb_meal_item_food", columnList = "food_id"),
           @Index(name = "idx_tb_meal_item_rcp",  columnList = "rcp_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
// DB 레벨 강제: FOOD_ID 또는 RCP_ID 둘 중 최소 하나는 존재
@Check(constraints = "(food_id is not null) OR (rcp_id is not null)")
public class TB_MEAL_ITEM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    // FK → TB_MEAL_PLAN.MEAL_ID (NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_tb_meal_item_meal"))
    @NotNull
    private TB_MEAL_PLAN meal;

    // FK → TB_FOOD.FOOD_ID (선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id",
                foreignKey = @ForeignKey(name = "fk_tb_meal_item_food"))
    private TB_FOOD food;

    // FK → TB_RECIPE.RCP_ID (선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rcp_id",
                foreignKey = @ForeignKey(name = "fk_tb_meal_item_rcp"))
    private TB_RECIPE recipe;

    // 애플리케이션 단에서도 동일 규칙 검증(둘 다 null 금지)
    @AssertTrue(message = "FOOD 또는 RECIPE 중 최소 하나는 설정해야 합니다.")
    public boolean hasFoodOrRecipe() {
        return food != null || recipe != null;
    }
}