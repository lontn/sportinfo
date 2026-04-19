package com.wbc.analytics.sportinfo.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 熱力圖 DTO (Heatmap)
 * 用於呈現好球帶熱點或進壘點分佈
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDTO {
    private String title;
    private List<Zone> zones; // 區域數據

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Zone {
        /**
         * 區域標識
         * 1-9: Store Zone (好球帶九宮格)
         * 11-14: Chase Zone (好球帶外圍追打區)
         *  1 | 2 | 3
         * ---+---+---
         *  4 | 5 | 6
         * ---+---+---
         *  7 | 8 | 9
         */
        private String zoneId;
        private double value;  // 數值 (例如: 打擊率 .312, 或 投球百分比 15.5)
        private String color;  // 前端渲染顏色 (可選, 例如 "#FF0000" 代表熱區)
    }
}

