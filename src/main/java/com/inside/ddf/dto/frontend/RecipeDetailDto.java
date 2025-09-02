package com.inside.ddf.dto.frontend;

import java.util.List;

import com.inside.ddf.entity.TB_RECIPE_INGR;
import com.inside.ddf.entity.TB_RECIPE_SAUCE;
import com.inside.ddf.entity.TB_RECIPE_STEP;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDetailDto {
   private String rcpNm;
   private String mainImg;
   private String portion;
   private String time;
   private String level;

   private List<IngredientDto> ingredients; 
   private List<SauceDto> sauces; 
   private List<StepDto> steps;
   
   private boolean liked;
   private int likeCount;
   
   public static record IngredientDto(String ingrName, String ingrCnt) {}
   public static record SauceDto(String sauceName, String sauceCnt) {}
   public static record StepDto(int stepOrd, String stepCont, String stepImg) {}

}
