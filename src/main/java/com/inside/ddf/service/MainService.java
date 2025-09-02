package com.inside.ddf.service;



import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import com.inside.ddf.code.GluTypeCode;
import com.inside.ddf.dto.req.ChatReq;
import com.inside.ddf.dto.res.ChatRes;
import com.inside.ddf.entity.TB_BLOOD_GLU;
import com.inside.ddf.entity.TB_CALENDAR;
import com.inside.ddf.entity.TB_CHAT_LOG;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.repository.Rep_BLOOD_GLU;
import com.inside.ddf.repository.Rep_CALENDAR;
import com.inside.ddf.repository.Rep_CHAT_LOG;

import jakarta.servlet.http.HttpSession;



@Service
@RequiredArgsConstructor
public class MainService {
	

	private final WebClient fastApiClient;

	@Autowired
	Rep_CHAT_LOG rep_chat;
	
	@Autowired
	Rep_CALENDAR rep_cal;
	
	@Autowired
	Rep_BLOOD_GLU rep_glu;
	
	// 챗봇에 진입했을 때
	public List<TB_CHAT_LOG> getChatLog(TB_USER user) {
		return rep_chat.findAllByUser(user);
	}
	
	
	// 사용자에게 답변용
	public String getExplainChat(ChatReq req, HttpSession session) {
		ChatRes res = getChat(req);
		saveChatContent(res,session);
		return res.getExplanation();
	}
	
	
	//DB에 저장하는 용도
	private void saveChatContent(ChatRes res, HttpSession session) {
		TB_CHAT_LOG chat = new TB_CHAT_LOG();
		chat.setInputTxt(res.getInput());
		chat.setOutputTxt(res.getExplanation());
		TB_USER user = (TB_USER) session.getAttribute("user");
		chat.setUser(user);
		chat = rep_chat.save(chat);
		System.out.println("저장되었습니다.");
		System.out.println(chat.getChatDt());
		
//		System.out.println(rep_chat.findById(1).get().getOutputTxt());
	}
	
