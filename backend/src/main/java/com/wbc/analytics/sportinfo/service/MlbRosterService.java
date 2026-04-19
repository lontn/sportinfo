package com.wbc.analytics.sportinfo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wbc.analytics.sportinfo.model.dto.PlayerSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MlbRosterService {

    private final RestTemplate restTemplate;
    // 注入 ObjectMapper 以手動解析 JSON
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${mlb.api.roster-url-template}")
    private String rosterUrlTemplate;

    public List<PlayerSummaryDTO> fetchRosterByTeam(String teamId) {
        if (StringUtils.isBlank(teamId)) {
            log.warn("嘗試查詢名單，但 teamId 為空");
            return Collections.emptyList();
        }

        List<PlayerSummaryDTO> rosterList = new ArrayList<>();

        try {
            log.debug("開始查詢球隊 {} 的名單", teamId);

            // Fetch as String first to avoid Type definition error with JsonNode direct mapping
            String jsonResponse = restTemplate.getForObject(rosterUrlTemplate, String.class, teamId);

            if (StringUtils.isBlank(jsonResponse)) {
                log.warn("球隊 {} 名單 API 回傳空內容", teamId);
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(jsonResponse);

            Optional.ofNullable(root)
                    .map(r -> r.path("roster"))
                    .filter(JsonNode::isArray)
                    .ifPresent(rosterNode -> {
                        for (JsonNode entry : rosterNode) {
                            processRosterEntry(entry, teamId).ifPresent(rosterList::add);
                        }
                    });

        } catch (RestClientException e) {
            log.error("呼叫 MLB Roster API 失敗，TeamId: {}, 錯誤: {}", teamId, e.getMessage());
        } catch (Exception e) {
            log.error("解析球隊名單發生未預期錯誤，TeamId: {}, 錯誤: {}", teamId, e.getMessage(), e);
        }

        return rosterList;
    }

    private Optional<PlayerSummaryDTO> processRosterEntry(JsonNode entry, String teamId) {
        // 使用 path() 確保即使欄位缺失也不會拋出 NullPointerException
        JsonNode person = entry.path("person");
        JsonNode position = entry.path("position");

        String id = person.path("id").asText(null);
        String name = person.path("fullName").asText();
        String posName = position.path("name").asText();
        String posAbbr = position.path("abbreviation").asText();

        // 簡單過濾：ID 為空則不處理
        if (StringUtils.isBlank(id)) {
            return Optional.empty();
        }

        return Optional.of(new PlayerSummaryDTO(id, name, posName, posAbbr, teamId));
    }
}
