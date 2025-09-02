package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
    name = "tb_recipe_likey",
    uniqueConstraints = {
        // 같은 사용자가 같은 레시피를 중복으로 찜하지 못하도록 방지
        @UniqueConstraint(name = "uk_rcp_likey_user_rcp", columnNames = {"user_id", "rcp_id"})
    },
    indexes = {
        @Index(name = "idx_rcp_likey_user", columnList = "user_id"),
        @Index(name = "idx_rcp_likey_rcp",  columnList = "rcp_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_RECIPE_LIKEY {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "likey_id")
    private Integer likeyId;

    // FK → TB_USER.USER_ID (NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_rcp_likey_user")
    )
    @NotNull
    private TB_USER user;

    // FK → TB_RECIPE.RCP_ID (NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "rcp_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_rcp_likey_recipe")
    )
    @NotNull
    private TB_RECIPE recipe;
}