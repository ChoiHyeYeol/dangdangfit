package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "tb_sr_item",
       indexes = {
         @Index(name = "idx_tb_sr_item_quest_id", columnList = "quest_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_SR_ITEM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId; // int PK, auto increment

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_tb_sr_item_quest"))
    private TB_SR_QUEST quest; // FK → TB_SR_QUEST.QUEST_ID

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "item_cont", length = 255, nullable = false)
    private String itemCont; // varchar(255)

    @NotNull
    @Column(name = "sort_ord", nullable = false)
    private Integer sortOrd; // int
}