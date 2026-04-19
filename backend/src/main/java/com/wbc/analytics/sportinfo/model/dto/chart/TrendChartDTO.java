package com.wbc.analytics.sportinfo.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趨勢圖 DTO (Trend / Line Chart)
 * Used for:
 * 1. Rolling OPS/ERA (Season Trend)
 * 2. Velocity Drop (Pitching Fatigue)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendChartDTO {
    private String title;
    private List<String> labels; // X軸標籤 (e.g. "Game 1", "Game 2", or Dates)
    private List<Series> series; // 數據系列

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Series {
        private String name; // 系列名稱 (e.g. "AVG", "ERA", "Fastball Velocity")
        private List<Number> data; //數值列表
    }
}

