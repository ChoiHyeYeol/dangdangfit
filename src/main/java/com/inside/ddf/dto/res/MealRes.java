package com.inside.ddf.dto.res;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class MealRes {
 // 순서 보존을 원하면 LinkedHashMap 권장
 private Map<Integer, List<List<String>>> plan = new LinkedHashMap<>();
}
