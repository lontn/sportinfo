package com.wbc.analytics.sportinfo.service;

import com.wbc.analytics.sportinfo.model.dto.PlayerRadarDTO;
import com.wbc.analytics.sportinfo.model.dto.chart.HeatmapDTO;
import com.wbc.analytics.sportinfo.model.dto.chart.PieChartDTO;
import com.wbc.analytics.sportinfo.model.dto.chart.ScatterPlotDTO;
import com.wbc.analytics.sportinfo.model.dto.chart.TrendChartDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MlbStatsService {
    // 若您沒有配置 RestTemplate Bean，這裡可以暫時維持 new，但建議後續移至 Config
    // private final RestTemplate restTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    // 現在有了 JacksonConfig，這裡就能順利注入了
    private final ObjectMapper objectMapper;

    @Value("${mlb.api.base-url}")
    private String baseUrl;

    @Value("${mlb.api.season}")
    private String season;

    @Value("${mlb.api.hydrate-params.group}")
    private String group;

    @Value("${mlb.api.hydrate-params.type}")
    private String type;

    public PlayerRadarDTO getPlayerWbcStats(String personId) {
        // 1. 構建 URL (使用 hydrate 同步抓取賽季統計)
        // 強制抓取 hitting, pitching 與 fielding 數據以支援雷達圖計算
        // season=2025 (or configured), group=[hitting,pitching,fielding]
        String targetGroup = "[hitting,pitching,fielding]";
        String url = String.format("%s%s?hydrate=stats(group=%s,type=%s,season=%s)",
                baseUrl, personId, targetGroup, type, season);

        try {
            log.info("Requesting MLB API: {}", url);
            String jsonResponse = restTemplate.getForObject(url, String.class);
            // log.debug("MLB API Response: {}", jsonResponse); // 減少 log 量
            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root == null || !root.has("people")) {
                log.warn("API 回傳缺少 'people' 節點");
                return null; // 或者拋出異常
            }

            // 3. 解析樹狀結構：people -> [0]
            JsonNode peopleArray = root.path("people");
            if (peopleArray.isEmpty()) {
                log.warn("API 回傳 'people' 陣列為空");
                return null;
            }
            JsonNode playerNode = peopleArray.get(0);
            String fullName = playerNode.path("fullName").asText();
            String teamName = playerNode.path("currentTeam").path("name").asText("Free Agent");

            // 判斷角色 (Pitcher vs Batter)
            String primaryPositionCode = playerNode.path("primaryPosition").path("code").asText();
            String primaryPositionType = playerNode.path("primaryPosition").path("type").asText();
            boolean isPitcher = "1".equals(primaryPositionCode) || "Pitcher".equalsIgnoreCase(primaryPositionType);
            String role = isPitcher ? "PITCHER" : "BATTER";

            // 4. 解析數據
            JsonNode statsArray = playerNode.path("stats");

            // 尋找數據節點
            JsonNode hittingStat = null;
            JsonNode pitchingStat = null;
            JsonNode fieldingStat = null;

            if (statsArray.isArray()) {
                for (JsonNode groupNode : statsArray) {
                    JsonNode groupType = groupNode.path("group");
                    JsonNode splits = groupNode.path("splits");
                    if (splits.isArray() && !splits.isEmpty()) {
                        JsonNode stat = splits.get(0).path("stat"); // 取第一筆 split (通常為總計)
                        String groupDisplayName = groupType.path("displayName").asText();

                        if ("hitting".equalsIgnoreCase(groupDisplayName)) {
                            hittingStat = stat;
                        } else if ("pitching".equalsIgnoreCase(groupDisplayName)) {
                            pitchingStat = stat;
                        } else if ("fielding".equalsIgnoreCase(groupDisplayName)) {
                            fieldingStat = stat;
                        }
                    }
                }
            }

            // 5. 數據轉化邏輯
            if (isPitcher) {
                if (pitchingStat == null || pitchingStat.isMissingNode()) {
                    log.warn("找不到投手 {} 的投球數據", fullName);
                    return createEmptyRadar(personId, fullName, teamName, role);
                }
                return mapToPitcherRadar(personId, fullName, teamName, pitchingStat);
            } else {
                if (hittingStat == null || hittingStat.isMissingNode()) {
                    log.warn("找不到打者 {} 的打擊數據", fullName);
                    return createEmptyRadar(personId, fullName, teamName, role);
                }
                return mapToBatterRadar(personId, fullName, teamName, hittingStat, fieldingStat);
            }

        } catch (Exception e) {
            log.error("解析 MLB API 發生錯誤: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new RuntimeException("MLB Data Fetch Error");
        }
    }

    private PlayerRadarDTO mapToBatterRadar(String id, String name, String team, JsonNode hittingStat, JsonNode fieldingStat) {
        List<PlayerRadarDTO.AbilityPoint> points = new ArrayList<>();

        // 1. 打擊 (Batting): 以 AVG 與 OPS 綜合評估
        // 滿分基準: AVG .350
        double avg = hittingStat.path("avg").asDouble(0.0);
        double ops = hittingStat.path("ops").asDouble(0.0);
        int battingScore = Math.min((int) ((avg / 0.350) * 60 + (ops / 1.000) * 40), 100);
        points.add(new PlayerRadarDTO.AbilityPoint("打擊", battingScore, 100));

        // 2. 力量 (Power): 以 HR 與 SLG 評估
        // 滿分基準: HR 40 (賽季) / SLG .600
        int hr = hittingStat.path("homeRuns").asInt(0);
        double slg = hittingStat.path("slg").asDouble(0.0);
        int powerScore = Math.min((int) ((hr / 40.0) * 60 + (slg / 0.600) * 40), 100);
        points.add(new PlayerRadarDTO.AbilityPoint("力量", powerScore, 100));

        // 3. 速度 (Speed): 以 SB (盜壘) 與 跑壘相關數據
        // 滿分基準: SB 30
        int sb = hittingStat.path("stolenBases").asInt(0);
        int tri = hittingStat.path("triples").asInt(0);
        int speedScore = Math.min((int) ((sb / 30.0) * 70 + (tri / 5.0) * 30), 100);
        points.add(new PlayerRadarDTO.AbilityPoint("速度", speedScore, 100));

        // 4. 防守 (Defense): 若無守備數據則給預設值
        // 滿分基準: Fielding % .990
        int defenseScore = 50; // 預設
        if (fieldingStat != null && !fieldingStat.isMissingNode()) {
            double fp = fieldingStat.path("fielding").asDouble(0.0);
            if (fp > 0) {
                 // Fielding % 通常在 .950 - .999 之間，將差異放大
                 defenseScore = (int) ((fp - 0.900) * 1000);
                 defenseScore = Math.max(0, Math.min(defenseScore, 100));
            }
        }
        points.add(new PlayerRadarDTO.AbilityPoint("防守", defenseScore, 100));

        // 5. 傳球 (Throwing): 以 Assists (助殺) 評估
        int throwingScore = 60; // 基礎分
        if (fieldingStat != null && !fieldingStat.isMissingNode()) {
            int assists = fieldingStat.path("assists").asInt(0);
            throwingScore = Math.min(60 + (int)(assists / 5.0), 100);
        }
        points.add(new PlayerRadarDTO.AbilityPoint("傳球", throwingScore, 100));

        // 6. 體力 (Stamina): 對於野手，用出賽數 (Games Played) 評估
        // 滿分基準: 150 場
        int games = hittingStat.path("gamesPlayed").asInt(0);
        int staminaScore = Math.min((int) ((games / 150.0) * 100), 100);
        points.add(new PlayerRadarDTO.AbilityPoint("體力", staminaScore, 100));

        return new PlayerRadarDTO(id, name, team, "BATTER", points);
    }

    private PlayerRadarDTO mapToPitcherRadar(String id, String name, String team, JsonNode pitchingStat) {
        List<PlayerRadarDTO.AbilityPoint> points = new ArrayList<>();

        // 取得基礎投球數據
        double k9 = pitchingStat.path("strikeoutsPer9Inn").asDouble(0.0);
        double bb9 = pitchingStat.path("walksPer9Inn").asDouble(0.0);
        double h9 = pitchingStat.path("hitsPer9Inn").asDouble(0.0);
        double whip = pitchingStat.path("whip").asDouble(0.0);
        double ip = pitchingStat.path("inningsPitched").asDouble(0.0); // 局數
        // int wins = pitchingStat.path("wins").asInt(0);
        // int saves = pitchingStat.path("saves").asInt(0);

        // 1. 球威 (Stuff / Velocity)
        // 這裡用 H9 (每九局被安打數) 與 K9 綜合評估。K9 高且 H9 低代表球威好。
        // 公式概念: H9 越低越好(基準 6.0 分), K9 越高越好(基準 12.0 分)
        int stuffScore = (int) ((12.0 / (h9 + 1.0)) * 50 + (k9 / 12.0) * 50);
        points.add(new PlayerRadarDTO.AbilityPoint("球威", normalize(stuffScore), 100));

        // 2. 控球 (Control) -> BB/9
        // 基準: BB/9 <= 1.5 為滿分， >= 5.0 為 0 分
        // 越低越好
        int controlScore = (int) ((5.0 - bb9) / 3.5 * 100);
        points.add(new PlayerRadarDTO.AbilityPoint("控球", normalize(controlScore), 100));

        // 3. 變化球 (Breaking Ball) -> 因無詳細球種數據，暫以 K9 作為犀利度參考
        // 假設 K9 > 11 為頂尖變化球水準
        int breakingScore = (int) ((k9 / 11.0) * 100);
        points.add(new PlayerRadarDTO.AbilityPoint("變化球", normalize(breakingScore), 100));

        // 4. 體力 (Stamina) -> IP (投球局數)
        // 基準: 先發投手 180局滿分, 後援投手另計? 此處統一以賽季局數做簡易評估
        // 若為後援投手(IP少)，此項自然低，符合邏輯
        int staminaScore = (int) ((ip / 180.0) * 100);
        // 保底給個基礎分，避免後援投手太難看
        staminaScore = Math.max(staminaScore, 40);
        points.add(new PlayerRadarDTO.AbilityPoint("體力", normalize(staminaScore), 100));

        // 5. 奪三振 (Strikeout) -> 直接對應 K9
        // 基準: 15.0 為滿分 (現代高標)
        int kScore = (int) ((k9 / 15.0) * 100);
        points.add(new PlayerRadarDTO.AbilityPoint("奪三振", normalize(kScore), 100));

        // 6. 抗壓 (Clutch / Mental) -> WHIP (每局被上壘率)
        // 基準: WHIP 0.80 為滿分, 1.60 為 0 分
        int clutchScore = (int) ((1.60 - whip) / 0.80 * 100);
        points.add(new PlayerRadarDTO.AbilityPoint("抗壓", normalize(clutchScore), 100));

        return new PlayerRadarDTO(id, name, team, "PITCHER", points);
    }

    private int normalize(int value) {
        return Math.max(0, Math.min(value, 100));
    }

    private PlayerRadarDTO createEmptyRadar(String id, String name, String team, String role) {
        List<PlayerRadarDTO.AbilityPoint> emptyStats = new ArrayList<>();
        String[] subjects;

        if ("PITCHER".equals(role)) {
            subjects = new String[]{"球威", "控球", "變化球", "體力", "奪三振", "抗壓"};
        } else {
            subjects = new String[]{"打擊", "力量", "速度", "防守", "傳球", "體力"};
        }

        for (String subject : subjects) {
            emptyStats.add(new PlayerRadarDTO.AbilityPoint(subject, 0, 100));
        }
        return new PlayerRadarDTO(id, name, team, role, emptyStats);
    }

    /**
     * 取得熱區圖數據 (Heatmap)
     * @param personId 球員ID
     * @param type 類型 (e.g. "strike-zone", "hot-cold")
     * @return HeatmapDTO
     */
    public HeatmapDTO getHeatmapData(String personId, String type) {
        // 構建 URL: 取得 hotColdZones
        // MLB API 的 type 參數需使用 hotColdZones
        String url = String.format("%s%s?hydrate=stats(group=[hitting,pitching],type=[hotColdZones],season=%s)",
                baseUrl, personId, season);

        try {
            log.info("Requesting Heatmap Data: {}", url);
            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root == null || !root.has("people") || root.path("people").isEmpty()) {
                return createEmptyHeatmap("無熱區數據");
            }

            JsonNode playerNode = root.path("people").get(0);
            JsonNode statsArray = playerNode.path("stats");

            if (!statsArray.isArray() || statsArray.isEmpty()) {
                return createEmptyHeatmap("無熱區數據 (Stats Empty)");
            }

            // 判斷是否為投手 (用於判斷預設數據類型)
            String primaryPositionCode = playerNode.path("primaryPosition").path("code").asText();
            String primaryPositionType = playerNode.path("primaryPosition").path("type").asText();
            boolean isPitcher = "1".equals(primaryPositionCode) || "Pitcher".equalsIgnoreCase(primaryPositionType);

            List<HeatmapDTO.Zone> zones = new ArrayList<>();
            String dataTitle = "熱區分佈";
            boolean foundData = false;

            for (JsonNode groupNode : statsArray) {
                String typeDisplayName = groupNode.path("type").path("displayName").asText();

                // 尋找 hotColdZones 類型的數據
                if ("hotColdZones".equalsIgnoreCase(typeDisplayName)) {
                    String groupName = groupNode.path("group").path("displayName").asText();

                    // 邏輯優化: 如果 API 回傳了 group 名稱，才進行嚴格比對。
                    // 若 group 為空，則預設該數據即為我們需要的。
                    if (StringUtils.isNotBlank(groupName)) {
                        boolean targetPitching;
                        String typeLower = StringUtils.defaultString(type).toLowerCase();

                        if (typeLower.contains("pitch")) {
                            targetPitching = true;
                        } else if (typeLower.contains("hit")) {
                            targetPitching = false;
                        } else {
                            targetPitching = isPitcher;
                        }

                        if (targetPitching && !"pitching".equalsIgnoreCase(groupName)) continue;
                        if (!targetPitching && !"hitting".equalsIgnoreCase(groupName)) continue;
                    }

                    JsonNode splits = groupNode.path("splits");
                    if (splits.isArray() && !splits.isEmpty()) {
                        // splits[0] 為主要數據 (battingAverage / exitVelocity 等)
                        JsonNode split = splits.get(0);
                        JsonNode statNode = split.path("stat");

                        String statName = statNode.path("name").asText();
                        dataTitle = (isPitcher ? "投球" : "打擊") + "熱區 (" + statName + ")";

                        JsonNode zonesNode = statNode.path("zones");
                        if (zonesNode.isArray()) {
                            for (JsonNode zoneNode : zonesNode) {
                                String zoneId = zoneNode.path("zone").asText();

                                // 過濾並只保留 1-9 號位 (好球帶九宮格)
                                if (isStrikeZone(zoneId)) {
                                    // 安全解析數值，避免 "-" 字串導致錯誤
                                    double value = 0.0;
                                    String valStr = zoneNode.path("value").asText();
                                    if (StringUtils.isNotBlank(valStr) && !"-".equals(valStr)) {
                                        try {
                                            value = Double.parseDouble(valStr);
                                        } catch (NumberFormatException ignored) {}
                                    }

                                    String temp = zoneNode.path("temp").asText(); // "hot", "cold", "lukewarm"

                                    // 優先使用 API 提供的顏色 (rgba)，若無則用 temp 判斷
                                    String colorStr = zoneNode.has("color")
                                            ? zoneNode.path("color").asText()
                                            : resolveZoneColor(temp, value);

                                    zones.add(new HeatmapDTO.Zone(
                                            formatZoneId(zoneId), // 確保是 "1"~"9"
                                            value,
                                            colorStr
                                    ));
                                }
                            }
                            foundData = true;
                            // 找到一組可用數據後即跳出 (通常只有一組)
                            break;
                        }
                    }
                }
            }

            if (!foundData || zones.isEmpty()) {
                // 若只有 mock 連結，可以在這裡 fallback 回傳 mock，但現在我們回傳空提示
                log.warn("API 回傳成功但無符合條件的熱區數據 personId={} type={} isPitcher={}", personId, type, isPitcher);
                return createEmptyHeatmap("查無熱區數據");
            }

            return HeatmapDTO.builder()
                    .title(dataTitle)
                    .zones(zones)
                    .build();

        } catch (Exception e) {
            log.error("取得熱區數據失敗: personId={}", personId, e);
            return createEmptyHeatmap("讀取失敗");
        }
    }

    private boolean isStrikeZone(String zoneId) {
        try {
            int z = Integer.parseInt(zoneId);
            return z >= 1 && z <= 9;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 將 "01" 轉為 "1" 以符合前端預期 (若前端習慣 1-9)
    private String formatZoneId(String zoneId) {
        try {
            return String.valueOf(Integer.parseInt(zoneId));
        } catch (NumberFormatException e) {
            return zoneId;
        }
    }

    private String resolveZoneColor(String temp, double value) {
        // 優先使用 API 的 temp 屬性
        if ("hot".equalsIgnoreCase(temp)) return "#FF4444"; // 紅色 (熱)
        if ("cold".equalsIgnoreCase(temp)) return "#4444FF"; // 藍色 (冷)
        if ("lukewarm".equalsIgnoreCase(temp)) return "#FFAA00"; // 橙色/溫 (如果有)

        // 若 API 無 temp 標示，根據數值 (假設是打擊率/長打率) 給色 (Fallback)
        // 這裡數值範圍較難統一，僅作簡易分級
        if (value >= 0.350) return "#FF0000";
        if (value >= 0.300) return "#FF8800";
        if (value >= 0.250) return "#FFFF00";
        if (value >= 0.200) return "#8888FF";
        return "#0000FF";
    }

    private HeatmapDTO createEmptyHeatmap(String msg) {
        return HeatmapDTO.builder()
                .title(msg)
                .zones(Collections.emptyList())
                .build();
    }

    /**
     * 取得散佈圖數據 (Scatter Plot / Spray Chart)
     * 分別處理：
     * 1. spray-chart: 擊球落點 (PlayLog -> hitData.coordinates)
     * 2. exit-velocity: 初速仰角 (PlayLog -> hitData.launchSpeed/Angle)
     * 3. pitch-movement: 球種位移 (PitchLog -> details)
     */
    public ScatterPlotDTO getScatterPlotData(String personId, String type) {
        // 嘗試取得 playLog (打擊) 與 pitchLog (投球)
        // 備註: 若需詳細 Statcast 數據，可能需要更深入的 API (e.g. game feed)，
        // 這裡嘗試從 playLog 解析 hitData。
        String url = String.format("%s%s?hydrate=stats(group=[hitting,pitching],type=[playLog,pitchLog],season=%s)",
                baseUrl, personId, season);

        try {
            log.info("Requesting ScatterPlot Data: {}", url);
            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root == null || !root.has("people") || root.path("people").isEmpty()) {
                log.warn("API 回傳無人員數據，使用預設模擬資料");
                return getMockScatterPlotData(type);
            }

            JsonNode playerNode = root.path("people").get(0);
            JsonNode statsArray = playerNode.path("stats");

            if (statsArray.isEmpty()) {
                return getMockScatterPlotData(type);
            }

            List<ScatterPlotDTO.Point> points = new ArrayList<>();
            String title;
            String xLabel;
            String yLabel;

            if ("spray-chart".equalsIgnoreCase(type)) {
                title = "擊球落點分佈 (Season)";
                xLabel = "水平距離 (ft)";
                yLabel = "垂直距離 (ft)";
                points = parseSprayChart(statsArray);
            } else if ("pitch-movement".equalsIgnoreCase(type)) {
                title = "球種位移 (Season)";
                xLabel = "水平位移 (inches)";
                yLabel = "垂直位移 (inches)";
                points = parsePitchMovement(statsArray);
            } else {
                // Default: Exit Velocity vs Launch Angle
                title = "擊球初速 vs 仰角 (Season)";
                xLabel = "擊球初速 (mph)";
                yLabel = "擊球仰角 (deg)";
                points = parseExitVelocity(statsArray);
            }

            if (points.isEmpty()) {
                log.warn("解析後無數據點 (可能 API 未回傳 hitData)，切換至模擬數據顯示範例。Type={}", type);
                return getMockScatterPlotData(type);
            }

            return ScatterPlotDTO.builder()
                    .title(title)
                    .xLabel(xLabel)
                    .yLabel(yLabel)
                    .data(points)
                    .build();

        } catch (Exception e) {
            log.error("取得散佈圖數據失敗: personId={}, type={}", personId, type, e);
            return getMockScatterPlotData(type);
        }
    }

    private List<ScatterPlotDTO.Point> parseSprayChart(JsonNode statsArray) {
        List<ScatterPlotDTO.Point> points = new ArrayList<>();
        // 遍歷所有 playLog
        for (JsonNode statGroup : statsArray) {
            String type = statGroup.path("type").path("displayName").asText();
            if ("playLog".equalsIgnoreCase(type)) {
                JsonNode splits = statGroup.path("splits");
                for (JsonNode split : splits) {
                    JsonNode stat = split.path("stat");
                    JsonNode hitData = stat.path("hitData");
                    JsonNode coordinates = hitData.path("coordinates");

                    // 若有座標資料
                    if (!coordinates.isMissingNode()) {
                        double x = coordinates.path("coordX").asDouble(0.0);
                        double y = coordinates.path("coordY").asDouble(0.0);
                        // 轉換座標系 (API 原點可能在左上或本壘，需視前端需求調整，這裡假設直接回傳)
                        // 通常 MLB 座標: 本壘約 (125, 200) ? 需前端確認，這裡先回傳原始值

                        String event = stat.path("event").asText("Hit");
                        String desc = stat.path("description").asText("");

                        // 過濾: 只有安打或出局才畫? 這裡全畫
                        points.add(ScatterPlotDTO.Point.builder()
                                .x(x)
                                .y(y)
                                .type(event)
                                .info(desc)
                                .build());
                    }
                }
            }
        }
        return points;
    }

    private List<ScatterPlotDTO.Point> parseExitVelocity(JsonNode statsArray) {
        List<ScatterPlotDTO.Point> points = new ArrayList<>();
        for (JsonNode statGroup : statsArray) {
            String type = statGroup.path("type").path("displayName").asText();
            if ("playLog".equalsIgnoreCase(type)) {
                JsonNode splits = statGroup.path("splits");
                for (JsonNode split : splits) {
                    JsonNode stat = split.path("stat");
                    JsonNode hitData = stat.path("hitData");

                    if (!hitData.isMissingNode()) {
                        double speed = hitData.path("launchSpeed").asDouble(0.0);
                        double angle = hitData.path("launchAngle").asDouble(0.0);

                        // 過濾無效數據
                        if (speed > 0) {
                            String event = stat.path("event").asText("Batted Ball");
                            String desc = String.format("%.1f mph / %.1f deg", speed, angle);

                            points.add(ScatterPlotDTO.Point.builder()
                                    .x(speed)
                                    .y(angle)
                                    .type(event)
                                    .info(desc)
                                    .build());
                        }
                    }
                }
            }
        }
        return points;
    }

    private List<ScatterPlotDTO.Point> parsePitchMovement(JsonNode statsArray) {
        List<ScatterPlotDTO.Point> points = new ArrayList<>();
        // PitchLog 目前 API 較少回傳詳細位移，這裡預留介面
        // 若有 pitchData -> coordinates / breaks
        return points;
    }

    private ScatterPlotDTO getMockScatterPlotData(String type) {
        List<ScatterPlotDTO.Point> points = new ArrayList<>();

        if ("spray-chart".equalsIgnoreCase(type)) {
            // 模擬落點分佈
            for (int i = 0; i < 50; i++) {
                points.add(ScatterPlotDTO.Point.builder()
                        .x((Math.random() * 200) - 100) // 假設球場座標
                        .y(Math.random() * 200)
                        .type(Math.random() > 0.8 ? "Home Run" : "Hit")
                        .info("Distance: 400ft")
                        .build());
            }
            return ScatterPlotDTO.builder()
                    .title("擊球落點分佈 (Mock)")
                    .xLabel("Horizontal Distance")
                    .yLabel("Vertical Distance")
                    .data(points)
                    .build();
        } else if ("pitch-movement".equalsIgnoreCase(type)) {
             // 模擬球種位移
             for (int i = 0; i < 30; i++) {
                 boolean isSlider = Math.random() > 0.5;
                 points.add(ScatterPlotDTO.Point.builder()
                         .x(isSlider ? 5 + Math.random() * 5 : -5 - Math.random() * 5)
                         .y(Math.random() * 10)
                         .type(isSlider ? "Slider" : "Fastball")
                         .info("Break info")
                         .build());
             }
             return ScatterPlotDTO.builder()
                     .title("球種位移 (Mock)")
                     .xLabel("Horizontal Break")
                     .yLabel("Vertical Break")
                     .data(points)
                     .build();
        } else {
            // 預設: Exit Vel vs Launch Angle
            for (int i = 0; i < 50; i++) {
                points.add(ScatterPlotDTO.Point.builder()
                        .x(60 + Math.random() * 50) // Exit Vel 60-110 mph
                        .y(-10 + Math.random() * 50) // Launch Angle -10 to 40 deg
                        .type("Batted Ball")
                        .info("98 mph / 15 deg")
                        .build());
            }
            return ScatterPlotDTO.builder()
                    .title("擊球初速 vs 仰角 (Mock)")
                    .xLabel("Exit Velocity (mph)")
                    .yLabel("Launch Angle (deg)")
                    .data(points)
                    .build();
        }
    }

    /**
     * 取得賽季趨勢圖數據 (Trend / Line Chart)
     * @param personId 球員ID
     * @param metric 指標 (e.g. "ops", "era", "avg")
     * @return TrendChartDTO
     */
    public TrendChartDTO getTrendData(String personId, String metric) {
        // TODO: 串接 MLB gameLog 數據
        List<String> labels = new ArrayList<>();
        List<Number> dataPoints = new ArrayList<>();

        // 模擬最近 15 場比賽
        double currentVal = 0.300;
        for (int i = 1; i <= 15; i++) {
            labels.add("G" + i);
            currentVal += (Math.random() - 0.5) * 0.050; // 隨機波動
            dataPoints.add(currentVal);
        }

        TrendChartDTO.Series series = TrendChartDTO.Series.builder()
                .name(metric.toUpperCase())
                .data(dataPoints)
                .build();

        return TrendChartDTO.builder()
                .title("近 15 場 " + metric.toUpperCase() + " 趨勢 (Mock)")
                .labels(labels)
                .series(List.of(series))
                .build();
    }

    /**
     * 取得配球比例數據 (Pie Chart)
     * @param personId 球員ID
     * @return List<PieChartDTO>
     */
    public List<PieChartDTO> getPitchUsageData(String personId) {
        // TODO: 串接 MLB pitchArsenal 數據
        // 模擬數據
        return List.of(
                new PieChartDTO("4-Seam Fastball", 45.0),
                new PieChartDTO("Slider", 25.0),
                new PieChartDTO("Curveball", 15.0),
                new PieChartDTO("Changeup", 15.0)
        );
    }
}
