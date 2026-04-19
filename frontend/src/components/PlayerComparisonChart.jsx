import React, { useMemo } from 'react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Legend, Tooltip } from 'recharts';

const COLORS = ['#38bdf8', '#f472b6', '#34d399', '#fbbf24', '#a78bfa'];

const PlayerComparisonChart = ({ players }) => {
  // 如果沒有選手，顯示提示
  if (!players || players.length === 0) {
    return (
      <div className="bg-slate-800 p-6 rounded-xl shadow-lg border border-slate-700 w-full h-[500px] flex items-center justify-center text-slate-400">
        請從列表選擇選手以進行比較
      </div>
    );
  }

  // 轉換資料結構
  // 原始資料: [{name: 'A', stats: [{subject: 'S1', value: 10}, ...]}, {name: 'B', stats: [{subject: 'S1', value: 20}, ...]}]
  // 目標資料: [{subject: 'S1', A: 10, B: 20, fullMark: 100}, ...]
  const chartData = useMemo(() => {
    if (players.length === 0) return [];
    
    // 假設所有選手的 stats 結構一致，取第一位選手的 stats 作為基準
    const subjects = players[0].stats.map(s => s.subject);
    
    return subjects.map(subject => {
      const dataPoint = { subject, fullMark: 100 };
      players.forEach(player => {
        const stat = player.stats.find(s => s.subject === subject);
        if (stat) {
          dataPoint[player.name] = stat.value;
        }
      });
      return dataPoint;
    });
  }, [players]);

  return (
    <div className="bg-slate-800 p-6 rounded-xl shadow-lg border border-slate-700 w-full h-[600px] flex flex-col">
      <div className="mb-4 flex flex-wrap gap-2 items-center justify-between">
        <h2 className="text-2xl font-bold text-white">戰力對比分析</h2>
        <div className="flex gap-2 text-sm text-slate-400">
          {players.map((p, i) => (
             <span key={p.id} className="flex items-center gap-1">
                <span className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[i % COLORS.length] }}></span>
                {p.name}
             </span>
          ))}
        </div>
      </div>

      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <RadarChart cx="50%" cy="50%" outerRadius="70%" data={chartData}>
            <PolarGrid stroke="#334155" />
            <PolarAngleAxis dataKey="subject" tick={{ fill: '#94a3b8', fontSize: 14 }} />
            <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
            
            {players.map((player, index) => (
              <Radar
                key={player.id}
                name={player.name}
                dataKey={player.name}
                stroke={COLORS[index % COLORS.length]}
                fill={COLORS[index % COLORS.length]}
                fillOpacity={0.3}
                dot={{ r: 3, fill: COLORS[index % COLORS.length] }}
              />
            ))}
            <Legend wrapperStyle={{ paddingTop: '20px' }} />
            <Tooltip 
              contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#f1f5f9' }}
              itemStyle={{ color: '#f1f5f9' }}
            />
          </RadarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default PlayerComparisonChart;
