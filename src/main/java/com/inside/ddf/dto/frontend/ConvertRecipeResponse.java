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
public class ConvertRecipeResponse {
	private String rcpId;
   private String rcpNm;
   private String mainImg;
   private String portion;
   private String time;
   private String level;

   private List<com.inside.ddf.dto.frontend.RecipeDetailDto.IngredientDto> ingredients; 
   private List<com.inside.ddf.dto.frontend.RecipeDetailDto.SauceDto> sauces; 
   private List<com.inside.ddf.dto.frontend.RecipeDetailDto.StepDto> steps;
   
   

}
