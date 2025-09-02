package com.inside.ddf.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RecipeJsonDto {
    private String mainImg;
    private String writer;
    private String title;
    private String summary;
    private String portion;
    private String time;
    private String level;

    private Map<String, String> ingredient; // { "귀리 가루":"1컵", ... }
    private Map<String, String> sauce;      // { "간장":"1큰술", ... }

    private List<String> knowHow;           // ["팬 예열...", ...]
    private List<Map<String, String>> step_list;         // ["1. ...", "2. ..."]
    private String tip;

    private String category_time;           // "아침" 등
    private String category_menu;           // "국물" 등
}