package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.entity.TB_CHAT_LOG;
import com.inside.ddf.entity.TB_USER;
public interface Rep_CHAT_LOG extends JpaRepository<TB_CHAT_LOG, Integer>{

	public List<TB_CHAT_LOG> findAllByUser(TB_USER user);
}
