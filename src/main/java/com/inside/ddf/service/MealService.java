package com.inside.ddf.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inside.ddf.code.CategoryTime;
import com.inside.ddf.dto.MealUpdateGet;
import com.inside.ddf.dto.frontend.EnterMealDTO;
import com.inside.ddf.dto.frontend.MainRecipeDTO;
import com.inside.ddf.dto.req.MealReq;
import com.inside.ddf.dto.req.OneMealReq;
import com.inside.ddf.dto.res.MealRes;
import com.inside.ddf.dto.res.OneMealRes;
import com.inside.ddf.entity.TB_FOOD;
import com.inside.ddf.entity.TB_MEAL_ITEM;
import com.inside.ddf.entity.TB_MEAL_PLAN;
import com.inside.ddf.entity.TB_RECIPE;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.repository.Rep_FOOD;
import com.inside.ddf.repository.Rep_MEAL_ITEM;
import com.inside.ddf.repository.Rep_MEAL_PLAN;
import com.inside.ddf.repository.Rep_RECIPE;
import com.inside.ddf.repository.Rep_USER;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealService {

	private final WebClient fastApiClient;

	@Autowired
	Rep_RECIPE rep_rcp;
	
	@Autowired
	Rep_FOOD rep_food;
	
	@Autowired
	Rep_MEAL_PLAN rep_meal;
	
	@Autowired
	Rep_MEAL_ITEM rep_item;
	
	@Autowired
	Rep_USER rep_user;
	

	CategoryTime[] mealType = {CategoryTime.B,CategoryTime.L,CategoryTime.D,CategoryTime.S};
	
	public void addAllUserMeal() {
		List<TB_USER> userList = rep_user.findAll();
		for(int i=0;i<userList.size();i++) {
			TB_USER user = userList.get(i);
			MealReq req = new MealReq();
	    	List<String> allergies = List.of("");
	    	List<String> preferences = List.of("");
	    	req.setAllergies(allergies);
	    	req.setPreferences(preferences);
	    	if (user.getUserType() == null) continue; // 설문조사 완료하지 않은 상태에서 12시가 되면
	    	req.setUser_type(user.getUserType().toString());
	    	addMeal(req,user);
		}
		
	}
	
	public void addMeal(MealReq req ,TB_USER user){
    	MealRes res =  fastApiClient.post()
                .uri("/api/model/diet/weekly")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(MealRes.class)
                .block(); // 간단히 block(), 서비스 내부에서 reactive 유지도 가능
    	System.out.println(res.toString());
    	LocalDate date = LocalDate.now();
    	
    	// 해당 기능을 사용하는 시점이 일요일에 서버가 자체적으로 돌아갈 때 + 사용자가 막 가입했을 때이다.
    	for (int day=0;day<res.getPlan().size() -(date.getDayOfWeek().getValue()%7) ;day++) {
    		for(int time=0;time<res.getPlan().get(day).size();time++) {
    	    	TB_MEAL_PLAN mp = new TB_MEAL_PLAN();
    	    	mp.setUser(user);
    	    	mp.setMealDate(date.plusDays(day)); 
    	    	CategoryTime[] mealType = {CategoryTime.B,CategoryTime.L,CategoryTime.D,CategoryTime.S};
    	    	mp.setMealType(mealType[time]);
    	    	
    	    	mp = rep_meal.save(mp);
    			for(int item=0;item<res.getPlan().get(day).get(time).size();item++) {
    				String content = res.getPlan().get(day).get(time).get(item);
//    				System.out.println();
    				if(content.startsWith("food")) {
    					try {
    						TB_RECIPE recipe = rep_rcp.findById(content).get();
    						
    						TB_MEAL_ITEM entity = new TB_MEAL_ITEM();
    						entity.setMeal(mp);
    						entity.setRecipe(recipe);
    						
    						rep_item.save(entity);
    					}catch(Exception e) {
        					System.out.print(content);
    					}
    				}
    				else {
    					TB_FOOD food = rep_food.findById(content).get();
    					TB_MEAL_ITEM entity = new TB_MEAL_ITEM();
						entity.setMeal(mp);
						entity.setFood(food);
						rep_item.save(entity);
    				}
    					
    			}
    		}
    	}
//    	return res;
    }
	
	private List<List<String>> showMeal(List<TB_MEAL_ITEM> mealList) {

		List<List<String>> result = new ArrayList<>();
		
		List<TB_MEAL_ITEM> list = mealList;
		for (TB_MEAL_ITEM it : list) {
		    if (it == null) continue;

		    if (it.getRecipe() != null) {
		        // 연관객체에서 바로 사용 (LAZY 초기화 → 트랜잭션 내에서)
		    	List<String> temp = List.of(it.getRecipe().getRcpNm(),it.getRecipe().getRcpId(),it.getRecipe().getCategoryMenu().toString());
		        result.add(temp);
		    } else if (it.getFood() != null) {
		    	List<String> temp = List.of(it.getFood().getFoodNm(),it.getFood().getFoodId(),"X");
		        result.add(temp);
		    } else {
		        // DB @Check 때문에 보통 오면 안 됨
		        System.out.println("둘 다 null입니다. 데이터 이상");
		    }
		}
		return result;
	}
	
	
	
	public List<List<String>> getMeal(TB_USER user, int time, LocalDate mealDate) {
		TB_MEAL_PLAN mp = rep_meal.findByUserAndMealTypeAndMealDate(user, mealType[time], mealDate);
		return showMeal(rep_item.findAllByMeal(mp));
	}
	public EnterMealDTO enterMeal(LocalDate day, TB_USER user, int time) {

    	EnterMealDTO result = new EnterMealDTO();
    	result.setDate(day);
    	result.setDDay(Period.between(user.getCreateDt(), day).getDays());
    	result.setNickName(user.getNickNm());
    	result.setMainMeal(getMeal(user,time,day));
    	result.setDessertMeal(getMeal(user,3,day));
    	return result;
	}
	
	
	public List<List<String>> refreshMeal(MealUpdateGet meal ,TB_USER user){
		OneMealReq req = new OneMealReq();
		int oneTime = meal.getOneTime();
		req.setOneTime(oneTime); // FE에서 아침 점심 저녁 0 1 2 에 매칭.
		
		int oneDay = meal.getDate().getDayOfWeek().getValue()%7;
		req.setOneDay(oneDay);
		
		List<String> allergies = List.of(""); // 사용자 설문조사 응답을 받을 예정.
		req.setAllergies(allergies);
		List<String> preferences = List.of(""); // 사용자 설문조사 응답을 받을 예정.
		req.setPreferences(preferences);
		req.setUser_type(user.getUserType().toString());
		
		
		
    	OneMealRes res =  fastApiClient.post()
                .uri("/api/model/diet/one")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(OneMealRes.class)
                .block(); // 간단히 block(), 서비스 내부에서 reactive 유지도 가능
    	System.out.println(res.toString());

    	
    	TB_MEAL_PLAN mp = rep_meal.findByUserAndMealTypeAndMealDate(user, mealType[oneTime], meal.getDate());
    	System.out.println(mp.getMealId());
    	
    	long cnt = rep_item.deleteAllByMeal(mp);
    	System.out.printf("%d 건 삭제가 완료되었습니다.\n",cnt);
		for(int item=0;item<res.getPlan().size();item++) {
			String content = res.getPlan().get(item);
//    				System.out.println();
			if(content.startsWith("food")) {
				
				TB_RECIPE recipe = rep_rcp.findById(content).get();
				TB_MEAL_ITEM entity = new TB_MEAL_ITEM();
				entity.setMeal(mp);
				entity.setRecipe(recipe);
				
				rep_item.save(entity);
				
			}
			else {
				TB_FOOD food = rep_food.findById(content).get();
				TB_MEAL_ITEM entity = new TB_MEAL_ITEM();
				entity.setMeal(mp);
				entity.setFood(food);
				rep_item.save(entity);
			}
		}
    		// 리턴 값은 식단 보여주기와 같은 방식으로.
    	return showMeal(rep_item.findAllByMeal(mp));
    }
	
	
	//메인 화면에서 식단 보여주기 -> 일단 첫번째 레시피로만.
	public MainRecipeDTO getMainRecipe(TB_USER user, int time, LocalDate mealDate) {
		MainRecipeDTO result = new MainRecipeDTO();
		TB_MEAL_PLAN mp = rep_meal.findByUserAndMealTypeAndMealDate(user, mealType[time], mealDate);

		List<TB_MEAL_ITEM> list = rep_item.findAllByMeal(mp);
		
		for (TB_MEAL_ITEM it : list) {
		    if (it == null) continue;

		    if (it.getRecipe() != null) {
		        // 연관객체에서 바로 사용 (LAZY 초기화 → 트랜잭션 내에서)
		    	
		    	TB_RECIPE target = it.getRecipe();
		    	result.setLevel(target.getLevel());
		    	result.setMainImg(target.getMainImg());
		    	result.setPortion(target.getPortion());
		    	result.setRcpId(target.getRcpId());
		    	result.setRcpNm(target.getRcpNm());
		    	result.setTime(target.getTime());
		    	return result;
		    } else if (it.getFood() != null) {
		    	continue;
		       
		    } else {
		        // DB @Check 때문에 보통 오면 안 됨
		        System.out.println("둘 다 null입니다. 데이터 이상");
		    }
		}
		if (list.size()>0) {
			TB_FOOD target = list.get(0).getFood();
	    	result.setRcpId(target.getFoodId());
	    	result.setRcpNm(target.getFoodNm());
	    	return result; //없으면 음식이라도.
		}
		else {
			return result; //음식도 없으면 없는 채로.
		}
	}
}
