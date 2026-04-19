package com.wbc.analytics.sportinfo.controller;

import com.wbc.analytics.sportinfo.model.dto.chart.HeatmapDTO;
import com.wbc.analytics.sportinfo.model.dto.chart.PieChartDTO;
import com.wbc.analytics.sportinfo.model.dto.chart.ScatterPlotDTO;
import com.wbc.analytics.sportinfo.model.dto.chart.TrendChartDTO;
import com.wbc.analytics.sportinfo.service.MlbStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players/{id}/charts")
@RequiredArgsConstructor
@Tag(name = "Player Charts", description = "進階圖表分析 API")
public class ChartController {

    private final MlbStatsService mlbStatsService;

    @GetMapping("/heatmap")
    @Operation(summary = "取得熱區圖數據 (Heatmap)", description = "例如: 好球帶熱點")
    public HeatmapDTO getHeatmap(
            @PathVariable String id,
            @RequestParam(defaultValue = "strike-zone") String type) {
        return mlbStatsService.getHeatmapData(id, type);
    }

    @GetMapping("/scatter")
    @Operation(summary = "取得散佈圖數據 (Scatter Plot)", description = "例如: 擊球初速/仰角、球種位移、落點分佈")
    public ScatterPlotDTO getScatterPlot(
            @PathVariable String id,
            @RequestParam(defaultValue = "exit-velocity") String type) {
        return mlbStatsService.getScatterPlotData(id, type);
    }

    @GetMapping("/trend")
    @Operation(summary = "取得賽季趨勢圖數據 (Trend)", description = "例如: 近 15 場 OPS 或 ERA 變化")
    public TrendChartDTO getTrendChart(
            @PathVariable String id,
            @RequestParam(defaultValue = "ops") String metric) {
        return mlbStatsService.getTrendData(id, metric);
    }

    @GetMapping("/usage")
    @Operation(summary = "取得配球比例數據 (Pie Chart)", description = "投手各球種使用比例")
    public List<PieChartDTO> getPitchUsage(@PathVariable String id) {
        return mlbStatsService.getPitchUsageData(id);
    }
}

