package com.inside.ddf.controller;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inside.ddf.code.GluTypeCode;
import com.inside.ddf.dto.frontend.MainRecipeDTO;
import com.inside.ddf.dto.req.ChatReq;
import com.inside.ddf.entity.TB_BLOOD_GLU;
import com.inside.ddf.entity.TB_CHAT_LOG;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.service.MainService;
import com.inside.ddf.service.MealService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

record CalendarRequest(LocalDate hosDate) {}
record BloodRequest(GluTypeCode type, int measurement) {}
record EnterMainResponse(String nickname,
		int pregweek,
		int dday,
		MainRecipeDTO[] rcp, //아침, 점심, 저녁 값 받아오기.
		int glucose // 혈당
		) {}
record CalendarResponse(LocalDate hosDate, int dday) {}

@RestController
@RequiredArgsConstructor
public class MainRestController {


	@Autowired
	MainService mainService;
	
	@Autowired
	MealService mealService;



	// 챗봇에게 질문 입력하고 "보내기" 눌렀을 때 -> 답변 한줄이 반환됨.
    @PostMapping("/api/model/chat/ask")
    public String recommend(@RequestBody ChatReq req, HttpSession session){
    	return mainService.getExplainChat(req, session);
    	//mainService.getChat(req);
    }

    // 챗봇에 처음 막 진입했을 때. -> 이전 대화 기록을 원한다면 BE 담당자에게 문의할것.
    @GetMapping("/api/chat")
    public void enterChat(HttpSession session) {
    	TB_USER user= (TB_USER) session.getAttribute("user");
//    	String Nickname = user.getNickNm();
//    	return Nickname;
    	List<TB_CHAT_LOG> chat_log = mainService.getChatLog(user);
    	for (int i=0; i<chat_log.size();i++) {
    		System.out.println(chat_log.get(i).getOutputTxt());
    	}
//    	return chat_log;
    }
    
    // 로그인 끝나고 메인에 막 진입했을 때. + 상단의  record EnterMainResponse 참고
    @GetMapping("/api/main")
    public EnterMainResponse enterMain(HttpSession session) {
    	TB_USER user= (TB_USER) session.getAttribute("user");
    	//닉네임
    	String nm = user.getNickNm();
//    	System.out.println(nm);
    	//임신주차수
    	int pw = user.getPregWeek();
//    	System.out.println(pw);
    	//한끼 식단 중 레시피 + 레시피 아이디
    	MainRecipeDTO[] recipes = {new MainRecipeDTO(),new MainRecipeDTO(),new MainRecipeDTO()};
    	for (int i=0;i<recipes.length;i++) {
    		recipes[i] = mealService.getMainRecipe(user, i, LocalDate.now());
//    		System.out.println(recipes[i].getRcpNm());
    	}
    	//Dday
    	Integer dd = Integer.MIN_VALUE; // 입력하지 않으면 해당 값으로 출력.
    	Optional<Integer> tempdd = mainService.getDday(user);
    	if (tempdd.isPresent()) dd=tempdd.get();
//    	System.out.println(dd);
    	//혈당 데이터
    	LocalDateTime date = LocalDateTime.now();
		int time=0;
		if (date.getHour()<8) time=0;
		if (date.getHour()<11) time=1;
		else if(date.getHour()<16) time=2;
		else time=3;
		
		List<Optional<Integer>> gluList = mainService.getTodayBlood(user);
		Integer glu = -1;
    	if (gluList.get(time).isPresent()) glu=gluList.get(time).get();
    	
//    	System.out.println(gluList.get(time));
    	EnterMainResponse result = new EnterMainResponse(nm,pw,dd,recipes,glu);
    	return result;
    }
    
    // 일정 관리 클릭해서 "팝업"이 띄워졌을 때, 날짜 가져오기
    @GetMapping("/api/get/calendar")
    public LocalDate getCalendar(HttpSession session) {
    	LocalDate hosDate = (LocalDate) session.getAttribute("cal");
    	return hosDate;
    }
    
    //혈당 관리 클릭해서 "팝업"이 띄워졌을 때, 해당 시간의 가져오기
    // get 방식이고, type 파라미터에 현재 시간을 알려줘야함.
    // 공복 : type=F
    // 아침 : type=M
    // 점심 : type=A
    // 저녁 : type=E
    @GetMapping("/api/get/blood")
    public int getBlood(@RequestParam GluTypeCode type, HttpSession session) {
    	TB_USER user= (TB_USER) session.getAttribute("user");
    	List<Integer> list = (List<Integer>)session.getAttribute("gluList");
		switch(type) {
		case F: return list.get(0);
		case M: return list.get(1);
		case A: return list.get(2);
		case E: return list.get(3);
		default: return -1;
		}
    }
    // 일정관리 팝업창에서 "저장" 눌렀을 때
    @PostMapping("/api/update/calendar")
    public CalendarResponse updateCalendar(@RequestBody CalendarRequest req, HttpSession session) {
    	TB_USER user= (TB_USER) session.getAttribute("user");
    	LocalDate hosDate = mainService.updateHosDate(user, req.hosDate()).getHosDate();
    	session.setAttribute("cal", hosDate);
    	int dday = mainService.getDday(user).get();
    	session.setAttribute("dday", dday);
    	System.out.println();
    	return new CalendarResponse(hosDate,dday);
    }
    // 혈당관리 팝업창에서 "저장" 눌렀을 때
    @PostMapping("/api/update/blood")
    public int updateBlood(@RequestBody BloodRequest req, HttpSession session) {
    	TB_USER user= (TB_USER) session.getAttribute("user");
    	TB_BLOOD_GLU glue = mainService.updateBlood(user,LocalDate.now(),req.type(),req.measurement());

    	List<Optional<Integer>> gluList = mainService.getTodayBlood(user);
		List<Integer> list=new ArrayList<>();
		for(int i=0;i<gluList.size();i++) {
			if(gluList.get(i).isEmpty()) {
				list.add(-1);
			}
			else {
				list.add(gluList.get(i).get());
			}
		}
		session.setAttribute("gluList", list);
		
    	return glue.getGluVal();
    	
    }
}
