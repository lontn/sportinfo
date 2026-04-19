# WBC Analytics Frontend (WBC 數據分析前端)

這是一個專為分析與視覺化棒球選手數據所建立的前端專案。此專案提供直觀的儀表板、雷達圖與圖表工具，幫助使用者比較與分析球員的表現。

## ✨ 主要功能 (Features)

*   **📊 數據儀表板 (Player Dashboard)**: 提供綜合性的選手數據預覽與指標分析。
*   **🕸️ 雷達圖分析 (Player Radar Chart)**: 透過多維度雷達圖快速檢視球員綜合能力 (如打擊、力量、速度等)。
*   **📈 選手比較 (Player Comparison)**: 支援多位選手的數據對比功能。
*   **⚾ MLB 球隊資料 (MLB Teams)**: 內建各球隊的相關分析與分類。

## 🛠️ 開發技術棧 (Tech Stack)

*   **框架**: [React 19](https://react.dev/) + [Vite](https://vitejs.dev/)
*   **樣式與 UI**: [Tailwind CSS 4](https://tailwindcss.com/)
*   **圖表套件**: [Recharts](https://recharts.org/)
*   **圖示庫**: [Lucide React](https://lucide.dev/)
*   **API 請求**: [Axios](https://axios-http.com/)

## 🚀 快速開始 (Getting Started)

請確保您的系統已安裝相應版本的 Node.js。

### 1. 安裝依賴 (Install Dependencies)

```bash
npm install
```

### 2. 啟動開發伺服器 (Start Development Server)

```bash
npm run dev
```

成功啟動後，開啟瀏覽器並前往 `http://localhost:5173` (預設 Vite 埠號) 即可預覽專案。

### 3. 編譯生產版本 (Build for Production)

```bash
npm run build
```

編譯完成的檔案將會生成在 `dist` 資料夾中。

## 📂 專案結構 (Project Structure)

```text
src/
├── api/             # API 請求邏輯 (Axios 等設定)
├── assets/          # 靜態資源 (圖檔、Icon 等)
├── components/      # 共用與主要前端元件 (例如：儀表板、雷達圖)
├── data/            # 本地 Mock 資料與常數設定 (例如：MLB 球隊名單)
├── App.jsx          # 應用程式主入口
└── main.jsx         # React 渲染註冊點
```

