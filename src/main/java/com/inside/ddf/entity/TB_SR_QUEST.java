package com.inside.ddf.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.inside.ddf.code.QuestType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_sr_quest")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TB_SR_QUEST {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quest_id")
    private Integer questId; // int

    @NotNull
    @Size(max = 255)
    @Column(name = "quest_cont", nullable = false, length = 255)
    private String questCont; // varchar(255)

    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", length = 1)
    private QuestType questType; // varchar(1) (또는 char(1))
	
}
