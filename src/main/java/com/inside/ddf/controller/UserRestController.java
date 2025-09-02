package com.inside.ddf.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.inside.ddf.config.ApiClientConfig;
import com.inside.ddf.dto.frontend.RecipeTopDto;
import com.inside.ddf.entity.TB_CALENDAR;
import com.inside.ddf.entity.TB_RECIPE;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.service.MainService;
import com.inside.ddf.service.RecipeService;
import com.inside.ddf.service.SurveyService;
import com.inside.ddf.service.UserService;

import jakarta.servlet.http.HttpSession;

record SignupRequest(
    String userID, String userPassword
) {}

record SignupResponse(boolean success, boolean surveyCompleted) {}

record JoinRequest(
		String userID, String userPassword, String userName, String nickName
		, Integer pregWeek, LocalDate birthDate) {}

record UserUpdateRequest(
		String userPassword, String nickName) {}

record EnterMyRecipeRequest(String category) {}

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final ApiClientConfig apiClientConfig;

	@Autowired
	UserService userService;
	@Autowired
	MainService mainService;
	
	@Autowired
	SurveyService surveyService;
	
	@Autowired
	RecipeService recipeService;

    UserRestController(ApiClientConfig apiClientConfig) {
        this.apiClientConfig = apiClientConfig;
    }
	
	@PostMapping("/userauth/login")
	public  ResponseEntity<SignupResponse> login(@RequestBody SignupRequest req, HttpSession session) {
		TB_USER user =  userService.findByIdAndPassword(req.userID(), req.userPassword());
		if (user != null) {
			session.setAttribute("user", user); // 유저 정보 기억
			
			boolean surveyCompleted = surveyService.isCompleted(user);
			
			Optional<TB_CALENDAR> cal =  mainService.getHosDate(user);
			if (cal.isPresent()) {
				session.setAttribute("cal", cal.get().getHosDate()); // 유저 D-day 기억
				session.setAttribute("dday", mainService.getDday(user).get());
			}
			else {
				session.setAttribute("cal", LocalDate.MIN); //D-day가 없으면 가장 최솟값 출력.
				session.setAttribute("dday", Integer.MIN_VALUE);
			}
			
			Optional<Double> avg_FPG =  mainService.getAverageFastBlood(user);
			if (avg_FPG.isPresent()) session.setAttribute("avg_FPG", avg_FPG.get()); // 유저 D-day 기억
			else session.setAttribute("avg_FPG", -1);
			
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
			session.setAttribute("gluList", list); //유저 오늘치 혈당 기록 기억
			
			System.out.println( (LocalDate)session.getAttribute("cal"));
			System.out.println(session.getAttribute("avg_FPG"));
			for(int i=0;i<list.size();i++) System.out.println(list.get(i));
			
			
			return ResponseEntity.ok(new SignupResponse(true, surveyCompleted)); //설문조사 했음.
		}
		else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SignupResponse(false, false));
		}
	}
	
	
	@GetMapping("/userauth/logout")
	public ResponseEntity<Void> logout(HttpSession session) {
//		session.removeAttribute("user");
		session.invalidate(); // 세션 전체 무효화
		System.out.println("로그아웃되었습니다.");
	    return ResponseEntity.noContent().build(); // 204 No Content
	}
	
	//회원가입
	@PostMapping("/userauth/join")
	public ResponseEntity<Void> join(@RequestBody JoinRequest req ){
		
		TB_USER user = new TB_USER();
		user.setUserId(req.userID());
		user.setUserPw(req.userPassword());
		user.setUserNm(req.userName());
		user.setNickNm(req.nickName());
		user.setPregWeek(req.pregWeek());
		user.setBirthDt(req.birthDate());
		
		userService.join(user);
		
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/my")
	public List<List<Integer>> enterMyPage(HttpSession session) {
		TB_USER user = (TB_USER) session.getAttribute("user");
		List<List<Integer>> result = mainService.get7daysBlood(user);
		System.out.println("JVM Default TimeZone: " + TimeZone.getDefault().getID());
	    System.out.println("Current Time: " + LocalDateTime.now());
	    
		return result;
	}
	
	@PostMapping("/my/recipe")
	public ResponseEntity<List<RecipeTopDto>> enterMyRecipe(HttpSession session, @RequestBody EnterMyRecipeRequest req) {
		TB_USER user = (TB_USER) session.getAttribute("user");
		if(req.category().equals("trans")) {
//			List<RecipeTopDto> temp = recipeService.getTransList(user);
//			for (RecipeTopDto tmp : temp) System.out.println(tmp.getRcpId());
			return ResponseEntity.ok(recipeService.getTransList(user));
		}
		else {
//			List<RecipeTopDto> temp = recipeService.getLikeList(user);
//			for (RecipeTopDto tmp : temp) System.out.println(tmp.getRcpId());
			return ResponseEntity.ok(recipeService.getLikeList(user));
		}
	}
	
	
	// 개인정보수정 - 닉네임, 비번
	
	@PostMapping("/my/update/user")
	public ResponseEntity<Void> updateUser (@RequestBody UserUpdateRequest req, HttpSession session) {
		TB_USER user = (TB_USER) session.getAttribute("user");
		user.setUserPw(req.userPassword());
		user.setNickNm(req.nickName());
		user = userService.join(user);
		session.setAttribute("user", user);
		return ResponseEntity.noContent().build();
	}
}
