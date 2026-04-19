# SportInfo - MLB/WBC 棒球數據分析平台

SportInfo 是一個用於 MLB 與 WBC 球員數據分析的全端應用系統，提供前端視覺化儀表板與後端 RESTful API，協助使用者從多個維度分析球員表現。

系統目前支援球員雷達圖、趨勢圖、熱區圖、散佈圖、球隊名單查詢與球員比較等功能，適合用於運動數據分析展示、個人作品集與全端專案實作。

## Features

- 球員數據儀表板
- 球員能力雷達圖
- 球員比較分析
- 熱區圖與散佈圖視覺化
- MLB 球隊與球員資料查詢
- Swagger API 文件

## Tech Stack

### Frontend

- React 19
- Vite
- Tailwind CSS 4
- Recharts
- Axios

### Backend

- Spring Boot 3
- Java 21
- Maven
- JPA / Hibernate
- MyBatis
- SpringDoc OpenAPI

## Project Structure

```text
sportinfo/
├── README.md
├── frontend/
└── backend/
```

## Quick Start

### 1. 啟動後端

進入 `backend` 資料夾後執行：

```bash
mvn clean install
mvn spring-boot:run
```

後端預設位址：

```text
http://localhost:8080
```

### 2. 啟動前端

進入 `frontend` 資料夾後執行：

```bash
npm install
npm run dev
```

前端預設位址：

```text
http://localhost:5173
```

## API Documentation

Swagger UI：

```text
http://localhost:8080/swagger-ui/index.html
```

## System Architecture

- Frontend 負責資料視覺化與互動操作
- Backend 負責整合 MLB Stats API、資料處理與 RESTful API 提供
- 前端透過 HTTP 呼叫後端 API 顯示圖表與分析結果

## Demo Screenshots

以下為預計補上的展示項目：

- Dashboard
- Radar Chart
- Heatmap
- Scatter Plot

## Future Improvements

- 加入球員搜尋與篩選
- 支援使用者登入與收藏功能
- 加入部署環境設定
- 支援更多進階棒球指標

---

## 附錄：Frontend README 範本

### WBC Analytics Frontend

這是 SportInfo 平台的前端應用，負責呈現 MLB 與 WBC 球員數據分析結果，提供儀表板、雷達圖、比較圖與其他圖表元件，讓使用者能以視覺化方式快速理解球員表現。

#### Features

- 球員數據儀表板
- 球員能力雷達圖
- 球員比較圖表
- 多種圖表小工具整合
- 球隊資料與球員名單查詢

#### Tech Stack

- React 19
- Vite
- Tailwind CSS 4
- Recharts
- Axios
- Lucide React

#### Development Setup

##### 安裝依賴

```bash
npm install
```

##### 啟動開發環境

```bash
npm run dev
```

前端預設執行於：

```text
http://localhost:5173
```

##### 建置正式版本

```bash
npm run build
```

#### Backend Requirement

本專案需要搭配 Spring Boot 後端 API 執行。請先確認後端服務已啟動於：

```text
http://localhost:8080
```

#### Environment Variables

建議使用環境變數管理 API 位址，例如：

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

之後可讓前端統一透過環境變數切換本機、測試與正式環境。

#### Project Structure

```text
src/
├── api/
├── assets/
├── components/
├── data/
├── App.jsx
└── main.jsx
```

#### Main Components

- PlayerDashboard
- PlayerRadarChart
- PlayerComparisonChart
- PlayerChartWidgets

#### Notes

- 目前前端主要透過後端 API 取得球員雷達圖、圖表資料與球隊名單資訊。
- 若後端未啟動，部分功能將無法正常顯示。

---

## 附錄：Backend README 範本

### SportInfo Backend

這是 SportInfo 平台的後端服務，基於 Spring Boot 建立，負責整合 MLB 資料來源、處理球員分析邏輯，並透過 RESTful API 提供前端使用。

#### Features

- MLB 球隊名單查詢
- 球員雷達圖數據計算
- 熱區圖資料提供
- 散佈圖與趨勢圖資料提供
- 投手球種使用比例分析
- Swagger API 文件整合

#### Tech Stack

- Spring Boot 3
- Java 21
- Maven
- Spring Web
- Spring Data JPA
- MyBatis
- Lombok
- Jackson
- SpringDoc OpenAPI

#### Run Application

##### 建置專案

```bash
mvn clean install
```

##### 啟動專案

```bash
mvn spring-boot:run
```

預設服務位址：

```text
http://localhost:8080
```

#### Swagger

API 文件位址：

```text
http://localhost:8080/swagger-ui/index.html
```

#### API Base Path

```text
/api/v1
```

#### Example Endpoints

- `GET /api/v1/teams/{teamId}/roster`
- `GET /api/v1/players/{id}/radar`
- `GET /api/v1/players/{id}/charts/heatmap`
- `GET /api/v1/players/{id}/charts/scatter`
- `GET /api/v1/players/{id}/charts/trend`
- `GET /api/v1/players/{id}/charts/usage`

#### Data Source

本系統部分資料來自 MLB Stats API，並在後端進行資料整合與格式轉換後提供給前端使用。

#### Frontend Integration

前端預期透過 HTTP 呼叫本後端服務，建議於本機開發時搭配：

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`

#### Configuration

主要設定可放置於 `application.yaml`，例如：

- 伺服器埠號
- MLB API Base URL
- logging level
- CORS 設定

#### Future Work

- 增加快取機制
- 支援更多球員進階指標
- 加入使用者登入與收藏
- 補齊單元測試與整合測試
