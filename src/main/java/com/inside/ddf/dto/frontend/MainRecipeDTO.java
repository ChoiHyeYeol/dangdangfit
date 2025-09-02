package com.inside.ddf.dto.frontend;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MainRecipeDTO {
	private String rcpId;     // 레시피 ID
    private String rcpNm;     // 레시피 이름
    private String mainImg;   // 대표 이미지
    private String time;      // 조리 시간
    private String portion;   // 인분
    private String level;     // 난이도
}
