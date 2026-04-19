package com.wbc.analytics.sportinfo.controller;

import com.wbc.analytics.sportinfo.model.dto.PlayerRadarDTO;
import com.wbc.analytics.sportinfo.service.MlbStatsService;
import com.wbc.analytics.sportinfo.service.PlayerStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor // 使用 Lombok 生成建構子，取代 @Autowired
public class WbcPlayerController {

//    private PlayerStatsService playerStatsService;

    private final MlbStatsService mlbStatsService;


    @GetMapping("/{id}/radar")
    public PlayerRadarDTO getRadarStats(@PathVariable String id) {
        return mlbStatsService.getPlayerWbcStats(id);
    }
}
