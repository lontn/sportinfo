package com.wbc.analytics.sportinfo.service;

import com.wbc.analytics.sportinfo.model.dto.PlayerRadarDTO;
import com.wbc.analytics.sportinfo.model.entity.PlayerStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 球員數據 Service
 * 負責處理業務邏輯與 DTO 轉換
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerStatsService {

//    private final PlayerStatsRepository playerStatsRepository;

    public PlayerRadarDTO getPlayerRadarData(String playerId) {
        // 假設從 MLB API 或資料庫抓到大谷翔平的原始數據
        // 在這裡進行你的「權重計算」邏輯

        return new PlayerRadarDTO(
                playerId,
                "Shohei Ohtani",
                "Japan",
                "PITCHER", // 預設角色
                Arrays.asList(
                        new PlayerRadarDTO.AbilityPoint("打擊", 95, 100),
                        new PlayerRadarDTO.AbilityPoint("力量", 99, 100),
                        new PlayerRadarDTO.AbilityPoint("速度", 88, 100),
                        new PlayerRadarDTO.AbilityPoint("防守", 75, 100),
                        new PlayerRadarDTO.AbilityPoint("傳球", 90, 100),
                        new PlayerRadarDTO.AbilityPoint("體力", 92, 100)
                )
        );
    }

    /**
     * 獲取所有球員的雷達圖數據
     *
     * @return List<PlayerRadarDTO>
     */
//    @Transactional(readOnly = true)
//    public List<PlayerRadarDTO> getAllPlayerRadarStats() {
//        log.info("Fetching all player stats for radar chart");
//
//        List<PlayerStats> allStats = playerStatsRepository.findAll();
//
//        if (CollectionUtils.isEmpty(allStats)) {
//            log.warn("No player stats found in database, returning mock data if needed");
//            // 若無資料庫數據，可在此回傳模擬數據，或回傳空列表
//            // 這裡為了演示，若無數據則生成一組預設模擬數據
//            return generateMockData();
//        }
//
//        // 將 Entity 轉換為前端需要的 DTO
//        return allStats.stream()
//                .map(this::convertToRadarDTO)
//                .collect(Collectors.toList());
//    }

    /**
     * 根據ID獲取單一球員雷達圖數據
     * @param id 球員ID
     * @return Optional<PlayerRadarDTO>
     */
//    @Transactional(readOnly = true)
//    public Optional<PlayerRadarDTO> getPlayerRadarStatsById(Long id) {
//        return playerStatsRepository.findById(id)
//                .map(this::convertToRadarDTO);
//    }

    /**
     * 生成模擬數據 (當資料庫為空時使用)
     */
    private List<PlayerRadarDTO> generateMockData() {
        PlayerStats mockPlayer = PlayerStats.builder()
                .id(1L)
                .playerName("大谷翔平")
                .speed(95)
                .power(98)
                .agility(85)
                .technique(92)
                .tactic(88)
                .mentality(99)
                .build();

        return List.of(convertToRadarDTO(mockPlayer));
    }

    /**
     * 轉換邏輯: Entity -> DTO
     */
    private PlayerRadarDTO convertToRadarDTO(PlayerStats entity) {
        // 定義雷達圖的各項能力值
        List<PlayerRadarDTO.AbilityPoint> stats = new ArrayList<>();

        // 速度
        stats.add(new PlayerRadarDTO.AbilityPoint("速度", entity.getSpeed() != null ? entity.getSpeed() : 0, 100));
        // 力量
        stats.add(new PlayerRadarDTO.AbilityPoint("力量", entity.getPower() != null ? entity.getPower() : 0, 100));
        // 敏捷
        stats.add(new PlayerRadarDTO.AbilityPoint("敏捷", entity.getAgility() != null ? entity.getAgility() : 0, 100));
        // 技巧
        stats.add(new PlayerRadarDTO.AbilityPoint("技巧", entity.getTechnique() != null ? entity.getTechnique() : 0, 100));
        // 戰術
        stats.add(new PlayerRadarDTO.AbilityPoint("戰術", entity.getTactic() != null ? entity.getTactic() : 0, 100));
        // 精神
        stats.add(new PlayerRadarDTO.AbilityPoint("精神", entity.getMentality() != null ? entity.getMentality() : 0, 100));

        // 這裡假設這是一個範例轉換，Team暫時寫死或雖後續擴充 Entity 欄位
        String teamName = "Team A"; // 預設值，因為 Entity 目前沒有 Team 欄位
        String idProxy = String.valueOf(entity.getId());

        return new PlayerRadarDTO(
            idProxy,
            entity.getPlayerName(),
            teamName,
            "BATTER", // 預設角色，或者從 Entity 中獲取
            stats
        );
    }
}
