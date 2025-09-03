package com.inside.ddf.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.inside.ddf.code.CategoryMenu;
import com.inside.ddf.code.CategoryTime;
import com.inside.ddf.controller.MainController;
import com.inside.ddf.dto.RecipeJsonDto;
import com.inside.ddf.dto.frontend.RecipeTopDto;
import com.inside.ddf.dto.req.RecipeReq;
import com.inside.ddf.dto.res.ChatRes;
import com.inside.ddf.dto.res.RecipeRes;
import com.inside.ddf.entity.TB_FOOD;
import com.inside.ddf.entity.TB_RECIPE;
import com.inside.ddf.entity.TB_RECIPE_INGR;
import com.inside.ddf.entity.TB_RECIPE_LIKEY;
import com.inside.ddf.entity.TB_RECIPE_SAUCE;
import com.inside.ddf.entity.TB_RECIPE_STEP;
import com.inside.ddf.entity.TB_USER;
import com.inside.ddf.repository.Rep_FOOD;
import com.inside.ddf.repository.Rep_RECIPE;
import com.inside.ddf.repository.Rep_RECIPE_INGR;
import com.inside.ddf.repository.Rep_RECIPE_LIKEY;
import com.inside.ddf.repository.Rep_RECIPE_SAUCE;
import com.inside.ddf.repository.Rep_RECIPE_STEP;
import com.inside.ddf.repository.Rep_USER;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecipeService {

	private final WebClient fastApiClient;

	private final MainController mainController;

	@Autowired
	Rep_USER rep_user;

	@Autowired
	Rep_RECIPE rep_rcp;

	@Autowired
	Rep_RECIPE_INGR rep_ingr;

	@Autowired
	Rep_RECIPE_SAUCE rep_sauce;

	@Autowired
	Rep_RECIPE_STEP rep_step;

	@Autowired
	Rep_FOOD rep_food;

	@Autowired
	Rep_RECIPE_LIKEY rep_likey; // 추가
	private final ObjectMapper objectMapper = new ObjectMapper();

	// 레시피 불러오기
	public Optional<TB_RECIPE> getRecipe(String rcp_id) {
		return rep_rcp.findById(rcp_id);
	}

	// 재료 불러오기
	public List<TB_RECIPE_INGR> getRecipeIngr(String rcp_id) {
		return rep_ingr.findAllByRecipe_RcpId(rcp_id);
	}

	public List<TB_RECIPE_SAUCE> getRecipeSauce(String rcp_id) {

		return rep_sauce.findAllByRecipe_RcpId(rcp_id);
	}

	public List<TB_RECIPE_STEP> getRecipeStep(String rcp_id) {

		return rep_step.findAllByRecipe_RcpId(rcp_id);
	}

	public boolean isLiked(String rcp_id, String userId) {
		return rep_likey.existsByRecipe_RcpIdAndUser_UserId(rcp_id, userId);
	}

	public Integer likeCount(String rcp_id) {
		return rep_likey.countByRecipe_RcpId(rcp_id);
	}

	@Transactional
	public void recipeLike(String rcp_id, String userId) {

		if (rep_likey.existsByRecipe_RcpIdAndUser_UserId(rcp_id, userId)) {
			rep_likey.deleteByRecipe_RcpIdAndUser_UserId(rcp_id, userId);
			System.out.println("좋아요 취소됨");
		} else {
			TB_RECIPE_LIKEY like = new TB_RECIPE_LIKEY();
			like.setRecipe(rep_rcp.findById(rcp_id).get()); // rcp_id, userid들은 일반 변수가 아니라 자기 테이블에서 가져와야함
			like.setUser(rep_user.findById(userId).get());
			rep_likey.save(like);
			System.out.println("좋아요 추가됨");
		}
	}

	public List<RecipeTopDto> getTopNRecipes(String userId, int rank) {

		// 1) 좋아요 개수 기준 TOP5 가져오기
		List<Object[]> results = rep_likey.findTopLikedRecipes(PageRequest.of(0, rank));

		List<RecipeTopDto> top5 = new ArrayList<>();

		for (Object[] row : results) {
			String rcpId = (String) row[0];

			TB_RECIPE recipe = rep_rcp.findById(rcpId).get();
			boolean liked = rep_likey.existsByRecipe_RcpIdAndUser_UserId(rcpId, userId);
			int likecount = rep_likey.countByRecipe_RcpId(rcpId);

			top5.add(new RecipeTopDto(recipe.getRcpId(), recipe.getRcpNm(), recipe.getMainImg(), recipe.getTime(),
					recipe.getPortion(), recipe.getLevel(), liked, likecount));
		}

		return top5;
	}

	public List<RecipeTopDto> getListRecipeTopDto(String userId, List<TB_RECIPE> lst) {
		List<TB_RECIPE> list = lst;

		List<RecipeTopDto> result = new ArrayList<>();

		for (TB_RECIPE row : list) {
			String rcpId = row.getRcpId();
			TB_RECIPE recipe = rep_rcp.findById(rcpId).get();
			boolean liked = rep_likey.existsByRecipe_RcpIdAndUser_UserId(rcpId, userId);
			int likecount = rep_likey.countByRecipe_RcpId(rcpId);
			int index = 0;
			for (int i = 0; i < result.size(); i++) {
				if (result.get(i).getLikecount() > likecount) { // 현재 값(좋아요 수)이 비교 값보다 작으면
					index++;
				}
			}
			result.add(index, new RecipeTopDto(recipe.getRcpId(), recipe.getRcpNm(), recipe.getMainImg(),
					recipe.getTime(), recipe.getPortion(), recipe.getLevel(), liked, likecount));
		}

		return result;
	}

	public List<RecipeTopDto> getSubList(String userId, CategoryMenu menu) {
		return getListRecipeTopDto(userId, rep_rcp.findAllByCategoryMenu(menu));
	}

	public List<RecipeTopDto> getSubList(String userId, CategoryTime time) {
		return getListRecipeTopDto(userId, rep_rcp.findAllByCategoryTime(time));
	}

	public List<RecipeTopDto> getTransList(TB_USER user) {
		return getListRecipeTopDto(user.getUserId(), rep_rcp.findAllByUser(user));
	}

	public List<RecipeTopDto> getLikeList(TB_USER user) {
		List<TB_RECIPE_LIKEY> likeyList = rep_likey.findAllByUser(user);
		List<TB_RECIPE> list = new ArrayList<>();
		for (int i = 0; i < likeyList.size(); i++) {
			list.add(likeyList.get(i).getRecipe());
		}
		return getListRecipeTopDto(user.getUserId(), list);
	}

	public RecipeRes getConvertRecipe(RecipeReq req) {
		RecipeRes res = fastApiClient.post().uri("/api/model/recipe/convert_recipe").bodyValue(req).retrieve()
				.bodyToMono(RecipeRes.class).block();
		return res;
	}
	
	public void test(TB_USER user, RecipeRes res,int idx) {
		updateRecipeByUser(user,res,idx);
	}

	public TB_RECIPE updateRecipeByUser(TB_USER user, RecipeRes res) {

		RecipeRes.Food detail = res.getFood();
		// 1. 레시피 저장
		TB_RECIPE recipe = new TB_RECIPE();
		recipe.setMainImg(detail.getMainImg());
		recipe.setRcpId(user.getUserId()+"_"+UUID.randomUUID().toString());
		recipe.setRcpNm(detail.getTitle());
		if (detail.getTitle().length() == 0)
			return recipe;
		recipe.setUser(user);
		recipe.setDescTxt(detail.getSummary());
		recipe.setPortion(detail.getPortion());
		recipe.setTime(detail.getTime());
		recipe.setLevel(detail.getLevel());
		recipe.setTip(detail.getTip());
		
		recipe.setChVal(res.getChVal());
		recipe.setFatVal(res.getFatVal());
		recipe.setGiVal(res.getGiVal());
		recipe.setIcVal(res.getIcVal());
		recipe.setPrVal(res.getPrVal());
		
		
		recipe = rep_rcp.save(recipe);

		// 2. 소스 저장.
		// 양쪽의 { } 제거

		Map <String,String> sauceMap = detail.getSauce();
		if (!sauceMap.isEmpty()) {

			for (String key : sauceMap.keySet()) {
				TB_RECIPE_SAUCE sauce = new TB_RECIPE_SAUCE();
				
				sauce.setSauceName(key);
				sauce.setSauceCnt(sauceMap.get(key));
				sauce.setRecipe(recipe);
				System.out.println("key=" + key + ", value=" + sauceMap.get(key));
				rep_sauce.save(sauce);
			}
		}

		// 3. 재료 저장.
		// 양쪽의 { } 제거
		Map <String,String> ingrMap = detail.getIngredient();
		if (!ingrMap.isEmpty()) {
			for (String key : ingrMap.keySet()) {
				TB_RECIPE_INGR ingr = new TB_RECIPE_INGR();
				
				ingr.setIngrName(key);
				ingr.setIngrCnt(ingrMap.get(key));
				ingr.setRecipe(recipe);
				System.out.println("key=" + key + ", value=" + ingrMap.get(key));
				rep_ingr.save(ingr);
			}
		}
		
		// 4. 스텝 저장.
		List<Map<String, String>> stepMapList = detail.getStepList();
		if (!stepMapList.isEmpty()) {
			for(int i=0;i<stepMapList.size();i++) {
				Map <String,String> stepMap = stepMapList.get(i);
				if (!stepMap.isEmpty()) {
					for (String key : stepMap.keySet()) {
						TB_RECIPE_STEP step = new TB_RECIPE_STEP();
						step.setStepOrd(i+1);
						step.setStepCont(key);
						step.setStepImg(stepMap.get(key));
						step.setRecipe(recipe);
						System.out.println("key=" + key + ", value=" + stepMap.get(key));
						rep_step.save(step);
					}
				}
			}
			
		}
		
		return recipe;
	}

	public TB_RECIPE updateRecipeByUser(TB_USER user, RecipeRes res, int idx) {

		RecipeRes.Food detail = res.getFood();
		// 1. 레시피 저장
		TB_RECIPE recipe = new TB_RECIPE();
		recipe.setMainImg(detail.getMainImg());
		recipe.setRcpId("food"+idx+".json");
		recipe.setRcpNm(detail.getTitle());
		if (detail.getTitle().length() == 0)
			return recipe;
		recipe.setUser(user);
		recipe.setDescTxt(detail.getSummary());
		recipe.setPortion(detail.getPortion());
		recipe.setTime(detail.getTime());
		recipe.setLevel(detail.getLevel());
		recipe.setTip(detail.getTip());
		
		recipe.setChVal(res.getChVal());
		recipe.setFatVal(res.getFatVal());
		recipe.setGiVal(res.getGiVal());
		recipe.setIcVal(res.getIcVal());
		recipe.setPrVal(res.getPrVal());
		
		
		recipe = rep_rcp.save(recipe);

		// 2. 소스 저장.
		// 양쪽의 { } 제거

		Map <String,String> sauceMap = detail.getSauce();
		if (!sauceMap.isEmpty()) {

			for (String key : sauceMap.keySet()) {
				TB_RECIPE_SAUCE sauce = new TB_RECIPE_SAUCE();
				
				sauce.setSauceName(key);
				sauce.setSauceCnt(sauceMap.get(key));
				sauce.setRecipe(recipe);
				System.out.println("key=" + key + ", value=" + sauceMap.get(key));
				rep_sauce.save(sauce);
			}
		}

		// 3. 재료 저장.
		// 양쪽의 { } 제거
		Map <String,String> ingrMap = detail.getIngredient();
		if (!ingrMap.isEmpty()) {
			for (String key : ingrMap.keySet()) {
				TB_RECIPE_INGR ingr = new TB_RECIPE_INGR();
				
				ingr.setIngrName(key);
				ingr.setIngrCnt(ingrMap.get(key));
				ingr.setRecipe(recipe);
				System.out.println("key=" + key + ", value=" + ingrMap.get(key));
				rep_ingr.save(ingr);
			}
		}
		
		// 4. 스텝 저장.
		List<Map<String, String>> stepMapList = detail.getStepList();
		if (!stepMapList.isEmpty()) {
			for(int i=0;i<stepMapList.size();i++) {
				Map <String,String> stepMap = stepMapList.get(i);
				if (!stepMap.isEmpty()) {
					for (String key : stepMap.keySet()) {
						TB_RECIPE_STEP step = new TB_RECIPE_STEP();
						step.setStepOrd(i+1);
						step.setStepCont(key);
						step.setStepImg(stepMap.get(key));
						step.setRecipe(recipe);
						System.out.println("key=" + key + ", value=" + stepMap.get(key));
						rep_step.save(step);
					}
				}
			}
			
		}
		
		return recipe;
	}
	
	
	public boolean deleteRecipe(String rcpId) {
		Optional<TB_RECIPE> temp = rep_rcp.findById(rcpId);
		if (temp.isEmpty()) {
			return false;
		}
		else {
			TB_RECIPE rcp = temp.get();
			List<TB_RECIPE_INGR> IngrList = getRecipeIngr(rcp.getRcpId());
			List<TB_RECIPE_SAUCE> SauceList = getRecipeSauce(rcp.getRcpId());
			List<TB_RECIPE_STEP> StepList = getRecipeStep(rcp.getRcpId());
			rep_ingr.deleteAll(IngrList);
			rep_sauce.deleteAll(SauceList);
			rep_step.deleteAll(StepList);
			rep_rcp.delete(rcp);
			return true;
		}
	}
	
	// 레시피 검색
	public List<RecipeTopDto> searchRecipe(TB_USER user, String query) {
		Page<TB_RECIPE> response = rep_rcp.searchSimple(query, PageRequest.of(0,10));
		List<TB_RECIPE> content = response.getContent();
//		for(int i=0;i<content.size();i++) System.out.println(content.get(i).getRcpNm());
		return getListRecipeTopDto(user.getUserId(),content);
	}
	
	
	
	
	
	
	/*
	 * 아래부터는 DB에 레시피 추가하는 코드
	 * 
	 * 
	 * 
	 */

	@Transactional
	public void updateRecipeByAdmin(TB_USER admin) {
		String dirPath = "C:\\Users\\dreac\\Desktop\\실전\\데이터\\recipes_split";
		Path dir = Paths.get(dirPath);
		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException("디렉터리 아님: " + dirPath);
		}
		try (Stream<Path> paths = Files.list(dir)) {
			paths.filter(p -> p.getFileName().toString().matches("food\\d+\\.json")).sorted().forEach(p -> {
				try {
					importOneFile(p, admin);
				} catch (Exception e) {
					log.error("파일 처리 실패: {}", p, e);
				}
			});
		} catch (Exception ex) {
			log.error("파일들 처리 실패: {}", ex);
		}
	}

	@Transactional
	public void importOneFile(Path file, TB_USER admin) throws IOException {
		String raw = Files.readString(file, StandardCharsets.UTF_8).trim();
		RecipeJsonDto dto = objectMapper.readValue(raw, RecipeJsonDto.class);
		System.out.println("--------------------");
		System.out.println(dto);
		upsertRecipe(dto, admin, file.getFileName().toString());
	}

	private void upsertRecipe(RecipeJsonDto dto, TB_USER admin, String fileName) throws IOException {
		String title = nullSafe(dto.getTitle());
		String mainImg = nullSafe(dto.getMainImg());
		// JSON 필드는 원본 유지 위해 문자열로 직렬화
		String ingredientJson = dto.getIngredient() == null ? null
				: objectMapper.writeValueAsString(dto.getIngredient());
		String sauceJson = dto.getSauce() == null ? null : objectMapper.writeValueAsString(dto.getSauce());
		String knowHowJson = dto.getKnowHow() == null ? null : objectMapper.writeValueAsString(dto.getKnowHow());
		String stepListJson = dto.getStep_list() == null ? null : objectMapper.writeValueAsString(dto.getStep_list());

		System.out.println(stepListJson);

		// 1. 레시피 저장
		TB_RECIPE recipe = new TB_RECIPE();
		recipe.setMainImg(mainImg);
		recipe.setRcpId(fileName);
		recipe.setRcpNm(title);
		if (title.length() == 0)
			return;
		recipe.setUser(admin);
		recipe.setDescTxt(dto.getSummary());
		recipe.setPortion(dto.getPortion());
		recipe.setTime(dto.getTime());
		recipe.setLevel(dto.getLevel());
		recipe.setTip(dto.getTip());
		/*
		 * Map<String, CategoryMenu> MenuMap = new HashMap<>(); MenuMap.put("양념",
		 * CategoryMenu.S); MenuMap.put("구이", CategoryMenu.G); MenuMap.put("국물",
		 * CategoryMenu.B); MenuMap.put("무침", CategoryMenu.M); MenuMap.put("절임",
		 * CategoryMenu.P); MenuMap.put("면/밥", CategoryMenu.N); MenuMap.put("밥",
		 * CategoryMenu.N); MenuMap.put("면", CategoryMenu.N); MenuMap.put("제과제빵",
		 * CategoryMenu.K); MenuMap.put("브라우니", CategoryMenu.K); MenuMap.put("간식",
		 * CategoryMenu.K); MenuMap.put("쿠키", CategoryMenu.K); MenuMap.put("디저트",
		 * CategoryMenu.K); MenuMap.put("팬케이크", CategoryMenu.K); MenuMap.put("케이크",
		 * CategoryMenu.K); MenuMap.put("기타", CategoryMenu.O);
		 * 
		 * recipe.setCategoryMenu(MenuMap.get(dto.getCategory_menu()));
		 * 
		 * Map<String, CategoryTime> TimeMap = new HashMap<>(); TimeMap.put("아침",
		 * CategoryTime.B); TimeMap.put("점심", CategoryTime.L); TimeMap.put("저녁",
		 * CategoryTime.D); TimeMap.put("간식", CategoryTime.S);
		 * 
		 * recipe.setCategoryTime(TimeMap.get(dto.getCategory_time()));
		 */
		recipe = rep_rcp.save(recipe);

		// 2. 소스 저장.
		// 양쪽의 { } 제거
		sauceJson = sauceJson.substring(1, sauceJson.length() - 1);
		if (!sauceJson.isBlank()) {
			// , 기준으로 분리
			String[] pairs = sauceJson.split(",");

			for (String pair : pairs) {
				TB_RECIPE_SAUCE sauce = new TB_RECIPE_SAUCE();
				// key:value 분리
				String[] kv = pair.split(":");
				String key = kv[0].replaceAll("\"", ""); // 따옴표 제거
				String value = kv[1].replaceAll("\"", "");
				sauce.setSauceName(key);
				sauce.setSauceCnt(value);
				sauce.setRecipe(recipe);
				System.out.println("key=" + key + ", value=" + value);
				rep_sauce.save(sauce);
			}
		}

		// 3. 재료 저장.
		// 양쪽의 { } 제거
		ingredientJson = ingredientJson.substring(1, ingredientJson.length() - 1);
		if (!ingredientJson.isBlank()) {
			// , 기준으로 분리
			String[] pairs = ingredientJson.split(",");

			for (String pair : pairs) {
				TB_RECIPE_INGR ingr = new TB_RECIPE_INGR();
				// key:value 분리
				String[] kv = pair.split(":");
				String key = kv[0].replaceAll("\"", ""); // 따옴표 제거
				String value = kv[1].replaceAll("\"", "");
				ingr.setIngrName(key);
				ingr.setIngrCnt(value);
				ingr.setRecipe(recipe);
				System.out.println("key=" + key + ", value=" + value);
				rep_ingr.save(ingr);
			}
		}
		/*
		 * // 4. 스텝 저장. // 양쪽의 { } 제거 stepListJson = stepListJson.substring(1,
		 * stepListJson.length() - 1); if (!stepListJson.isBlank()) { // , 기준으로 분리
		 * String[] pairs = stepListJson.split(",");
		 * 
		 * int idx=1; for (String pair : pairs) { TB_RECIPE_STEP step = new
		 * TB_RECIPE_STEP(); // key:value 분리 step.setStepCont(pair);
		 * step.setStepImg(null); step.setStepOrd(idx++); step.setRecipe(recipe);
		 * System.out.println(""); rep_step.save(step); } }
		 */
		// 양쪽의 [ ] 제거
		stepListJson = stepListJson.substring(1, stepListJson.length() - 1);
		if (!stepListJson.isBlank()) {
			// , 기준으로 분리
			String[] pairs = stepListJson.split(",");

			int idx = 1;
			for (String pair : pairs) {
				TB_RECIPE_STEP step = new TB_RECIPE_STEP();
				// 양쪽의 {} 제거
				pair = pair.substring(1, pair.length() - 1);
				// key:value 분리
				String[] kv = pair.split(":");
				String content = kv[0].replaceAll("\"", ""); // 따옴표 제거
				String stepImg = kv[1].replaceAll("\"", "");
				step.setStepCont(content);
				step.setStepImg(stepImg);
				step.setStepOrd(idx++);
				step.setRecipe(recipe);
				System.out.println("");
				rep_step.save(step);
			}
		}

	}

	private String nullSafe(String s) {
		return (s == null || s.isBlank()) ? null : s.trim();
	}

	// 엑셀 FOOD DB에 넣기

	// 헤더명 → 엔티티 필드 키
	private static final Map<String, String> H = Map.of("식품코드", "food_id", "식품대분류명", "desc_txt", "식품명", "food_nm",
			"에너지(kcal)", "cal_val", "단백질(g)", "pr_val", "탄수화물(g)", "ch_val", "지방(g)", "fat_val", "철(mg)", "ic_val");

	public void importFromPath(String excelPath) throws Exception {
		try (FileInputStream fis = new FileInputStream(Path.of(excelPath).toFile());
				Workbook wb = WorkbookFactory.create(fis)) {

			Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
			if (sheet == null)
				return;

			// 헤더 맵핑: 컬럼 인덱스 → 엔티티 필드 키
			Row header = sheet.getRow(sheet.getFirstRowNum());
			if (header == null)
				return;

			Map<Integer, String> colMap = new HashMap<>();
			for (int c = header.getFirstCellNum(); c < header.getLastCellNum(); c++) {
				String name = str(header.getCell(c));
				if (name == null)
					continue;
				String key = H.get(name.trim());
				if (key != null)
					colMap.put(c, key);
			}
			if (!colMap.containsValue("food_id"))
				return; // PK 없으면 의미 없음

			// 본문 → 엔티티 빌드
			Map<String, TB_FOOD> buffer = new LinkedHashMap<>();
			for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
				Row row = sheet.getRow(r);
				if (row == null)
					continue;

				String foodId = null;
				String descTxt = null, foodNm = null;
				Double calVal = null, prVal = null, chVal = null, fatVal = null, icVal = null;

				for (Map.Entry<Integer, String> e : colMap.entrySet()) {
					Cell cell = row.getCell(e.getKey());
					switch (e.getValue()) {
					case "food_id" -> foodId = s(cell);
					case "desc_txt" -> descTxt = s(cell);
					case "food_nm" -> foodNm = s(cell);
					case "cal_val" -> calVal = d(cell);
					case "pr_val" -> prVal = d(cell);
					case "ch_val" -> chVal = d(cell);
					case "fat_val" -> fatVal = d(cell);
					case "ic_val" -> icVal = d(cell);
					}
				}
				if (isBlank(foodId))
					continue;

				buffer.put(foodId, TB_FOOD.builder().foodId(foodId).descTxt(n(descTxt)).foodNm(n(foodNm)).calVal(calVal)
						.prVal(prVal).chVal(chVal).fatVal(fatVal).icVal(icVal).build());
			}

			if (!buffer.isEmpty()) {
				rep_food.saveAll(buffer.values());
			}
		}
	}

	/* ---- 유틸: 짧고 안전하게 ---- */
	private static String str(Cell c) {
		if (c == null)
			return null;
		return switch (c.getCellType()) {
		case STRING -> c.getStringCellValue();
		case NUMERIC -> {
			double v = c.getNumericCellValue();
			yield (Math.floor(v) == v) ? String.valueOf((long) v) : String.valueOf(v);
		}
		case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
		case FORMULA -> {
			try {
				yield c.getStringCellValue();
			} catch (Exception e) {
				try {
					yield String.valueOf(c.getNumericCellValue());
				} catch (Exception ex) {
					yield null;
				}
			}
		}
		default -> null;
		};
	}

	private static String s(Cell c) {
		String v = str(c);
		if (v == null)
			return null;
		v = v.trim();
		return (v.isEmpty() || "NaN".equalsIgnoreCase(v)) ? null : v;
	}

	private static Double d(Cell c) {
		if (c == null)
			return null;
		try {
			return switch (c.getCellType()) {
			case NUMERIC -> c.getNumericCellValue();
			case STRING -> {
				String v = c.getStringCellValue();
				if (v == null)
					yield null;
				v = v.trim().replace(",", "");
				if (v.isEmpty() || "NaN".equalsIgnoreCase(v) || "-".equals(v))
					yield null;
				yield Double.valueOf(v);
			}
			case FORMULA -> {
				try {
					yield c.getNumericCellValue();
				} catch (Exception e) {
					String v = c.getStringCellValue();
					yield (v == null || v.isBlank()) ? null : Double.valueOf(v.trim());
				}
			}
			default -> null;
			};
		} catch (Exception e) {
			return null;
		}
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static String n(String s) {
		return isBlank(s) ? null : s.trim();
	}
	
	
	
	// DB에 STEP 이미지 다시 넣기
//	public void stepImgPush() {
//		try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\dreac\\Desktop\\실전\\데이터\\stepImgOutput.csv"))) {
//		    String line;
//		    int idx=0;
//		    while ((line = br.readLine()) != null) {
//		        String[] values = line.split(",");
//		        // values[0], values[1] ... 로 접근
////		        for (int i=0;i<values.length;i++) {
////		        	values[i]
////		        }
//		        String rcp_id = "food"+idx+".json";
//		        List<TB_RECIPE_STEP> stepList = rep_step.findAllByRecipe_RcpId(rcp_id);
//		        for(int i=0;i<stepList.size();i++) {
////			        System.out.println(values[i]);
//			        try {
//			        	stepList.get(i).setStepImg(values[i]);
//			        	rep_step.save(stepList.get(i));
//			        	System.out.println(values[i]);
//			        }
//			        catch(Exception e){
//			        	break;
//			        }
//			        
//		        }
//		        
//		    }
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
