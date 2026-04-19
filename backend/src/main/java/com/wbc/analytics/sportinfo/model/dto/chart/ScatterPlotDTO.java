package com.wbc.analytics.sportinfo.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 散佈圖 DTO (Scatter Plot)
 * Used for:
 * 1. Exit Velocity vs Launch Angle (Barrel Zones)
 * 2. Pitch Movement (Horizontal/Vertical Break)
 * 3. Spray Chart (Hit Location coordinates)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScatterPlotDTO {
    private String title;
    private String xLabel; // X軸名稱
    private String yLabel; // Y軸名稱
    private List<Point> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private Number x;    // X軸數值 (e.g. Exit Velocity, Horizontal Break, coordinate X)
        private Number y;    // Y軸數值 (e.g. Launch Angle, Vertical Break, coordinate Y)
        private String type; // 類別 (e.g. "Home Run", "Slider", "Flyout")
        private String info; // 額外資訊供 Tooltip (e.g. "105.5 mph / 25 deg")
    }
}

