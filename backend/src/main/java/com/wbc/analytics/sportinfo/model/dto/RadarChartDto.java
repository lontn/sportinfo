package com.wbc.analytics.sportinfo.model.dto;

import lombok.Builder;
import java.util.List;

/**
 * 專供前端雷達圖使用的 DTO
 * 使用 Java Record (Java 17+)
 *
 * @param title 圖表標題
 * @param indicators 雷達圖的指標 (例如: 速度, 力量等)
 * @param series 數據系列 (例如: 球員 A 的數據, 球員 B 的數據)
 */
@Builder
public record RadarChartDto(
    String title,
    List<RadarIndicator> indicators,
    List<RadarSeries> series
) {
    /**
     * 雷達圖指標定義
     *
     * @param name 指標名稱 (顯示文字)
     * @param max 該指標的最大值
     */
    public record RadarIndicator(
        String name,
        Integer max
    ) {}

    /**
     * 雷達圖數據系列
     *
     * @param name 系列名稱 (例如球員名字)
     * @param data 數值列表 (對應 indicators 的順序)
     */
    public record RadarSeries(
        String name,
        List<Integer> data
    ) {}
}

