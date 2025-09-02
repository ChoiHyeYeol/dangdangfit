package com.inside.ddf.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.entity.TB_SR_ITEM;
import com.inside.ddf.entity.TB_SR_QUEST;

public interface Rep_SR_ITEM extends JpaRepository<TB_SR_ITEM, Integer>{

	public List<TB_SR_ITEM> findAllByQuest(TB_SR_QUEST quest);
	
	public TB_SR_ITEM findByQuestAndSortOrd(TB_SR_QUEST quest, Integer sortOrd);
}
