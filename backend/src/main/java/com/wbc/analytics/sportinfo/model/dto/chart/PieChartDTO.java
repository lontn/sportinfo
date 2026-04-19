package com.wbc.analytics.sportinfo.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 圓餅圖 / 佔比圖 DTO (Pie Chart / Usage)
 * Used for:
 * 1. Pitch Repertoire / Usage %
 * 2. Count Analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieChartDTO {
    private String label; // 標籤 (e.g. "Four-Seam Fastball", "Slider")
    private double value; // 數值 (Percentage or Count)
}

