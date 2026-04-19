package com.wbc.analytics.sportinfo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlayerRadarDTO {
    private String id;
    private String name;
    private String team;
    private String role; // PITCHER or BATTER
    private List<AbilityPoint> stats;

    @Data
    @AllArgsConstructor
    public static class AbilityPoint {
        private String subject; // 例如：打擊、力量、速度、防守、傳球、體力
        private int value;      // 0-100
        private int fullMark;   // 滿分 (通常為 100)
    }
}
