# SportInfo - MLB/WBC 棒球數據分析平台

這是一個基於 **Spring Boot 3.x** 的後端專案，旨在提供 MLB (美國職棒大聯盟) 與 WBC (世界棒球經典賽) 的進階數據分析服務。本系統整合各類棒球數據，並透過 RESTful API 提供給前端應用程式 (如 React) 進行視覺化呈現，包含雷達圖、熱區圖與散佈圖等賽伯密學 (Sabermetrics) 分析圖表。

## 🚀 專案特點 (Key Features)

*   **MLB 數據整合**: 直接串接對接 MLB Stats API，取得球員名單 (Roster)、攻守數據與個人資料。
*   **進階圖表支援**:
    *   📊 **雷達圖 (Radar Chart)**: 分析球員五項戰力 (打擊、力量、速度、防守、傳球 等)。詳見 [SKILL.md](SKILL.md) 了解詳細定義。
    *   🔥 **熱區圖 (Heatmap)**: 呈現打者好球帶攻擊熱點與弱點 (Hot/Cold Zones)。
    *   ☄️ **散佈圖 (Scatter Plot)**: 分析擊球初速/仰角分佈與擊球落點 (Spray Chart)。
    *   📈 **趨勢圖 (Trend Chart)**: 追蹤球員賽季 OPS/ERA 起伏趨勢。
    *   🥧 **圓餅圖 (Pie Chart)**: 投手球種使用比例分析。
*   **現代化開發規範**:
    *   使用 Java 17+ 新特性 (Records, Switch Expressions)。
    *   嚴格遵循 RESTful API 設計原則。
    *   整合 Swagger (OpenAPI 3) 自動產生 API 文件。
    *   跨來源資源共用 (CORS) 設定，支援前端開發環境。

## 🛠️ 技術堆疊 (Technology Stack)

*   **核心框架**: Spring Boot 3.x (Spring Web, Spring Data JPA)
*   **程式語言**: Java 21
*   **建置工具**: Maven
*   **資料庫存取**:
    *   **JPA / Hibernate**: 標準 CRUD 操作。
    *   **MyBatis**: 複雜 SQL 查詢與映射 (配合 XML Mapper)。
*   **工具庫**:
    *   **Lombok**: 簡化 Java Boilerplate code (DTO, Entity, Logging)。
    *   **Apache Commons**: Lang3 (字串處理), Collections4 (集合處理)。
    *   **Jackson**: JSON 資料處理 (支援 Java Time API JSR-310)。
*   **API 文件**: SpringDoc OpenAPI (Swagger UI)。

## ⚙️ 環境需求 (Prerequisites)

*   **Java Development Kit (JDK)**: 17 或 21。
*   **Maven**: 3.8+。
*   **IDE**: IntelliJ IDEA (推薦) 或 Eclipse/VS Code。

## 📦 安裝與執行 (Installation & Running)

1.  **複製專案 (Clone Repository)**
    ```bash
    git clone https://github.com/your-username/sportinfo.git
    cd sportinfo
    ```

2.  **建置專案 (Build)**
    ```bash
    mvn clean install
    ```

3.  **執行應用程式 (Run)**
    ```bash
    mvn spring-boot:run
    ```
    或者是直接執行 `SportinfoApplication.java` 的 `main` 方法。

4.  **驗證服務**
    *   API 服務啟動於: `http://localhost:8080`
    *   **Swagger API 文件**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 📖 API 文件說明 (API Endpoints)

以下列出部分關鍵 API 端點，完整內容請參閱 Swagger UI。

### 🧢 球隊相關 (Teams)
*   `GET /api/v1/teams/{teamId}/roster`
    *   取得指定球隊 (Team ID) 的選手名單。

### ⚾ 球員數據與圖表 (Player Charts)
*   `GET /api/v1/players/{id}/radar`
    *   取得球員能力雷達圖數據 (例如: 大谷翔平 `id=660271`)。
*   `GET /api/v1/players/{id}/charts/heatmap`
    *   取得熱區圖數據 (參數 `type=strike-zone` 等)。
*   `GET /api/v1/players/{id}/charts/scatter`
    *   取得散佈圖數據 (參數 `type=exit-velocity` 或 `spray-chart`)。
*   `GET /api/v1/players/{id}/charts/trend`
    *   取得賽季趨勢數據。
*   `GET /api/v1/players/{id}/charts/usage`
    *   取得投手配球比例。

## 🔧 設定說明 (Configuration)

主要設定檔位於 `src/main/resources/application.yaml`。

```yaml
mlb:
  api:
    base-url: "https://statsapi.mlb.com/api/v1/people/"
    roster-url-template: "https://statsapi.mlb.com/api/v1/teams/{teamId}/roster"
    season: 2025

logging:
  level:
    com.wbc.analytics.sportinfo: DEBUG
  file:
    name: logs/sportinfo-app.log
```

## 📁 專案結構 (Project Structure)

```
src/main/java/com/wbc/analytics/sportinfo
├── config      # 設定檔 (WebConfig, JacksonConfig, RestTemplateConfig)
├── controller  # API 控制器 (處理 HTTP 請求)
├── model       # 資料模型
│   ├── dto     # 資料傳輸物件 (Data Transfer Objects)
│   └── entity  # 資料庫實體 (JPA Entities)
├── repository  # 資料存取層 (JPA Repository & MyBatis Mapper)
├── service     # 業務邏輯層 (Service Classes)
└── SportinfoApplication.java # 啟動類別
```

## 📝 開發者指南 (Developer Guide)

*   **DTO 規範**: 請優先使用 Java `record` 或 Lombok `@Data`，日期欄位使用 Java Time API (`LocalDate`, `LocalDateTime`)。
*   **Service 規範**: 業務邏輯請寫在 Service 層，並使用介面 (Interface) 設計模式。
*   **Controller 規範**: 僅處理路由與參數驗證，不包含業務邏輯。回傳統一使用 `ResponseEntity`。
*   **Git Commit**: 請遵循 Conventional Commits 規範 (feat, fix, refactor, docs 等)。

---
**Copyleft** © 2026 SportInfo Analytics Team.

