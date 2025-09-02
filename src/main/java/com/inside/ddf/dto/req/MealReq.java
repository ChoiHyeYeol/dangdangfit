package com.inside.ddf.dto.req;

import java.util.List;

import lombok.Data;

@Data 
public class MealReq {

	String user_type;
	List<String> preferences;
	List<String> allergies;
}
