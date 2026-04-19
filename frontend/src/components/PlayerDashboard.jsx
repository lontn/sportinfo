import React, { useState, useEffect, useMemo } from 'react';
import { MOCK_PLAYERS } from '../data/mockData';
import { MLB_TEAMS } from '../data/mlbTeams';
import PlayerComparisonChart from './PlayerComparisonChart';
import { PlayerHeatmap, PlayerScatterChart, PlayerTrendChart, PlayerPieChart } from './PlayerChartWidgets';
import { User, Plus, Check, Loader, Users, Search, Filter, Activity, PieChart, TrendingUp, Grid } from 'lucide-react';
import { fetchPlayerRadarData, fetchTeamRoster, fetchPlayerChart, fetchScatterStats } from '../api/playerApi';

// Mock Data for fallback
const MOCK_HEATMAP = {
  title: "打擊熱區",
  zones: [
    { zoneId: "1", value: 0.312, color: "#ef4444" }, { zoneId: "2", value: 0.250, color: "#3b82f6" }, { zoneId: "3", value: 0.280, color: "#64748b" },
    { zoneId: "4", value: 0.305, color: "#ef4444" }, { zoneId: "5", value: 0.290, color: "#ef4444" }, { zoneId: "6", value: 0.220, color: "#3b82f6" },
    { zoneId: "7", value: 0.210, color: "#3b82f6" }, { zoneId: "8", value: 0.240, color: "#64748b" }, { zoneId: "9", value: 0.280, color: "#64748b" }
  ]
};
const MOCK_TREND = {
  title: "近 15 場 OPS 趨勢",
  labels: ["G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "G10", "G11", "G12", "G13", "G14", "G15"],
  series: [{ name: "OPS", data: [0.850, 0.860, 0.820, 0.790, 0.810, 0.950, 1.050, 1.100, 0.980, 0.920, 0.880, 0.900, 0.930, 0.950, 1.020] }]
};
const MOCK_SCATTER = {
  title: "擊球落點分佈", xLabel: "Horizontal Distance", yLabel: "Vertical Distance",
  data: [
    { x: -50, y: 180, type: "Hit", info: "Double" }, { x: 80, y: 380, type: "Home Run", info: "HR 400ft" },
    { x: 20, y: 250, type: "Out", info: "Flyout" }, { x: -100, y: 320, type: "Out", info: "Flyout" }, { x: 50, y: 150, type: "Single", info: "Single" }
  ]
};
const MOCK_PIE = [
  { name: "4-Seam", value: 45 }, { name: "Slider", value: 25 }, { name: "Curve", value: 15 }, { name: "Changeup", value: 15 }
];

const PlayerDashboard = () => {
  const [selectedTeamId, setSelectedTeamId] = useState('119'); // 預設選擇道奇隊
  const [teamRoster, setTeamRoster] = useState([]); // 儲存該球隊的選手名單
  const [isRosterLoading, setIsRosterLoading] = useState(false);
  const [teamError, setTeamError] = useState(null);
  
  // 新增篩選器狀態 (all: 全部, pitcher: 投手, fielder: 野手)
  const [positionFilter, setPositionFilter] = useState('all');
  
  const [selectedPlayerIds, setSelectedPlayerIds] = useState([]); 
  const [playerDataMap, setPlayerDataMap] = useState({}); // 儲存從 API 取回的選手雷達資料
  const [loadingIds, setLoadingIds] = useState([]); // 記錄正在 Loading 雷達資料的選手 ID

  // Tab 狀態: 'heatmap', 'pie', 'trend', 'scatter'
  const [activeTab, setActiveTab] = useState('heatmap');
  const [scatterType, setScatterType] = useState('spray-chart'); // 散佈圖子類型: 'spray-chart', 'exit-velocity', 'pitch-movement'

  // 進階圖表資料狀態
  // 我們只針對「最後選中」或「列表第一位」的選手顯示進階圖表，避免畫面混亂
  const [chartData, setChartData] = useState({
    heatmap: null,
    scatter: null,
    trend: null,
    pie: null,
    loading: false
  });

  // 1. 當選擇的球隊改變時，拉取球隊名單
  useEffect(() => {
    const fetchRoster = async () => {
      setIsRosterLoading(true);
      setTeamError(null);
      try {
        const rosterData = await fetchTeamRoster(selectedTeamId);
        setTeamRoster(rosterData);
      } catch (error) {
        console.error("無法取得球隊名單:", error);
        setTeamError("無法載入球隊名單，請稍後再試。");
        // 如果失敗，可以塞一些假資料或清空
        setTeamRoster([]); 
      } finally {
        setIsRosterLoading(false);
      }
    };
    
    fetchRoster();
  }, [selectedTeamId]);

  // 1.5. 根據篩選器過濾名單
  const filteredRoster = useMemo(() => {
    return teamRoster.filter(player => {
      if (positionFilter === 'all') return true;
      
      // 判斷是否為投手 (縮寫為 P, SP, RP, CP 等)
      const isPitcher = ['P', 'SP', 'RP', 'CP'].includes(player.positionAbbreviation);
      
      if (positionFilter === 'pitcher') return isPitcher;
      if (positionFilter === 'fielder') return !isPitcher; // 非純投手的都算野手 (包含 TWP 二刀流)
      return true;
    });
  }, [teamRoster, positionFilter]);

  // 2. 當選中的選手 ID 改變時，如果沒有雷達圖資料則向 API 請求
  useEffect(() => {
    const fetchMissingData = async () => {
      const missingIds = selectedPlayerIds.filter(id => !playerDataMap[id] && !loadingIds.includes(id));
      
      if (missingIds.length === 0) return;

      setLoadingIds(prev => [...prev, ...missingIds]);

      for (const id of missingIds) {
        try {
          const data = await fetchPlayerRadarData(id);
          setPlayerDataMap(prev => ({ ...prev, [id]: data }));
        } catch (error) {
          console.error(`無法獲取選手 ${id} 的數據:`, error);
          // 若 API 失敗，可選擇 fallback 到 MOCK_PLAYERS 或顯示預設錯誤結構
          const fallbackData = MOCK_PLAYERS.find(p => p.id === id);
          if (fallbackData) {
            setPlayerDataMap(prev => ({ ...prev, [id]: fallbackData }));
          }
        }
      }

      setLoadingIds(prev => prev.filter(lId => !missingIds.includes(lId)));
    };

    fetchMissingData();
  }, [selectedPlayerIds]);

  const handlePlayerToggle = (id) => {
    if (selectedPlayerIds.includes(id)) {
      setSelectedPlayerIds(prev => prev.filter(pid => pid !== id));
    } else {
      setSelectedPlayerIds(prev => [...prev, id]);
    }
  };

  // 生成圖表所需的選手陣列 (只傳入已有資料的選手)
  const selectedPlayers = selectedPlayerIds
    .map(id => playerDataMap[id])
    .filter(p => p !== undefined);

  // 決定要詳細顯示圖表的選手 ID (預設取最後一個選中的，方便用戶查看剛點選的)
  const focusPlayerId = selectedPlayerIds.length > 0 ? selectedPlayerIds[selectedPlayerIds.length - 1] : null;

  // 3. 獲取進階圖表資料
  useEffect(() => {
    const fetchCharts = async () => {
      if (!focusPlayerId) {
        setChartData({ heatmap: null, scatter: null, trend: null, pie: null, loading: false });
        return;
      }

      setChartData(prev => ({ ...prev, loading: true }));
      try {
        // 並行請求所有圖表 (若失敗則使用 Mock)
        // 注意: Scatter 圖表改用 fetchScatterStats 請求
        const [heatmap, scatter, trend, pie] = await Promise.all([
          fetchPlayerChart(focusPlayerId, 'heatmap').catch(() => null),
          fetchScatterStats(focusPlayerId, scatterType).catch(() => null),
          fetchPlayerChart(focusPlayerId, 'trend', { metric: 'ops' }).catch(() => null),
          fetchPlayerChart(focusPlayerId, 'usage').catch(() => null),
        ]);

        setChartData({
          heatmap: heatmap || MOCK_HEATMAP,
          scatter: scatter || MOCK_SCATTER,
          trend: trend || MOCK_TREND,
          pie: pie || MOCK_PIE,
          loading: false
        });
      } catch (error) {
        console.error("無法加載詳細圖表", error);
        setChartData(prev => ({ ...prev, loading: false }));
      }
    };

    fetchCharts();
  }, [focusPlayerId, scatterType]); // 當 focusPlayerId 或 scatterType 改動時重新抓取

  // 判斷是否同時混合了打者與投手 (若混合，可以給予小提示)
  const selectedRoles = new Set(selectedPlayers.map(p => p.role).filter(Boolean));
  const hasMixedRoles = selectedRoles.has('PITCHER') && selectedRoles.has('BATTER');
  
  // 獲取當前聚焦的選手物件，用於顯示名字
  const focusPlayer = playerDataMap[focusPlayerId];

  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-8 w-full max-w-[1600px] mx-auto pb-10">
      {/* 左側：球隊與選手列表 */}
      <div className="md:col-span-1 flex flex-col gap-4 h-fit">
        
        {/* 球隊選擇區 */}
        <div className="bg-slate-800 rounded-xl border border-slate-700 p-5">
           <h2 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
            <Users className="w-5 h-5 text-emerald-400" />
            選擇球隊
          </h2>
          <select 
            value={selectedTeamId} 
            onChange={(e) => setSelectedTeamId(e.target.value)}
            className="w-full bg-slate-900 border border-slate-600 text-slate-200 rounded-lg p-3 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition"
          >
            {MLB_TEAMS.map(team => (
              <option key={team.id} value={team.id}>{team.name}</option>
            ))}
          </select>
        </div>

        {/* 選手名單區 */}
        <div className="bg-slate-800 rounded-xl border border-slate-700 p-5 h-[600px] flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <User className="w-5 h-5 text-blue-400" />
              球隊名單
              {isRosterLoading && <Loader className="w-4 h-4 animate-spin text-slate-400 ml-2" />}
            </h2>
            {selectedPlayerIds.length > 0 && (
              <button
                onClick={() => setSelectedPlayerIds([])}
                className="text-xs text-slate-400 hover:text-red-400 transition-colors px-2 py-1 rounded bg-slate-700/50 hover:bg-red-500/10"
              >
                清除全部
              </button>
            )}
          </div>

          {/* 篩選器按鈕 */}
          <div className="flex bg-slate-900 rounded-lg p-1 mb-4 border border-slate-700">
            <button
              onClick={() => setPositionFilter('all')}
              className={`flex-1 text-xs font-medium py-1.5 rounded-md transition-colors ${positionFilter === 'all' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
            >
              全部
            </button>
            <button
              onClick={() => setPositionFilter('fielder')}
              className={`flex-1 text-xs font-medium py-1.5 rounded-md transition-colors ${positionFilter === 'fielder' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
            >
              野手
            </button>
            <button
              onClick={() => setPositionFilter('pitcher')}
              className={`flex-1 text-xs font-medium py-1.5 rounded-md transition-colors ${positionFilter === 'pitcher' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
            >
              投手
            </button>
          </div>
          
          <div className="flex-1 overflow-y-auto pr-2 space-y-2 custom-scrollbar">
            {teamError && <div className="text-red-400 text-sm">{teamError}</div>}
            
            {!isRosterLoading && filteredRoster.length === 0 && !teamError && (
               <div className="text-slate-500 text-sm text-center mt-10">找不到符合條件的球員</div>
            )}

            {filteredRoster.map(player => {
              const isSelected = selectedPlayerIds.includes(player.id);
              const isLoadingRadar = loadingIds.includes(player.id);
              
              return (
                <div 
                  key={player.id}
                  onClick={() => !isLoadingRadar && handlePlayerToggle(player.id)}
                  className={`
                    p-3 rounded-lg flex items-center justify-between transition-all group
                    ${isLoadingRadar ? 'cursor-wait opacity-70' : 'cursor-pointer'}
                    ${isSelected 
                      ? 'bg-blue-900/40 border-l-4 border-blue-500' 
                      : 'bg-slate-700/30 hover:bg-slate-700/80 border-l-4 border-transparent'}
                  `}
                >
                  <div className="overflow-hidden flex items-center gap-3">
                    <span className="text-[11px] font-bold tracking-wider bg-slate-800 text-slate-400 w-8 text-center py-1 rounded shrink-0">
                      {player.positionAbbreviation}
                    </span>
                    <h3 className={`font-semibold text-sm ${isSelected ? 'text-blue-200' : 'text-slate-200 group-hover:text-white'}`}>
                      {player.fullName || player.name}
                    </h3>
                  </div>
                  
                  <div className={`
                    w-6 h-6 shrink-0 rounded-full flex items-center justify-center transition-colors
                    ${isSelected ? 'bg-blue-500 text-white' : 'bg-slate-700 text-slate-400'}
                  `}>
                    {isLoadingRadar ? (
                      <Loader size={12} className="animate-spin text-slate-300" />
                    ) : isSelected ? (
                      <Check size={12} />
                    ) : (
                      <Plus size={12} />
                    )}
                  </div>
                </div>
              );
            })}
          </div>
          
          <div className="mt-4 pt-4 border-t border-slate-700/50 text-xs text-slate-400">
            已勾選 <span className="text-blue-400 font-bold">{selectedPlayerIds.length}</span> 位選手進行分析
          </div>
        </div>
      </div>

      {/* 右側：雷達圖 Dashboard */}
      <div className="md:col-span-3">
        {hasMixedRoles && (
          <div className="mb-4 bg-yellow-500/10 border border-yellow-500/50 text-yellow-200 px-4 py-2 rounded-lg text-sm flex items-center gap-2">
            ⚠️ 您同時選擇了「投手」與「打者」，雷達圖會將兩種指標合併顯示，可能會稍微擁擠喔！
          </div>
        )}
        <PlayerComparisonChart players={selectedPlayers} />
        
        {/* 下方可以放一些數據表格或其他詳細資訊 */}
        {selectedPlayers.length > 0 && (
          <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {selectedPlayers.map(player => (
              <div key={player.id} className="bg-slate-800/80 p-4 rounded-lg border border-slate-700/50 relative">
                 {/* 顯示選手類型 Badge */}
                 {player.role && (
                   <div className={`absolute top-4 right-4 text-[10px] font-bold px-2 py-0.5 rounded ${
                     player.role === 'PITCHER' ? 'bg-indigo-900/80 text-indigo-300 border border-indigo-500/30' : 
                     player.role === 'BATTER' ? 'bg-emerald-900/80 text-emerald-300 border border-emerald-500/30' : 
                     'bg-slate-700 text-slate-300'
                   }`}>
                     {player.role === 'PITCHER' ? '投手 P' : player.role === 'BATTER' ? '野手 B' : player.role}
                   </div>
                 )}
                 <div className="flex justify-between items-start mb-3 pr-12">
                   <div>
                     <h4 className="font-bold text-white leading-tight">{player.name}</h4>
                     {player.team && <p className="text-xs text-slate-400 mt-0.5">{player.team}</p>}
                   </div>
                 </div>
                 <div className="space-y-1.5">
                   {player.stats?.map(stat => (
                     <div key={stat.subject} className="flex justify-between text-sm items-center">
                       <span className="text-slate-400">{stat.subject}</span>
                       <div className="flex items-center gap-2">
                         <div className="w-24 h-1.5 bg-slate-700 rounded-full overflow-hidden">
                           <div 
                             className={`h-full rounded-full ${stat.value >= 90 ? 'bg-yellow-400' : 'bg-blue-400'}`}
                             style={{ width: `${stat.value}%` }}
                           />
                         </div>
                         <span className={`font-mono w-6 text-right ${stat.value >= 90 ? 'text-yellow-400 font-bold' : 'text-slate-200'}`}>
                           {stat.value}
                         </span>
                       </div>
                     </div>
                   ))}
                 </div>
              </div>
            ))}
          </div>
        )}

        {/* 詳細圖表區塊 (Tab 版) */}
        {focusPlayer && (
          <div className="mt-8 border-t border-slate-700/50 pt-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-2">
                <Activity className="w-5 h-5 text-emerald-400" />
                <h3 className="text-xl font-bold text-white">
                  <span className="text-emerald-400">{focusPlayer.name}</span> 進階數據分析
                </h3>
              </div>
              
              {/* Tab 切換按鈕 */}
              <div className="flex bg-slate-800 p-1 rounded-lg border border-slate-700">
                {[
                  { id: 'heatmap', label: '熱區', icon: Grid },
                  { id: 'pie', label: '配球', icon: PieChart },
                  { id: 'trend', label: '趨勢', icon: TrendingUp },
                  { id: 'scatter', label: '落點', icon: Activity },
                ].map(tab => (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                      activeTab === tab.id 
                        ? 'bg-emerald-600 text-white shadow-sm' 
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-700'
                    }`}
                  >
                    <tab.icon className="w-4 h-4" />
                    {tab.label}
                  </button>
                ))}
              </div>
            </div>
            
            <div className="bg-slate-800/50 p-6 rounded-xl border border-slate-700/50 backdrop-blur-sm min-h-[400px]">
              {activeTab === 'heatmap' && (
                <div className="h-[400px]">
                  <PlayerHeatmap data={chartData.heatmap || MOCK_HEATMAP} loading={chartData.loading} />
                </div>
              )}
              {activeTab === 'pie' && (
                <div className="h-[400px]">
                  <PlayerPieChart data={chartData.pie || MOCK_PIE} loading={chartData.loading} />
                </div>
              )}
              {activeTab === 'trend' && (
                <div className="h-[400px]">
                  <PlayerTrendChart data={chartData.trend || MOCK_TREND} title="近況趨勢" loading={chartData.loading} />
                </div>
              )}
              {activeTab === 'scatter' && (
                <div className="h-[430px] flex flex-col">
                  {/* Scatter Type Selector */}
                  <div className="flex justify-end gap-2 mb-2">
                     {[
                       { type: 'spray-chart', label: '擊球落點 (Spray)' },
                       { type: 'exit-velocity', label: '初速仰角 (Exit Velo)' },
                       { type: 'pitch-movement', label: '位移 (Movement)' }
                     ].map(opt => (
                       <button 
                         key={opt.type}
                         onClick={() => setScatterType(opt.type)}
                         className={`text-xs px-3 py-1 rounded-full border transition-all ${
                           scatterType === opt.type 
                             ? 'bg-fuchsia-600 border-fuchsia-400 text-white' 
                             : 'bg-slate-800 border-slate-600 text-slate-400 hover:bg-slate-700'
                         }`}
                       >
                         {opt.label}
                       </button>
                     ))}
                  </div>
                  <div className="flex-1">
                    <PlayerScatterChart data={chartData.scatter || MOCK_SCATTER} loading={chartData.loading} />
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PlayerDashboard;
