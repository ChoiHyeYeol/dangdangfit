package com.inside.ddf.dto.res;


import lombok.Data;

@Data
public class ChatRes {

	String input;
	String matched_food;
	Boolean nutrition_hit;
	String nutrition_source;
	double serving_g;
	double carb_g;
	double gi;
	double gl;
	String gl_level;
	String adjusted_level;
	String decision;
	double probability_ok;
	double recommended_portion_g;
	String explanation;
}
