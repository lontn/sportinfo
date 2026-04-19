package com.wbc.analytics.sportinfo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerSummaryDTO {
    private String id;                 // MLB 選手 ID
    private String name;               // 姓名 (fullName)
    private String positionName;       // 守備位置名稱 (例如: Pitcher)
    private String positionAbbreviation; // 守備位置縮寫 (例如: P, 1B, OF)
    private String teamId;             // 所屬隊伍 ID
}
