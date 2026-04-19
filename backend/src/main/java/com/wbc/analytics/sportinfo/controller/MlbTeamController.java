package com.wbc.analytics.sportinfo.controller;

import com.wbc.analytics.sportinfo.model.dto.PlayerSummaryDTO;
import com.wbc.analytics.sportinfo.service.MlbRosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "MLB Team API", description = "MLB 球隊相關資料接口")
public class MlbTeamController {

    private final MlbRosterService mlbRosterService;

    @Operation(summary = "取得球隊選手名單", description = "根據 Team ID 查詢該球隊的目前名單 (Roster)")
    @GetMapping("/{teamId}/roster")
    public ResponseEntity<List<PlayerSummaryDTO>> getTeamRoster(@PathVariable String teamId) {
        log.info("收到查詢球隊名單請求，Team ID: {}", teamId);
        List<PlayerSummaryDTO> roster = mlbRosterService.fetchRosterByTeam(teamId);
        return ResponseEntity.ok(roster);
    }
}

