import React, { useMemo } from 'react';
import { 
  ComposedChart, Line, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell, ScatterChart, Scatter, ZAxis
} from 'recharts';

/**
 * 1. 熱區圖 (Heatmap) - 顯示好球帶九宮格
 */
export const PlayerHeatmap = ({ data }) => {
  if (!data || !data.zones) return <div className="text-slate-500 text-center py-10">無熱區資料</div>;

  // 1-9 號位對應九宮格順序 (1:左上, 2:中上, 3:右上...)
  // 假設資料陣列不一定照順序，先轉 map
  const zoneMap = {};
  data.zones.forEach(z => { zoneMap[z.zoneId] = z; });

  const renderZone = (id) => {
    const zone = zoneMap[id];
    const bgColor = zone ? zone.color : '#334155'; // 預設深灰
    const value = zone ? zone.value : '-';
    
    return (
      <div 
        className="flex items-center justify-center border border-slate-600/50 text-xs font-mono font-bold text-white/90 shadow-inner transition-transform hover:scale-105"
        style={{ backgroundColor: bgColor }}
      >
        {value}
      </div>
    );
  };

  return (
    <div className="bg-slate-800 p-5 rounded-xl border border-slate-700 h-full flex flex-col">
      <h3 className="text-white font-bold mb-4 flex items-center justify-between">
        {data.title || "好球帶熱區"}
        <span className="text-xs font-normal text-slate-400 bg-slate-700 px-2 py-0.5 rounded">Strike Zone</span>
      </h3>
      <div className="flex-1 flex items-center justify-center">
        <div className="grid grid-cols-3 grid-rows-3 w-48 h-64 border-4 border-slate-500 bg-slate-900 gap-0.5 relative">
           {/* 1-9 區域 */}
           {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(id => (
             <React.Fragment key={id}>{renderZone(id.toString())}</React.Fragment>
           ))}
           
           {/* 本壘板示意圖 */}
           <div className="absolute -bottom-6 left-1/2 -translate-x-1/2 w-48 h-4">
              <div className="w-0 h-0 border-l-[96px] border-l-transparent border-r-[96px] border-r-transparent border-t-[16px] border-t-slate-500 opacity-30 mx-auto"></div>
           </div>
        </div>
      </div>
    </div>
  );
};

/**
 * 2. 趨勢圖 (Trend / Line Chart) - 顯示近期 OPS/ERA
 */
