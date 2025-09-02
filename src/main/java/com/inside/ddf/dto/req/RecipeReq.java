package com.inside.ddf.dto.req;

import java.util.List;

import lombok.Data;

@Data
public class RecipeReq {

	String url;
	String user_type;
	List<String> allergies;
}
