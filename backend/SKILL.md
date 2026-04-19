# 棒球能力指標定義 (Baseball Skill Definitions)

本文件詳細說明 `SportInfo` 系統中，用於球員雷達圖 (Radar Chart) 的各項能力指標定義與計算邏輯。這些指標旨在量化球員在不同維度的表現，並以 0-100 的評分呈現。

## 1. 野手/打者能力 (Batter Skills - 5 Tools)
針對野手與指定打擊，採用傳統的「五拍子 (Five-Tool Player)」概念並結合進階數據進行評分。

| 指標 (Subject) | 定義 (Definition) | 計算參考數據 (Reference Stats) |
| :--- | :--- | :--- |
| **打擊 (Contact)** | 擊中球的能力，以及將球打進場內的機率。 | 打擊率 (AVG), 擊球率 (Contact%), 三振率 (K%) |
| **力量 (Power)** | 擊球的飛行距離與產生長打的能力。 | 長打率 (SLG), 孤立長打率 (ISO), 擊球初速 (Exit Velocity) |
| **速度 (Speed)** | 跑壘速度與破壞力。 | 衝刺速度 (Sprint Speed), 盜壘成功率 (SB%), 三壘安打數 |
| **防守 (Defense)** | 守備範圍與接球穩定性。 | OAA (Outs Above Average), UZR, 守備率 (FPCT) |
| **傳球 (Arm)** | 傳球臂力與準確度。 | 傳球速度 (Arm Strength), 助殺數 (Assists) |
| **體力 (Stamina)** | 長期出賽的耐受度 (此項為額外指標)。 | 出賽場次 (G), 打席數 (PA) |

---

## 2. 投手能力 (Pitcher Skills - 6 Tools)
針對投手設計的專屬六軸指標，反映現代棒球對投手的評價標準。

| 指標 (Subject) | 定義 (Definition) | 計算參考數據 (Reference Stats) |
| :--- | :--- | :--- |
| **球威 (Stuff)** | 直球的速度、轉速與尾勁。 | 快速球均速 (Fastball Velocity), 轉速 (Spin Rate) |
| **控球 (Control)** | 將球投進好球帶邊角的能力，減少保送。 | 每九局保送 (BB/9), 好球率 (Strike%) |
| **變化球 (Breaking)** | 變化球種 (滑球、曲球等) 的位移與犀利度。 | 變化球垂直/水平位移, 揮空率 (Whiff%) |
| **奪三振 (K-Ability)** | 讓打者揮空或站著不動遭到三振的能力。 | 每九局三振 (K/9), 三振率 (K%) |
| **體力 (Stamina)** | 負擔局數與投球數的續航力。 | 投球局數 (IP), 投球數 (Pitches/G) |
| **抗壓 (Clutch)** | 在壘上有人或關鍵時刻的心理素質。 | 得點圈被打擊率 (BA/RISP), 殘壘率 (LOB%) |

---

## 3. 圖表類型與數據來源 (Charts & Data Sources)

### 3.1 雷達圖 (Radar Chart)
- **用途**: 快速綜覽球員優缺點，比較不同球員的能力分佈。
- **API 端點**: 
  - GET `/api/charts/radar/batter/{playerId}`
  - GET `/api/charts/radar/pitcher/{playerId}`

### 3.2 擊球落點散佈圖 (Spray Chart)
- **用途**: 分析打者擊球傾向 (拉打、推打、廣角) 與落點分佈。
- **API 端點**: 
  - GET `/api/stats/scatter?type=spray-chart`

### 3.3 擊球仰角與初速 (Exit Velocity vs Launch Angle)
- **用途**: 尋找「Barrel」甜蜜點分佈，分析長打潛力。
- **API 端點**: 
  - GET `/api/stats/scatter?type=exit-velocity`

### 3.4 投手進壘點熱區圖 (Pitch Heatmap)
- **用途**: 顯示投手各球種的進壘位置與控球穩定度。
- **API 端點**: 
  - GET `/api/stats/heatmap?playerId={id}`

