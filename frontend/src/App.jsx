import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import PlayerDashboard from './components/PlayerDashboard';
import { Trophy } from 'lucide-react';

function App() {
  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 p-8">
      <header className="flex items-center gap-3 mb-10 border-b border-slate-700 pb-6">
        <Trophy className="text-yellow-500 w-10 h-10" />
        <div>
          <h1 className="text-3xl font-black tracking-tighter text-white">WBC 2026 戰力分析中心</h1>
          <p className="text-slate-400 text-sm">多名選手戰力對比與數據儀表板</p>
        </div>
      </header>

      <main className="mx-auto w-full">
        <PlayerDashboard />
      </main>
    </div>
  );
}

export default App
