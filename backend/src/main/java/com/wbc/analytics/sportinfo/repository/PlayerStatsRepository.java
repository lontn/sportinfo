package com.wbc.analytics.sportinfo.repository;

import com.wbc.analytics.sportinfo.model.entity.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 球員數據 Repository
 * 繼承 JpaRepository 以提供標準 CRUD
 */
@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {

    /**
     * 依據球員名稱查詢
     */
    List<PlayerStats> findByPlayerName(String playerName);
}

