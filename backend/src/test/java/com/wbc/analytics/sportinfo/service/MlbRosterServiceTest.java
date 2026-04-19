package com.wbc.analytics.sportinfo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wbc.analytics.sportinfo.model.dto.PlayerSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MlbRosterServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MlbRosterService mlbRosterService;

    private final String urlTemplate = "https://statsapi.mlb.com/api/v1/teams/{teamId}/roster";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mlbRosterService, "rosterUrlTemplate", urlTemplate);
    }

    @Test
    void fetchRosterByTeam_WhenTeamIdIsBlank_ShouldReturnEmptyList() {
        List<PlayerSummaryDTO> result = mlbRosterService.fetchRosterByTeam("   ");
        assertThat(result).isEmpty();
    }

    @Test
    void fetchRosterByTeam_WhenApiReturnsNull_ShouldReturnEmptyList() {
        when(restTemplate.getForObject(urlTemplate, String.class, "119")).thenReturn(null);
        List<PlayerSummaryDTO> result = mlbRosterService.fetchRosterByTeam("119");
        assertThat(result).isEmpty();
    }

    @Test
    void fetchRosterByTeam_WhenApiReturnsBlankString_ShouldReturnEmptyList() {
        when(restTemplate.getForObject(urlTemplate, String.class, "119")).thenReturn("  ");
        List<PlayerSummaryDTO> result = mlbRosterService.fetchRosterByTeam("119");
        assertThat(result).isEmpty();
    }

    @Test
    void fetchRosterByTeam_WhenApiThrowsException_ShouldReturnEmptyList() {
        when(restTemplate.getForObject(urlTemplate, String.class, "119"))
                .thenThrow(new RestClientException("API 連線超時"));
        List<PlayerSummaryDTO> result = mlbRosterService.fetchRosterByTeam("119");
        assertThat(result).isEmpty();
    }

    @Test
    void fetchRosterByTeam_WhenJsonIsValid_ShouldReturnRosterList() {
        String json = """
                {
                  "roster": [
                    {
                      "person": {
                        "id": 660271,
                        "fullName": "Shohei Ohtani"
                      },
                      "position": {
                        "name": "Two-Way Player",
                        "abbreviation": "TWP"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.getForObject(urlTemplate, String.class, "119")).thenReturn(json);

        List<PlayerSummaryDTO> result = mlbRosterService.fetchRosterByTeam("119");

        assertThat(result).hasSize(1);
        PlayerSummaryDTO player = result.get(0);
        assertThat(player.getId()).isEqualTo("660271");
        assertThat(player.getName()).isEqualTo("Shohei Ohtani");
        assertThat(player.getPositionName()).isEqualTo("Two-Way Player");
        assertThat(player.getPositionAbbreviation()).isEqualTo("TWP");
        assertThat(player.getTeamId()).isEqualTo("119");
    }

    @Test
    void fetchRosterByTeam_WhenPlayerIdIsMissing_ShouldSkipPlayerAndReturnEmpty() {
        String json = """
                {
                  "roster": [
                    {
                      "person": {
                        "fullName": "Unknown Player"
                      },
                      "position": {
                        "name": "Pitcher",
                        "abbreviation": "P"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.getForObject(urlTemplate, String.class, "119")).thenReturn(json);

        List<PlayerSummaryDTO> result = mlbRosterService.fetchRosterByTeam("119");

        assertThat(result).isEmpty();
    }

    @Test
    void fetchRosterByTeam_WhenJsonIsMissingRosterArray_ShouldReturnEmptyList() {
        String json = """
                {
                  "message": "查無資料"
                }
                """;
        when(restTemplate.getForObject(urlTemplate, String.class, "119")).thenReturn(json);

        List<PlayerSummaryDTO> result = mlbRosterService.fetchRosterByTeam("119");

        assertThat(result).isEmpty();
    }
}
