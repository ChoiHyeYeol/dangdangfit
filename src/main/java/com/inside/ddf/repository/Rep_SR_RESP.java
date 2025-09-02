package com.inside.ddf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.entity.TB_SR_QUEST;
import com.inside.ddf.entity.TB_SR_RESP;
import com.inside.ddf.entity.TB_USER;

public interface Rep_SR_RESP extends JpaRepository<TB_SR_RESP, Integer>{

	public Optional<TB_SR_RESP> findByUserAndQuest(TB_USER user, TB_SR_QUEST quest);
	public List<TB_SR_RESP> findAllByUserAndQuest(TB_USER user, TB_SR_QUEST quest);
}
