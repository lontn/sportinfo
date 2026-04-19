import axios from 'axios';

/**
 * 建立一個 Axios 實例，類似於 Java 中的 RestClient 配置。
 * 這樣未來如果 API 修改了 Base URL 或需要加 Token，改這裡就好。
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1/players', // 指向你的 Spring Boot 後端
  timeout: 5000, // 5 秒超時，避免請求卡死
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 獲取選手雷達圖數據
 * @param {string} playerId 選手的 MLB ID (例如: 660271)
 */
export const fetchPlayerRadarData = async (playerId) => {
  try {
    // 呼叫你在 Spring Boot 寫好的 GET /{id}/radar 端點
    const response = await api.get(`/${playerId}/radar`);
    console.log(`成功獲取選手 ${playerId} 的數據:`, response);
    // 成功回傳數據
    return response.data;
  } catch (error) {
    // 這裡處理錯誤，類似 Java 的 try-catch
    console.error(`無法獲取選手 ${playerId} 的數據:`, error.response?.data || error.message);
    
    // 可以選擇拋出錯誤給前端 UI 處理
    throw error;
  }
};

/**
 * 獲取圖表相關數據 (Heatmap, Scatter, Trend, Pie)
 * 統一管理圖表相關的 API
 */
export const fetchPlayerChart = async (playerId, chartType, params = {}) => {
  try {
    const response = await api.get(`/${playerId}/charts/${chartType}`, { params });
    return response.data;
  } catch (error) {
    console.error(`無法獲取選手 ${playerId} 的 ${chartType} 數據:`, error);
    throw error;
  }
};

/**
 * 建立另一個 Axios 實例給 teams API
 */
const teamApi = axios.create({
  baseURL: 'http://localhost:8080/api/v1/teams',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * 獲取散佈圖數據 (Spray Chart, Exit Velocity, Pitch Movement)
 * 修正後的路徑: /api/v1/players/{id}/charts/scatter?type=...
 * @param {string} playerId 選手ID
 * @param {string} type 圖表類型: 'spray-chart', 'exit-velocity', 'pitch-movement'
 */
export const fetchScatterStats = async (playerId, type = 'spray-chart') => {
  try {
    const response = await api.get(`/${playerId}/charts/scatter`, { 
      params: { type } 
    });
    return response.data;
  } catch (error) {
    console.error(`無法獲取散佈圖數據 (${type}):`, error);
    throw error;
  }
};

/**
 * 獲取球隊名單
 * @param {string} teamId 球隊的 ID (例如: 119)
 */
export const fetchTeamRoster = async (teamId) => {
  try {
    const response = await teamApi.get(`/${teamId}/roster`);
    return response.data;
  } catch (error) {
    console.error(`無法獲取球隊 ${teamId} 的名單:`, error);
    throw error;
  }
};

export default {
  fetchPlayerRadarData,
  fetchPlayerChart,
  fetchScatterStats,
  fetchTeamRoster
};