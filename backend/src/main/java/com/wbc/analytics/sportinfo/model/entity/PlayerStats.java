package com.wbc.analytics.sportinfo.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 球員數據實體類別
 * 對應資料庫中的球員數據表
 */
@Data
@Entity
@Table(name = "player_stats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStats {

    /**
     * 主鍵 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 球員名稱
     */
    private String playerName;

    /**
     * 比賽日期
     */
    private LocalDateTime matchDate;

    /**
     * 速度 (0-100)
     */
    private Integer speed;

    /**
     * 力量 (0-100)
     */
    private Integer power;

    /**
     * 敏捷 (0-100)
     */
    private Integer agility;

    /**
     * 技巧 (0-100)
     */
    private Integer technique;

    /**
     * 戰術意識 (0-100)
     */
    private Integer tactic;

    /**
     * 精神力 (0-100)
     */
    private Integer mentality;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
}

