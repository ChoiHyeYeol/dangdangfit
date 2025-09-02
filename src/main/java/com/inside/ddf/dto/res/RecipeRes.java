package com.inside.ddf.dto.res;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipeRes {

 @JsonProperty("food")
 private Food food;

 // 숫자 값이 누락될 수 있으면 Integer로 (null 허용)
 @JsonProperty("GI_VAL")
 private Integer giVal;

 @JsonProperty("CH_VAL")
 private Integer chVal;

 @JsonProperty("PR_VAL")
 private Integer prVal;

 @JsonProperty("FAT_VAL")
 private Integer fatVal;

 @JsonProperty("IC_VAL")
 private Integer icVal;

 // ---- 중첩 DTO ----
 @Getter @Setter
 @NoArgsConstructor @AllArgsConstructor
 @Builder
 @JsonIgnoreProperties(ignoreUnknown = true)
 public static class Food {

     @JsonProperty("mainImg")
     private String mainImg;
     @JsonProperty("writer")
     private String writer;

     @JsonProperty("title")
     private String title;

     @JsonProperty("summary")
     private String summary;

     @JsonProperty("portion")
     private String portion;

     @JsonProperty("time")
     private String time;

     @JsonProperty("level")
     private String level;

     // 자유 키(공백/특수문자 포함) → 값(수량/단위)
     @JsonProperty("ingredient")
     private Map<String, String> ingredient;

     @JsonProperty("sauce")
     private Map<String, String> sauce;

     // knowHow: 문자열 배열
     @JsonProperty("knowHow")
     private List<String> knowHow;

     // step_list: [{ "설명": "이미지URL" }, ... ]
     @JsonProperty("step_list")
     private List<Map<String, String>> stepList;

     @JsonProperty("tip")
     private String tip;

     @JsonProperty("notes")
     private List<String> notes;
 }
}
