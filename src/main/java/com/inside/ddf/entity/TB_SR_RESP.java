package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "tb_sr_resp",
       indexes = {
           @Index(name = "idx_tb_sr_resp_user_id", columnList = "user_id"),
           @Index(name = "idx_tb_sr_resp_quest_id", columnList = "quest_id"),
           @Index(name = "idx_tb_sr_resp_item_id", columnList = "item_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TB_SR_RESP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resp_id")
    private Integer respId;

    // FK → TB_USER.USER_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_tb_sr_resp_user"))
    private TB_USER user;

    // FK → TB_SR_ITEM.ITEM_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_tb_sr_resp_item"))
    private TB_SR_ITEM item;
    
    // FK → TB_SR_QUEST.QUEST_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id",
			    nullable = false,
			    foreignKey = @ForeignKey(name = "fk_tb_sr_resp_quest"))
    private TB_SR_QUEST quest;

    @Size(max = 255)
    @Column(name = "resp_txt", length = 255)
    private String respTxt;
}
