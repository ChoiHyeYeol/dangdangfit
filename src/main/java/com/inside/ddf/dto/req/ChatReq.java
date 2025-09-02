package com.inside.ddf.dto.req;

import java.util.List;


import lombok.Data;

@Data 
public class ChatReq {

	String message;
    List<Integer> recent_glucose;
    int portion_g;
}