export const PlayerTrendChart = ({ data }) => {
  if (!data || !data.series) return <div className="text-slate-500 text-center py-10">無趨勢資料</div>;

  // 轉換格式適配 Recharts
  // 原始數據: labels: ['G1', 'G2'], series: [{name: 'OPS', data: [0.8, 0.9]}]
  // 目標格式: [{label: 'G1', OPS: 0.8}, {label: 'G2', OPS: 0.9}]
  const chartData = useMemo(() => {
    if (!data.labels || data.labels.length === 0) return [];
    return data.labels.map((label, index) => {
      const point = { name: label };
      data.series.forEach(s => {
        point[s.name] = s.data[index] !== undefined ? s.data[index] : null;
      });
      return point;
    });
  }, [data]);

  return (
    <div className="bg-slate-800 p-5 rounded-xl border border-slate-700 h-full flex flex-col">
      <h3 className="text-white font-bold mb-4">{data.title || "近期趨勢分析"}</h3>
      <div className="flex-1 min-h-[200px]">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
            <XAxis dataKey="name" tick={{fill: '#94a3b8', fontSize: 10}} tickLine={false} axisLine={{stroke: '#475569'}} />
            <YAxis tick={{fill: '#94a3b8', fontSize: 10}} tickLine={false} axisLine={false} domain={['auto', 'auto']} />
            <Tooltip 
              contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#f1f5f9' }}
              itemStyle={{ color: '#f1f5f9' }}
            />
            <Legend />
            {data.series.map((s, i) => (
              <Area 
                key={s.name}
                type="monotone" 
                dataKey={s.name} 
                stroke={i === 0 ? "#38bdf8" : "#fbbf24"} 
                fill={i === 0 ? "url(#colorPv)" : "url(#colorUv)"} 
                fillOpacity={0.1}
                strokeWidth={2}
                dot={{r: 3, fill: i === 0 ? "#38bdf8" : "#fbbf24"}} 
              />
            ))}
            <defs>
              <linearGradient id="colorPv" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#38bdf8" stopOpacity={0.3}/>
                <stop offset="95%" stopColor="#38bdf8" stopOpacity={0}/>
              </linearGradient>
            </defs>
          </ComposedChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

/**
 * 3. 散佈圖 (Scatter Plot) - 落點或初速仰角
 */
export const PlayerScatterChart = ({ data }) => {
  if (!data || !data.data) return <div className="text-slate-500 text-center py-10">無散佈圖資料</div>;

  return (
    <div className="bg-slate-800 p-5 rounded-xl border border-slate-700 h-full flex flex-col">
      <h3 className="text-white font-bold mb-4">{data.title || "進階擊球分析"}</h3>
      <div className="flex-1 min-h-[200px]">
        <ResponsiveContainer width="100%" height="100%">
          <ScatterChart margin={{ top: 20, right: 20, bottom: 20, left: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
            <XAxis 
              type="number" 
              dataKey="x" 
              name={data.xLabel} 
              tick={{fill: '#94a3b8', fontSize: 10}}
              label={{ value: data.xLabel, position: 'bottom', fill: '#64748b', fontSize: 10 }} 
            />
            <YAxis 
              type="number" 
              dataKey="y" 
              name={data.yLabel} 
              tick={{fill: '#94a3b8', fontSize: 10}}
              label={{ value: data.yLabel, angle: -90, position: 'insideLeft', fill: '#64748b', fontSize: 10 }}
            />
            <Tooltip 
               cursor={{ strokeDasharray: '3 3' }}
               content={({ active, payload }) => {
                 if (active && payload && payload.length) {
                   const d = payload[0].payload;
                   return (
                     <div className="bg-slate-900 border border-slate-700 p-2 text-xs rounded shadow-lg text-slate-200">
                       <p className="font-bold text-white mb-1">{d.type}</p>
                       <p>{d.info}</p>
                     </div>
                   );
                 }
                 return null;
               }}
            />
            <Scatter name="Data" data={data.data} fill="#f472b6">
              {data.data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.type === 'Home Run' ? '#ef4444' : entry.type === 'Hit' ? '#22c55e' : '#38bdf8'} />
              ))}
            </Scatter>
          </ScatterChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

/**
 * 4. 圓餅圖 (Pie Chart) - 配球比例
 */
const PIE_COLORS = ['#38bdf8', '#fbbf24', '#f472b6', '#34d399', '#a78bfa'];

export const PlayerPieChart = ({ data }) => {
  if (!data) return <div className="text-slate-500 text-center py-10">無配球資料</div>;
  
  // 假設傳入的是 array based data: [{label: 'Fastball', value: 45}, ...]
  // 如果傳入的是單純物件 {title: '...', data: [...] } 則解構
  const pieData = Array.isArray(data) ? data : (data.data || []);
  const title = !Array.isArray(data) ? data.title : "配球比例";

  return (
    <div className="bg-slate-800 p-5 rounded-xl border border-slate-700 h-full flex flex-col">
      <h3 className="text-white font-bold mb-4">{title}</h3>
      <div className="flex-1 min-h-[200px]">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={pieData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={80}
              paddingAngle={5}
              dataKey="value"
            >
              {pieData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} stroke="rgba(0,0,0,0)" />
              ))}
            </Pie>
            <Tooltip 
              contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#f1f5f9' }}
              itemStyle={{ color: '#f1f5f9' }}
            />
            <Legend 
              layout="vertical" 
              verticalAlign="middle" 
              align="right"
              formatter={(value, entry) => <span className="text-slate-300 text-xs ml-1">{value} ({entry.payload.value}%)</span>}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
