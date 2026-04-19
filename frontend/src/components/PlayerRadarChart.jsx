import React from 'react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer } from 'recharts';

const PlayerRadarChart = ({ data }) => {
  // 檢查資料結構是否符合預期
  if (!data || !data.stats) return <p className="text-white">請選擇選手...</p>;

  return (
    <div className="bg-slate-800 p-6 rounded-xl shadow-lg border border-slate-700 w-full h-[500px] flex flex-col">
      <div className="mb-4">
        <h2 className="text-2xl font-bold text-white">{data.name}</h2>
        <p className="text-blue-400">{data.team} - 2026 WBC 指標</p>
      </div>

      {/* 讓圖表佔據剩下的空間 */}
      <div className="flex-1 min-h-0"> 
        <ResponsiveContainer width="100%" height="100%">
          <RadarChart cx="50%" cy="50%" outerRadius="70%" data={data.stats}>
            {/* 調整格線顏色讓它更有透明感 */}
            <PolarGrid stroke="#334155" />
            
            {/* dataKey 對應 JSON 中的 "subject": "打擊" */}
            <PolarAngleAxis 
              dataKey="subject" 
              tick={{ fill: '#94a3b8', fontSize: 14 }} 
            />
            
            <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
            
            {/* dataKey 對應 JSON 中的 "value": 70 */}
            <Radar
              name={data.name}
              dataKey="value"
              stroke="#38bdf8"  /* 使用 WBC 亮藍色 */
              fill="#38bdf8"    /* 使用 WBC 亮藍色 */
              fillOpacity={0.6}
              dot={{ r: 4, fill: '#38bdf8' }} /* 增加亮點 */
            />
          </RadarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default PlayerRadarChart;