# 🌊 潛水社團管理系統

國立中央大學 資訊管理學系  
系統分析與設計 - 期末專題  
指導老師：陳仲儼

## 📋 專題簡介

本系統旨在數位化管理潛水社團的各項營運功能，包含活動管理、報名系統、公告管理等模組。
本專題重點實作**活動管理模組**，展示完整的 UML 分析與程式碼對應。

## 🎯 核心功能

### UC-A05 建立活動
- 社團幹部填寫活動資訊（標題、時間、地點、費用等）
- 支援儲存草稿與送出審核
- 前端驗證：日期邏輯、必填欄位檢查
- 圖片上傳與預覽功能

### UC-A09 審核活動
- 超級管理員檢視待審核列表
- 審閱活動詳情並執行核准/退回
- 退回時須輸入原因（預設選項 + 自訂輸入）
- 自動發送 Email 通知提案人

## 🛠️ 技術架構

- **後端框架**: Spring Boot 3.2.0
- **模板引擎**: Thymeleaf
- **前端框架**: Bootstrap 5
- **動畫效果**: Animate.css, SweetAlert2
- **開發工具**: IntelliJ IDEA / Eclipse

## 📦 安裝與執行

### 環境需求
- Java 17 或以上
- Maven 3.8+
- 瀏覽器（Chrome / Firefox / Edge）

### 執行步驟
\`\`\`bash
# 1. Clone 專案
git clone https://github.com/your-username/dive-club-system.git
cd dive-club-system

# 2. 編譯專案
mvn clean install

# 3. 執行應用程式
mvn spring-boot:run

# 4. 開啟瀏覽器
http://localhost:8080
\`\`\`

## 📐 UML 圖表對應

| UML 圖表 | 對應實作 |
|---------|---------|
| Use Case Diagram | 7 個 HTML 頁面對應 UC-A01/A05/A09 |
| Activity Diagram | JavaScript 驗證函數對應決策點 |
| Sequence Diagram | Controller → Service → Repository |
| State-chart Diagram | Activity.status 狀態管理 |
| Class Diagram | Entity 類別設計 |

## 👥 開發團隊

- 組長：XXX (112403XXX)
- 組員：XXX (112403XXX)
- 組員：XXX (112403XXX)
- 組員：XXX (112403XXX)
- 組員：XXX (112403XXX)

## 📅 開發時程

- 2025/11 - 需求分析與 UML 設計
- 2025/12 - 前後端開發
- 2025/12/19 - 期末展示

## 📝 授權

本專案僅供學術用途，版權所有 © 前社長真的好漂亮
\`\`\`
