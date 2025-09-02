package com.inside.ddf.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.repository.Rep_USER;

@Service
public class UserService {

	@Autowired
	Rep_USER rep_user;
	
	public TB_USER findByIdAndPassword(String userID, String userPassword) {
		Optional<TB_USER> entity = rep_user.findById(userID);
		if(!entity.isEmpty()) {
			TB_USER user = rep_user.findById(userID).get();
			if (user.getUserPw().equals(userPassword)) return user;
		}
		return null;
		
	}
	
	public Optional<TB_USER> findById(String userID) {
		return rep_user.findById(userID);
	}
	
	public TB_USER join(TB_USER user) {
		return rep_user.save(user);
	}
	
	public void updateWeek() {
		List<TB_USER> userList = rep_user.findAll();
		for(TB_USER user : userList) {
			user.setPregWeek(user.getPregWeek()+1);
			rep_user.save(user);
		}
	}
}
