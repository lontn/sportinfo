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

### Dashboard

### Radar Chart

### Heatmap

### Scatter Plot

你可以在這一段放 GitHub 圖片連結或 `screenshots` 資料夾截圖。

## Future Improvements

- 加入球員搜尋與篩選
- 支援使用者登入與收藏功能
- 加入部署環境設定
- 支援更多進階棒球指標
