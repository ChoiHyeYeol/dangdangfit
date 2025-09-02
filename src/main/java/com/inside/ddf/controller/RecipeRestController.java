package com.inside.ddf.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inside.ddf.code.CategoryMenu;
import com.inside.ddf.code.CategoryTime;
import com.inside.ddf.dto.frontend.RecipeDetailDto;
import com.inside.ddf.dto.frontend.RecipeDetailDto.IngredientDto;
import com.inside.ddf.dto.frontend.RecipeDetailDto.SauceDto;
import com.inside.ddf.dto.frontend.RecipeDetailDto.StepDto;
import com.inside.ddf.dto.frontend.RecipeTopDto;
import com.inside.ddf.dto.req.RecipeReq;
import com.inside.ddf.dto.res.RecipeRes;
import com.inside.ddf.entity.TB_RECIPE;
import com.inside.ddf.entity.TB_SR_RESP;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.service.RecipeService;
import com.inside.ddf.service.SurveyService;
import com.inside.ddf.service.UserService;

import jakarta.servlet.http.HttpSession;

record RecipeRequest(String rcp_Id) {
};

record ConvertRecipeRequest(String url) {
};

@RestController
public class RecipeRestController {

	@Autowired
	RecipeService recipeService;

	@Autowired
	UserService userService;

	@Autowired
	SurveyService surveyService;

	@GetMapping("update/recipe")
	public void updateRecipe() {
		Optional<TB_USER> admin = userService.findById("admin");
		recipeService.updateRecipeByAdmin(admin.get());
	}

