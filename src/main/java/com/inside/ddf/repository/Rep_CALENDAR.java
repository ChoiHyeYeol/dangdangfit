package com.inside.ddf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.entity.TB_CALENDAR;
import com.inside.ddf.entity.TB_USER;

public interface Rep_CALENDAR extends JpaRepository<TB_CALENDAR, Integer>{

	public Optional<TB_CALENDAR> findByUser(TB_USER user); 
}
