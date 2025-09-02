package com.inside.ddf.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.code.GluTypeCode;
import com.inside.ddf.entity.TB_BLOOD_GLU;
import com.inside.ddf.entity.TB_USER;

public interface Rep_BLOOD_GLU extends JpaRepository<TB_BLOOD_GLU, Integer>{

	public Optional<TB_BLOOD_GLU> findByUserAndMeasDtAndGluTypeCd(TB_USER user,LocalDate measDt, GluTypeCode gluTypeCd);
	public List<TB_BLOOD_GLU> findAllByUserAndGluTypeCdOrderByGluIdDesc(TB_USER user,GluTypeCode gluTypeCd,PageRequest of);
	public List<TB_BLOOD_GLU> findAllByUserAndMeasDt(TB_USER user,LocalDate measDt);
	
}