	@GetMapping("update/food")
	public void updateFood() {
		String excelPath = "C:\\Users\\dreac\\Desktop\\DevStudy\\fastapi\\ddf\\app\\data\\diet\\filtered_gdm_foods.xlsx";
		try {
			recipeService.importFromPath(excelPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 상세레시피
	@GetMapping("/api/recipe/detail")
	public ResponseEntity<RecipeDetailDto> detail(@RequestParam String rcp_id, HttpSession session) {

		String userId = ((TB_USER) session.getAttribute("user")).getUserId();

		TB_RECIPE recipe = recipeService.getRecipe(rcp_id).get();

		// 재료 여러개기 때문에 list 사용
		List<IngredientDto> recipe_ingr = recipeService.getRecipeIngr(rcp_id).stream()
				.map(i -> new IngredientDto(i.getIngrName(), i.getIngrCnt())).toList();

		List<SauceDto> recipe_sauce = recipeService.getRecipeSauce(rcp_id).stream()
				.map(s -> new SauceDto(s.getSauceName(), s.getSauceCnt())).toList();

		List<StepDto> recipe_step = recipeService.getRecipeStep(rcp_id).stream()
				.map(s -> new StepDto(s.getStepOrd(), s.getStepCont(), s.getStepImg())).toList();

		boolean islike = recipeService.isLiked(rcp_id, userId);

		Integer likecount = recipeService.likeCount(rcp_id);

		RecipeDetailDto dto = RecipeDetailDto.builder().rcpNm(recipe.getRcpNm()).mainImg(recipe.getMainImg())
				.level(recipe.getLevel()).portion(recipe.getPortion()).time(recipe.getTime()).ingredients(recipe_ingr)
				.sauces(recipe_sauce).steps(recipe_step).liked(islike).likeCount(likecount).build();

		return ResponseEntity.ok(dto);
	}

	// 레시피찜
	@PostMapping("/api/recipe/like")
	public ResponseEntity<Map<String, Object>> recipelike(@RequestBody RecipeRequest req, HttpSession session) {

		String userId = ((TB_USER) session.getAttribute("user")).getUserId();

		recipeService.recipeLike(req.rcp_Id(), userId);

		boolean islike = recipeService.isLiked(req.rcp_Id(), userId);

		Integer likecount = recipeService.likeCount(req.rcp_Id());

		System.out.println("누른 후 좋아요 여부: " + islike);
		System.out.println("누른 후 좋아요 개수: " + likecount);

		// JSON 응답으로 내려줄 데이터
		Map<String, Object> result = new HashMap<>();
		result.put("liked", islike);
		result.put("likeCount", likecount);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/api/recipe/top5")
	public ResponseEntity<List<RecipeTopDto>> getTop5(HttpSession session) {

		String userId = ((TB_USER) session.getAttribute("user")).getUserId();

		List<RecipeTopDto> top5 = recipeService.getTopNRecipes(userId, 5);

		return ResponseEntity.ok(top5);

	}

	@GetMapping("/api/recipe/subList")
	public ResponseEntity<List<RecipeTopDto>> getSubList(@RequestParam String recipeMethod,
			@RequestParam String recipeCategory, HttpSession session) {
		TB_USER user = ((TB_USER) session.getAttribute("user"));
		CategoryMenu[] menuList = { CategoryMenu.S, CategoryMenu.G, CategoryMenu.B, CategoryMenu.M, CategoryMenu.P,
				CategoryMenu.N, CategoryMenu.K, CategoryMenu.O };
		CategoryTime[] timeList = { CategoryTime.B, CategoryTime.L, CategoryTime.D, CategoryTime.S };

		if (recipeMethod.equals("time")) {
			String[] temp = { "아침", "점심", "저녁", "간식" };
			for (int i = 0; i < temp.length; i++) {
				if (recipeCategory.equals(temp[i])) {
					List<RecipeTopDto> result = recipeService.getSubList(user.getUserId(), timeList[i]);
					return ResponseEntity.ok(result); // 레시피 검색
				}
			}
		} else {
			String[] temp = { "양념", "구이", "국물", "무침", "절임", "면/밥", "제과제빵", "기타" };
			for (int i = 0; i < temp.length; i++) {
				if (recipeCategory.equals(temp[i])) {
					List<RecipeTopDto> result = recipeService.getSubList(user.getUserId(), menuList[i]);
					return ResponseEntity.ok(result); // 레시피 검색
				}
			}
		}
		return ResponseEntity.noContent().build();
	}

	// 변환 레시피

	@PostMapping("/api/model/recipe/convert_recipe")
	public RecipeDetailDto convertRecipe(@RequestBody ConvertRecipeRequest link, HttpSession session) {
		TB_USER user = (TB_USER) session.getAttribute("user");
		RecipeReq req = new RecipeReq();
		req.setAllergies(surveyService.getAllergies(user));
		req.setUrl(link.url());
		req.setUser_type(user.getUserType().toString());
		RecipeRes res = recipeService.getConvertRecipe(req);
//    	for(Map<String, String> item : res.getFood().getStepList()) System.out.println(item.keySet());
		TB_RECIPE recipe = recipeService.updateRecipeByUser(user, res);
		// 재료 여러개기 때문에 list 사용
		List<IngredientDto> recipe_ingr = recipeService.getRecipeIngr(recipe.getRcpId()).stream()
				.map(i -> new IngredientDto(i.getIngrName(), i.getIngrCnt())).toList();

		List<SauceDto> recipe_sauce = recipeService.getRecipeSauce(recipe.getRcpId()).stream()
				.map(s -> new SauceDto(s.getSauceName(), s.getSauceCnt())).toList();

		List<StepDto> recipe_step = recipeService.getRecipeStep(recipe.getRcpId()).stream()
				.map(s -> new StepDto(s.getStepOrd(), s.getStepCont(), s.getStepImg())).toList();

		RecipeDetailDto dto = RecipeDetailDto.builder().rcpNm(recipe.getRcpNm()).mainImg(recipe.getMainImg())
				.level(recipe.getLevel()).portion(recipe.getPortion()).time(recipe.getTime()).ingredients(recipe_ingr)
				.sauces(recipe_sauce).steps(recipe_step).build();
		return dto;
	}

	@PostMapping("/api/recipe/delete")
	public ResponseEntity<Void> deleteRecipe(@RequestBody RecipeRequest recipe) {
		if (recipeService.deleteRecipe(recipe.rcp_Id()))
			System.out.println("삭제되었습니다.");
		return ResponseEntity.noContent().build();
	}
}
