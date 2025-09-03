package com.inside.ddf.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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
	
	@GetMapping("/api/recipe/search")
	public ResponseEntity<List<RecipeTopDto>> searchRecipe(@RequestParam String query, HttpSession session) {
		TB_USER user = (TB_USER) session.getAttribute("user");
		return ResponseEntity.ok(recipeService.searchRecipe(user, query));
	}
	
//	 public void test() {
//		 String[] urls = {
//				  "https://www.10000recipe.com/recipe/6885961",
//				  "https://www.10000recipe.com/recipe/6885427",
//				  "https://www.10000recipe.com/recipe/6845333",
//				  "https://www.10000recipe.com/recipe/6886093",
//				  "https://www.10000recipe.com/recipe/6885665",
//				  "https://www.10000recipe.com/recipe/6964657",
//				  "https://www.10000recipe.com/recipe/6886133",
//				  "https://www.10000recipe.com/recipe/7013286",
//				  "https://www.10000recipe.com/recipe/6885973",
//				  "https://www.10000recipe.com/recipe/6886037",
//				  "https://www.10000recipe.com/recipe/6885574",
//				  "https://www.10000recipe.com/recipe/6885176",
//				  "https://www.10000recipe.com/recipe/6887081",
//				  "https://www.10000recipe.com/recipe/6885467",
//				  "https://www.10000recipe.com/recipe/6918004",
//				  "https://www.10000recipe.com/recipe/6921038",
//				  "https://www.10000recipe.com/recipe/6913419",
//				  "https://www.10000recipe.com/recipe/6886139",
//				  "https://www.10000recipe.com/recipe/6885394",
//				  "https://www.10000recipe.com/recipe/7044561",
//				  "https://www.10000recipe.com/recipe/6990471",
//				  "https://www.10000recipe.com/recipe/6885777",
//				  "https://www.10000recipe.com/recipe/6932890",
//				  "https://www.10000recipe.com/recipe/6980785",
//				  "https://www.10000recipe.com/recipe/6885899",
//				  "https://www.10000recipe.com/recipe/6885319",
//				  "https://www.10000recipe.com/recipe/6850878",
//				  "https://www.10000recipe.com/recipe/6970148",
//				  "https://www.10000recipe.com/recipe/6885429",
//				  "https://www.10000recipe.com/recipe/6885518",
//				  "https://www.10000recipe.com/recipe/6886116",
//				  "https://www.10000recipe.com/recipe/6885703",
//				  "https://www.10000recipe.com/recipe/6932688",
//				  "https://www.10000recipe.com/recipe/7013470",
//				  "https://www.10000recipe.com/recipe/6885478",
//				  "https://www.10000recipe.com/recipe/6885428",
//				  "https://www.10000recipe.com/recipe/6885608",
//				  "https://www.10000recipe.com/recipe/7016204",
//				  "https://www.10000recipe.com/recipe/6936687",
//				  "https://www.10000recipe.com/recipe/7016203",
//				  "https://www.10000recipe.com/recipe/6839266",
//				  "https://www.10000recipe.com/recipe/7013675",
//				  "https://www.10000recipe.com/recipe/1560257",
//				  "https://www.10000recipe.com/recipe/6276386",
//				  "https://www.10000recipe.com/recipe/6885303",
//				  "https://www.10000recipe.com/recipe/6942070",
//				  "https://www.10000recipe.com/recipe/2375007",
//				  "https://www.10000recipe.com/recipe/6873175",
//				  "https://www.10000recipe.com/recipe/7008865",
//				  "https://www.10000recipe.com/recipe/6966347",
//				  "https://www.10000recipe.com/recipe/6976341",
//				  "https://www.10000recipe.com/recipe/6976443",
//				  "https://www.10000recipe.com/recipe/6935329",
//				  "https://www.10000recipe.com/recipe/7026926",
//				  "https://www.10000recipe.com/recipe/6885804",
//				  "https://www.10000recipe.com/recipe/5229687",
//				  "https://www.10000recipe.com/recipe/6885866",
//				  "https://www.10000recipe.com/recipe/6932888",
//				  "https://www.10000recipe.com/recipe/6932889",
//				  "https://www.10000recipe.com/recipe/7015932",
//				  "https://www.10000recipe.com/recipe/6932784",
//				  "https://www.10000recipe.com/recipe/6885480",
//				  "https://www.10000recipe.com/recipe/6929013",
//				  "https://www.10000recipe.com/recipe/6532433",
//				  "https://www.10000recipe.com/recipe/6660947",
//				  "https://www.10000recipe.com/recipe/6876552",
//				  "https://www.10000recipe.com/recipe/6932687",
//				  "https://www.10000recipe.com/recipe/6921108",
//				  "https://www.10000recipe.com/recipe/5106409",
//				  "https://www.10000recipe.com/recipe/6835481",
//				  "https://www.10000recipe.com/recipe/6885450",
//				  "https://www.10000recipe.com/recipe/6885835",
//				  "https://www.10000recipe.com/recipe/6885845",
//				  "https://www.10000recipe.com/recipe/6885391",
//				  "https://www.10000recipe.com/recipe/7038854",
//				  "https://www.10000recipe.com/recipe/7002656",
//				  "https://www.10000recipe.com/recipe/6999660",
//				  "https://www.10000recipe.com/recipe/6976526",
//				  "https://www.10000recipe.com/recipe/7021237",
//				  "https://www.10000recipe.com/recipe/7044738",
//				  "https://www.10000recipe.com/recipe/6835481",
//				  "https://www.10000recipe.com/recipe/6932785",
//				  "https://www.10000recipe.com/recipe/6848172",
//				  "https://www.10000recipe.com/recipe/6988223",
//				  "https://www.10000recipe.com/recipe/7046841",
//				  "https://www.10000recipe.com/recipe/6885499",
//				  "https://www.10000recipe.com/recipe/6960778",
//				  "https://www.10000recipe.com/recipe/6196250",
//				  "https://www.10000recipe.com/recipe/6980863",
//				  "https://www.10000recipe.com/recipe/6885974",
//				  "https://www.10000recipe.com/recipe/6918324",
//				  "https://www.10000recipe.com/recipe/7050414",
//				  "https://www.10000recipe.com/recipe/7043143",
//				  "https://www.10000recipe.com/recipe/7013457",
//				  "https://www.10000recipe.com/recipe/7044915",
//				  "https://www.10000recipe.com/recipe/7039668",
//				  "https://www.10000recipe.com/recipe/7008194",
//				  "https://www.10000recipe.com/recipe/7015319",
//				  "https://www.10000recipe.com/recipe/7013671",
//				  "https://www.10000recipe.com/recipe/7031072",
//				  "https://www.10000recipe.com/recipe/5449252",
//				  "https://www.10000recipe.com/recipe/7013458",
//				  "https://www.10000recipe.com/recipe/7013461",
//				  "https://www.10000recipe.com/recipe/7003547",
//				  "https://www.10000recipe.com/recipe/6998667",
//				  "https://www.10000recipe.com/recipe/7042988",
//				  "https://www.10000recipe.com/recipe/6656558",
//				  "https://www.10000recipe.com/recipe/7013465",
//				  "https://www.10000recipe.com/recipe/7049796",
//				  "https://www.10000recipe.com/recipe/6885566",
//				  "https://www.10000recipe.com/recipe/6975399",
//				  "https://www.10000recipe.com/recipe/7015885",
//				  "https://www.10000recipe.com/recipe/6551171",
//				  "https://www.10000recipe.com/recipe/5449335",
//				  "https://www.10000recipe.com/recipe/7025653",
//				  "https://www.10000recipe.com/recipe/7016664",
//				  "https://www.10000recipe.com/recipe/7013459",
//				  "https://www.10000recipe.com/recipe/7014870",
//				  "https://www.10000recipe.com/recipe/7039206",
//				  "https://www.10000recipe.com/recipe/6964199",
//				  "https://www.10000recipe.com/recipe/6976412",
//				  "https://www.10000recipe.com/recipe/7041699",
//				  "https://www.10000recipe.com/recipe/7013860",
//				  "https://www.10000recipe.com/recipe/7013468",
//				  "https://www.10000recipe.com/recipe/7013583",
//				  "https://www.10000recipe.com/recipe/7013667",
//				  "https://www.10000recipe.com/recipe/7014341",
//				  "https://www.10000recipe.com/recipe/6976312",
//				  "https://www.10000recipe.com/recipe/6964199",
//				  "https://www.10000recipe.com/recipe/6981223",
//				  "https://www.10000recipe.com/recipe/6655376",
//				  "https://www.10000recipe.com/recipe/6872909",
//				  "https://www.10000recipe.com/recipe/7015930",
//				  "https://www.10000recipe.com/recipe/7015971",
//				  "https://www.10000recipe.com/recipe/7016639",
//				  "https://www.10000recipe.com/recipe/7042192",
//				  "https://www.10000recipe.com/recipe/7013574",
//				  "https://www.10000recipe.com/recipe/7013668",
//				  "https://www.10000recipe.com/recipe/7049795",
//				  "https://www.10000recipe.com/recipe/6864930",
//				  "https://www.10000recipe.com/recipe/7015320",
//				  "https://www.10000recipe.com/recipe/7015974",
//				  "https://www.10000recipe.com/recipe/7016659",
//				  "https://www.10000recipe.com/recipe/7016665",
//				  "https://www.10000recipe.com/recipe/7013591",
//				  "https://www.10000recipe.com/recipe/7013862",
//				  "https://www.10000recipe.com/recipe/7002777",
//				  "https://www.10000recipe.com/recipe/7047177",
//				  "https://www.10000recipe.com/recipe/6885806",
//				  "https://www.10000recipe.com/recipe/6999738",
//				  "https://www.10000recipe.com/recipe/7016667",
//				  "https://www.10000recipe.com/recipe/7014842",
//				  "https://www.10000recipe.com/recipe/7007805",
//				  "https://www.10000recipe.com/recipe/7013594",
//				  "https://www.10000recipe.com/recipe/6980626",
//				  "https://www.10000recipe.com/recipe/7013467",
//				  "https://www.10000recipe.com/recipe/7013584",
//				  "https://www.10000recipe.com/recipe/7016668",
//				  "https://www.10000recipe.com/recipe/7016669",
//				  "https://www.10000recipe.com/recipe/7047453",
//				  "https://www.10000recipe.com/recipe/7013593",
//				  "https://www.10000recipe.com/recipe/7013676",
//				  "https://www.10000recipe.com/recipe/7014348",
//				  "https://www.10000recipe.com/recipe/7040257",
//				  "https://www.10000recipe.com/recipe/6885369",
//				  "https://www.10000recipe.com/recipe/6885769",
//				  "https://www.10000recipe.com/recipe/6980837",
//				  "https://www.10000recipe.com/recipe/7013586",
//				  "https://www.10000recipe.com/recipe/7016393",
//				  "https://www.10000recipe.com/recipe/6980626",
//				  "https://www.10000recipe.com/recipe/7013467",
//				  "https://www.10000recipe.com/recipe/7013584",
//				  "https://www.10000recipe.com/recipe/7016668",
//				  "https://www.10000recipe.com/recipe/7016669",
//				  "https://www.10000recipe.com/recipe/7047453",
//				  "https://www.10000recipe.com/recipe/7047484",
//				  "https://www.10000recipe.com/recipe/6990857",
//				  "https://www.10000recipe.com/recipe/6973543",
//				  "https://www.10000recipe.com/recipe/7015973",
//				  "https://www.10000recipe.com/recipe/7013855",
//				  "https://www.10000recipe.com/recipe/4939003",
//				  "https://www.10000recipe.com/recipe/6929103",
//				  "https://www.10000recipe.com/recipe/7015886",
//				  "https://www.10000recipe.com/recipe/6885840",
//				  "https://www.10000recipe.com/recipe/7015359",
//				  "https://www.10000recipe.com/recipe/7015922",
//				  "https://www.10000recipe.com/recipe/7015925",
//				  "https://www.10000recipe.com/recipe/7015928",
//				  "https://www.10000recipe.com/recipe/7013466",
//				  "https://www.10000recipe.com/recipe/6990856",
//				  "https://www.10000recipe.com/recipe/7013858",
//				  "https://www.10000recipe.com/recipe/7014339",
//				  "https://www.10000recipe.com/recipe/7015357",
//				  "https://www.10000recipe.com/recipe/7015920",
//				  "https://www.10000recipe.com/recipe/7043993",
//				  "https://www.10000recipe.com/recipe/6980345",
//				  "https://www.10000recipe.com/recipe/7040187",
//				  "https://www.10000recipe.com/recipe/7040575",
//				  "https://www.10000recipe.com/recipe/7013852",
//				  "https://www.10000recipe.com/recipe/7013854",
//				  "https://www.10000recipe.com/recipe/7014339",
//				  "https://www.10000recipe.com/recipe/7043993",
//				  "https://www.10000recipe.com/recipe/5152968",
//				  "https://www.10000recipe.com/recipe/6885902",
//				  "https://www.10000recipe.com/recipe/7016202",
//				  "https://www.10000recipe.com/recipe/7013673",
//				  "https://www.10000recipe.com/recipe/7040718",
//				  "https://www.10000recipe.com/recipe/6996098",
//				  "https://www.10000recipe.com/recipe/7013579",
//				  "https://www.10000recipe.com/recipe/7014340",
//				  "https://www.10000recipe.com/recipe/7024815",
//				  "https://www.10000recipe.com/recipe/7014839",
//				  "https://www.10000recipe.com/recipe/7014871",
//				  "https://www.10000recipe.com/recipe/7016396",
//				  "https://www.10000recipe.com/recipe/7015927",
//				  "https://www.10000recipe.com/recipe/7024230",
//				  "https://www.10000recipe.com/recipe/7013857",
//				  "https://www.10000recipe.com/recipe/6885491",
//				  "https://www.10000recipe.com/recipe/6975262",
//				  "https://www.10000recipe.com/recipe/6977749",
//				  "https://www.10000recipe.com/recipe/7011220",
//				  "https://www.10000recipe.com/recipe/7033612",
//				  "https://www.10000recipe.com/recipe/7013589",
//				  "https://www.10000recipe.com/recipe/7015929",
//				  "https://www.10000recipe.com/recipe/6849699",
//				  "https://www.10000recipe.com/recipe/7014342",
//				  "https://www.10000recipe.com/recipe/7015361",
//				  "https://www.10000recipe.com/recipe/7046171",
//				  "https://www.10000recipe.com/recipe/7044399",
//				  "https://www.10000recipe.com/recipe/7014869",
//				  "https://www.10000recipe.com/recipe/7015889",
//				  "https://www.10000recipe.com/recipe/5151847",
//				  "https://www.10000recipe.com/recipe/6995877",
//				  "https://www.10000recipe.com/recipe/7042765",
//				  "https://www.10000recipe.com/recipe/7014352",
//				  "https://www.10000recipe.com/recipe/6948610",
//				  "https://www.10000recipe.com/recipe/7015887",
//				  "https://www.10000recipe.com/recipe/6977950",
//				  "https://www.10000recipe.com/recipe/7014345",
//				  "https://www.10000recipe.com/recipe/7001518",
//				  "https://www.10000recipe.com/recipe/7039421",
//				  "https://www.10000recipe.com/recipe/6885684",
//				  "https://www.10000recipe.com/recipe/6936582",
//				  "https://www.10000recipe.com/recipe/7033986",
//				  "https://www.10000recipe.com/recipe/6840727",
//				  "https://www.10000recipe.com/recipe/2300444",
//				  "https://www.10000recipe.com/recipe/7003627",
//				  "https://www.10000recipe.com/recipe/7014548"
//				};
//		 TB_USER admin = userService.findById("admin").get();
//		 RecipeReq req = new RecipeReq();
//		 for (int i=22;i<urls.length;i++) {
//			 req.setUrl(urls[i]);
//			 req.setUser_type("F");
//			 req.setAllergies(List.of(""));
//			 RecipeRes res = recipeService.getConvertRecipe(req);
//	    	 recipeService.test(admin,res,i);
//		 }
//    }
}
