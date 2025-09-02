package com.inside.ddf.controller;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.inside.ddf.dto.MealUpdateGet;
import com.inside.ddf.dto.frontend.EnterMealDTO;
import com.inside.ddf.dto.frontend.GetMealInDTO;
import com.inside.ddf.dto.req.MealReq;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.service.MealService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MealRestController {

	
	@Autowired
	MealService mealService;
	
	
	// 노션 6번 기능. 관리자가 관리하는 부분 -> 일요일 12시에 자동으로 생성되어서 저장됨. 모든 사용자에 대해.
    @PostMapping("/api/model/diet/weekly")
    public void recommand() {
    	mealService.addAllUserMeal();
    }
    
    // 노션 5번 기능. 처음 가입한 사람에게 식단 추천 -> Survey Controller에서 진행하는걸로.
    /*
    @PostMapping("/api/model/diet/initial")
    public void recommandInitial(HttpSession session) {
    	TB_USER user = (TB_USER) session.getAttribute("user");
    	MealReq req = new MealReq();
    	if(user == null) {
    		System.out.println("세션이 만료되었습니다.");
    	}
    	else {
    		req.setUser_type(user.getUserType().toString());
    		// 설문조사가 완료되면 추가가능.
    		List<String> allergies = List.of("");
	    	List<String> preferences = List.of("");
	    	req.setAllergies(allergies);
	    	req.setPreferences(preferences);
    	}
    	mealService.addMeal(req, user);
    	System.out.println("추가가 완료되었습니다.");
    }

     */
    
    
    // 노션 4번 기능. 한 끼 식단 새로고침
    @PostMapping("/api/model/diet/one")
    public List<List<String>> refreshMeal(@RequestBody MealUpdateGet meal, HttpSession session) {
    	TB_USER user = (TB_USER) session.getAttribute("user");
    	List<List<String>> list = mealService.refreshMeal(meal,user);
//    	for(int i=0;i<list.size();i++) System.out.println(list.get(i).get(0)+list.get(i).get(1));
    	return list;
    }
    
    // 노션 1번 기능. 진입했을 때
    @GetMapping("/api/diet")
    public EnterMealDTO enterMeal(HttpSession session) {
    	// 유저 세션 불러오기 및 식단 세션 설정.
    	TB_USER user = (TB_USER) session.getAttribute("user");
    	LocalDate day = LocalDate.now();
    	session.setAttribute("mealSelectDate", day);
    	LocalDateTime date = LocalDateTime.now();
		int time=0;
		if (date.getHour()<11) time=0;
		else if(date.getHour()<16) time=1;
		else time=2;
    	session.setAttribute("mealSelectTime", time);
    	return mealService.enterMeal(day,user,time);
    }
    
    // 노션 2번 + 3번 기능.
    // 날짜를 선택하면 자동으로 FE에서의 state 값이 0으로 바뀌게 초기화하기.
    // 다음, 이전을 눌러도 현재 선택된 날짜가 session에 담겨져 있으므로 해당 날짜에서의 이동 가능.
    @PostMapping("/api/diet")
    public List<List<String>> getMeal(@RequestBody GetMealInDTO dto, HttpSession session) {
    	TB_USER user = (TB_USER) session.getAttribute("user");
    	System.out.println(dto.getTime());
		session.setAttribute("mealSelectDate", dto.getDate());
		session.setAttribute("mealSelectTime", dto.getTime());
		System.out.println(mealService.getMeal(user,dto.getTime(),dto.getDate()));
    	return mealService.getMeal(user,dto.getTime(),dto.getDate());
    	
    
    }
    
    
}
