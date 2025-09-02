package com.inside.ddf.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inside.ddf.code.UserType;
import com.inside.ddf.dto.frontend.GetResponseAllergyDTO;
import com.inside.ddf.dto.frontend.GetResponseComposeDTO;
import com.inside.ddf.dto.frontend.GetResponseHabitsDTO;
import com.inside.ddf.dto.frontend.GetResponseInfoDTO;
import com.inside.ddf.dto.frontend.GetResponsePreferenceDTO;
import com.inside.ddf.dto.frontend.GetResponseUnknownFpgDTO;
import com.inside.ddf.dto.frontend.GetResponseUnknownPpgDTO;
import com.inside.ddf.dto.req.MealReq;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.service.MealService;
import com.inside.ddf.service.SurveyService;
import com.inside.ddf.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/survey")
public class SurveyRestController {

	@Autowired
	SurveyService surveyService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	MealService mealService; // 설문 완료 후, 식단 추천.

	// getResponseallergy()까지는 "다음 버튼을 눌렀을 떄" 매핑되어 실행되는 함수들.
	@PostMapping("/basic-info")
	public void getResponseInfo(@RequestBody GetResponseInfoDTO dto, HttpSession session) {

		TB_USER user= (TB_USER) session.getAttribute("user");
		int [] answer = {dto.getAnswer2()};
		surveyService.saveResponse(2,answer,user);
	}
	@PostMapping("/bstrend/unknown-fpg")
	public void getResponseUnknownFpg(@RequestBody GetResponseUnknownFpgDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		
		int [] answer = {dto.getAnswer3(),dto.getAnswer4(),dto.getAnswer5()};
		surveyService.saveResponse(3,answer,user);
	}
	@PostMapping("/bstrend/fpg")
	public void getResponseFpg(@RequestBody GetResponseUnknownFpgDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");

		int [] answer = {dto.getAnswer3(),dto.getAnswer4(),dto.getAnswer5()};
		surveyService.saveResponse(3,answer,user);
	}
	@PostMapping("/bstrend/unknown-ppg")
	public void getResponseUnknownPpg(@RequestBody GetResponseUnknownPpgDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		int [] answer = {dto.getAnswer6(),dto.getAnswer7(),dto.getAnswer8(),dto.getAnswer9()};
 		surveyService.saveResponse(6,answer,user);
	}
	@PostMapping("/bstrend/ppg")
	public void getResponsePpg(@RequestBody GetResponseUnknownPpgDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		int [] answer = {dto.getAnswer6(),dto.getAnswer7(),dto.getAnswer8(),dto.getAnswer9()};
 		surveyService.saveResponse(6,answer,user);
	}
	@PostMapping("/diet/compose")
	public void getResponseCompose(@RequestBody GetResponseComposeDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		int [] answer = {dto.getAnswer11(),dto.getAnswer11(),dto.getAnswer12()};
		// 10번 따로 받기
 		surveyService.saveResponse(10,answer,user,dto.getAnswer10());
	}
	@PostMapping("/habits")
	public void getResponseHabits(@RequestBody GetResponseHabitsDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		int [] answer = {dto.getAnswer13(),dto.getAnswer14()};
 		surveyService.saveResponse(13,answer,user);
	}
	@PostMapping("/meal/preference")
	public void getResponsePreference(@RequestBody GetResponsePreferenceDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		int [] answer = {dto.getAnswer15()};
 		surveyService.saveResponse(15,answer,user);
	}
	@PostMapping("/allergy")
	public void getResponseAllergy(@RequestBody GetResponseAllergyDTO dto, HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		int [] answer = {16}; //saveResponse에서 answer값이 아무것도 없어서 실행을 한번도 안함. -> 아무 값이나 넣음.
 		surveyService.saveResponse(16,answer,user,dto.getAnswer16());
	}
	
	
	@GetMapping("/done")
	public void surveyDone(HttpSession session) {
		TB_USER user= (TB_USER) session.getAttribute("user");
		
		//유저 정보 업데이트
		SurveyService.CaseClassification out = surveyService.classify(surveyService.setClassifyInput(user));
		System.out.println(out.primaryLabel.toString());
		UserType result = UserType.valueOf(out.primaryLabel.toString());
		user.setUserType(result);
		user = userService.join(user);
		session.setAttribute("user", user);
		
		//식단 추천 받기
		MealReq req = new MealReq();
		req.setUser_type(user.getUserType().toString());
		// 설문조사가 완료되면 추가가능.
    	req.setAllergies(surveyService.getAllergies(user));
    	req.setPreferences(surveyService.getPreferences(user));
		mealService.addMeal(req, user);
	}
}