    public ChatRes getChat(ChatReq req){
    	ChatRes res =  fastApiClient.post()
                .uri("/api/model/chat/ask")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ChatRes.class)
                .block(); // 간단히 block(), 서비스 내부에서 reactive 유지도 가능
    	//System.out.println(res.getExplanation());
    	return res;
    }
    
    public TB_CALENDAR updateHosDate(TB_USER user, LocalDate hosDate) {
    	Optional<TB_CALENDAR> temp = rep_cal.findByUser(user);
    	TB_CALENDAR calendar = new TB_CALENDAR();
    	if (temp.isPresent()) {
    		calendar = temp.get();
    	}
    	calendar.setHosDate(hosDate);
    	calendar.setUser(user);
    	return rep_cal.save(calendar);
    }
    public Optional<TB_CALENDAR> getHosDate(TB_USER user) {
    	return rep_cal.findByUser(user);
    }
    
    public Optional<Integer> getDday(TB_USER user) { //병원 다녀온지 좀 되면 +가 뜨도록 하기 위해. D-3 / D+4 이런 느낌.
    	Optional<TB_CALENDAR> cal = getHosDate(user);
    	if (cal.isEmpty()) {
    		return Optional.empty();
    	}
    	return Optional.ofNullable(Period.between(cal.get().getHosDate(), LocalDate.now()).getDays());
    }
    
    public TB_BLOOD_GLU updateBlood(TB_USER user, LocalDate date, GluTypeCode type, int measurement) {
    	Optional<TB_BLOOD_GLU> temp = rep_glu.findByUserAndMeasDtAndGluTypeCd(user,date,type);
    	TB_BLOOD_GLU glucose = new TB_BLOOD_GLU();
    	if (temp.isPresent()) {
    		glucose = temp.get(); //해당 날짜와 시간에 해당하는 입력이 있으면 그 입력을 수정.
    	}
    	glucose.setGluTypeCd(type);
    	glucose.setGluVal(measurement);
    	glucose.setMeasDt(date);
    	glucose.setUser(user);
    	return rep_glu.save(glucose);
    }
    public List<TB_BLOOD_GLU> getFastBlood(TB_USER user, int rank) {
    	return rep_glu.findAllByUserAndGluTypeCdOrderByGluIdDesc(user,GluTypeCode.F,PageRequest.of(0, rank));
    }
    public List<TB_BLOOD_GLU> findTodayBlood(TB_USER user) { 
    	return rep_glu.findAllByUserAndMeasDt(user,LocalDate.now());
    }
    
    public Optional<Double> getAverageFastBlood(TB_USER user) {
    	List<TB_BLOOD_GLU> gluList = getFastBlood(user, 7);
    	Optional<Double> result = Optional.empty();
    	if (gluList.size()==0) return result;
    	Double temp = 0.0;
    	for (int i=0;i<gluList.size();i++) {
    		temp += gluList.get(i).getGluVal();
    	}
    	result = Optional.ofNullable(temp/gluList.size());
    	return result;
    }
    
    public List<Optional<Integer>> getTodayBlood(TB_USER user) {
    	List<TB_BLOOD_GLU> gluList = findTodayBlood(user);
    	List<Optional<Integer>> result = Arrays.asList(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty());
    	for (TB_BLOOD_GLU glu : gluList) {
    		switch(glu.getGluTypeCd()) {
    		case F :
    			result.set(0, Optional.ofNullable(glu.getGluVal()));
    			break;
    		case M :
    			result.set(1, Optional.ofNullable(glu.getGluVal()));
    			break;
    		case A :
    			result.set(2, Optional.ofNullable(glu.getGluVal()));
    			break;
    		case E :
    			result.set(3, Optional.ofNullable(glu.getGluVal()));
    			break;
    		}
    	}
    	return result;
    }
    
    //마이페이지 용도
    public List<List<TB_BLOOD_GLU>> find7daysBlood(TB_USER user) { 
    	List<List<TB_BLOOD_GLU>> result = new ArrayList<>();
		LocalDate date = LocalDate.now();
    	for(int i=6;i>=0;i--) {
    		result.add(rep_glu.findAllByUserAndMeasDt(user,date.minusDays(i))) ;
    	}
    	return result;
    }
    public List<List<Integer>> get7daysBlood(TB_USER user) {
    	List<List<TB_BLOOD_GLU>> gluList = find7daysBlood(user);
    	List<List<Integer>> result = Arrays.asList(
    			Arrays.asList(-1,-1,-1,-1),
    			Arrays.asList(-1,-1,-1,-1),
    			Arrays.asList(-1,-1,-1,-1),
    			Arrays.asList(-1,-1,-1,-1),
    			Arrays.asList(-1,-1,-1,-1),
    			Arrays.asList(-1,-1,-1,-1),
    			Arrays.asList(-1,-1,-1,-1)
    			);
    	for (int i=0;i<gluList.size();i++) {
    		for (TB_BLOOD_GLU glu:gluList.get(i)) {
    			switch(glu.getGluTypeCd()) {
        		case F :
        			result.get(i).set(0, glu.getGluVal());
        			break;
        		case M :
        			result.get(i).set(1, glu.getGluVal());
        			break;
        		case A :
        			result.get(i).set(2, glu.getGluVal());
        			break;
        		case E :
        			result.get(i).set(3, glu.getGluVal());
        			break;
        		}
    		}
    	}
    	return result;
    }
    
    @Autowired
    private UserService userService;

    @Autowired
    private MealService mealService;
    
    @Scheduled(cron = "0 0 0 * * SUN", zone = "Asia/Seoul")
    public void runSundayJobs() {
        userService.updateWeek();   	  // 1) 주차수 업데이트
        mealService.addAllUserMeal();     // 2) 식단 추천
    }
}
