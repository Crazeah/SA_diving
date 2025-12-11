# 🌊 潛水社社團系統 (Diving Club Management System)

國立中央大學 - 系統分析與設計課程期末專題

---

## 📋 專案概述

這是一個基於 Spring Boot 的潛水社團管理系統，專注於「活動管理模組」的完整實作。系統採用 MVC 架構，整合了活動建立、審核、報名等核心功能。

**系統特色：**
- ✅ 完整的活動生命週期管理（草稿 → 待審核 → 已發布 → 已結束）
- ✅ 基於角色的權限控制（訪客、會員、幹部、管理員）
- ✅ 自動化狀態流轉與郵件通知
- ✅ RESTful API 設計
- ✅ Spring Security 認證授權

---

## 🏗️ 系統架構

### 技術堆疊
- **後端框架**: Spring Boot 3.2.0
- **資料庫**: H2 (開發) / MySQL (生產)
- **ORM**: Spring Data JPA
- **安全框架**: Spring Security
- **模板引擎**: Thymeleaf
- **郵件服務**: Spring Mail
- **建構工具**: Maven
- **Java 版本**: 17

### MVC 架構層級
```
Controller Layer (REST API)
    ↓
Service Layer (業務邏輯 & 狀態機)
    ↓
Repository Layer (資料持久化)
    ↓
Entity Layer (Domain Model)
```

---

## 👥 使用者角色與權限

| 角色 | Spring Security Role | 權限描述 |
|------|---------------------|---------|
| **訪客** (Guest) | ROLE_GUEST | 瀏覽已發布活動列表 |
| **會員** (Member) | ROLE_MEMBER | 瀏覽活動詳情、報名活動 |
| **幹部** (Manager) | ROLE_MANAGER | 建立、編輯、刪除活動 |
| **管理員** (SuperManager) | ROLE_ADMIN | 審核活動、最高權限 |

---

## 🔄 活動狀態機 (State Machine)

```
DRAFTING (編輯中)
    ↓ submit
PENDING_REVIEW (待審核)
    ↓ approve          ↓ reject
PUBLISHED (已發布)   NEEDS_REVISION (需修正)
    ↓ auto-check         ↓ resubmit
ENDED (已結束)       PENDING_REVIEW
```

### 狀態轉換規則
1. **DRAFTING → PENDING_REVIEW**: 幹部提交審核
2. **PENDING_REVIEW → PUBLISHED**: 管理員核准
3. **PENDING_REVIEW → NEEDS_REVISION**: 管理員退回（需提供原因）
4. **NEEDS_REVISION → PENDING_REVIEW**: 幹部修正後重新提交
5. **PUBLISHED → ENDED**: 系統自動檢查（每小時執行）

---

## 🚀 快速開始

### 前置需求
- Java 17+
- Maven 3.6+
- IDE (推薦 IntelliJ IDEA 或 VS Code)

### 啟動步驟

1. **Clone 專案**
```bash
git clone <repository-url>
cd SA_diving
```

2. **建構專案**
```bash
mvn clean install
```

3. **執行應用程式**
```bash
mvn spring-boot:run
```

4. **訪問系統**
- 應用程式: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:diveclub`
  - Username: `sa`
  - Password: (空白)

### 測試帳號

系統會自動初始化以下測試帳號：

| 角色 | Email | Password |
|------|-------|----------|
| 管理員 | admin@diveclub.com | admin123 |
| 幹部 1 | manager1@diveclub.com | manager123 |
| 幹部 2 | manager2@diveclub.com | manager123 |
| 會員 | member@diveclub.com | member123 |

---

## 📡 REST API 文件

### 活動管理 API

#### 1. 建立活動 (UC-A05)
```http
POST /api/activities
Authorization: Required (ROLE_MANAGER or ROLE_ADMIN)
Content-Type: application/json

{
  "title": "墾丁潛水體驗",
  "description": "適合初學者的潛水體驗活動",
  "category": "潛水訓練",
  "startTime": "2025-12-25T09:00:00",
  "endTime": "2025-12-25T17:00:00",
  "location": "墾丁國家公園海域",
  "maxParticipants": 20,
  "cost": 3500.00,
  "qualifications": "無需任何經驗",
  "imageUrl": "https://example.com/image.jpg"
}
```

**回應：**
```json
{
  "success": true,
  "message": "活動建立成功，狀態為草稿",
  "data": { /* ActivityResponseDTO */ }
}
```

#### 2. 提交審核
```http
POST /api/activities/{id}/submit
Authorization: Required (ROLE_MANAGER - owner only)
```

#### 3. 審核活動 (UC-A09)
```http
POST /api/activities/{id}/audit
Authorization: Required (ROLE_ADMIN)
Content-Type: application/json

// 核准
{
  "action": "APPROVE"
}

// 退回
{
  "action": "REJECT",
  "reason": "活動描述不夠詳細，請補充更多資訊"
}
```

#### 4. 更新活動
```http
PUT /api/activities/{id}
Authorization: Required (ROLE_MANAGER - owner only)
Content-Type: application/json
```

#### 5. 刪除活動
```http
DELETE /api/activities/{id}
Authorization: Required (ROLE_MANAGER - owner only)
```

#### 6. 取消活動
```http
POST /api/activities/{id}/cancel
Authorization: Required (ROLE_MANAGER - owner only)
```

#### 7. 查詢活動

**取得所有已發布活動**
```http
GET /api/activities
Authorization: Not Required (Public)
```

**取得待審核活動**
```http
GET /api/activities/pending
Authorization: Required (ROLE_ADMIN)
```

**取得我的活動**
```http
GET /api/activities/my
Authorization: Required (ROLE_MANAGER or ROLE_ADMIN)
```

**搜尋活動**
```http
GET /api/activities/search?keyword=墾丁
Authorization: Not Required (Public)
```

**依類別篩選**
```http
GET /api/activities/category/{category}
Authorization: Not Required (Public)
```

**依狀態篩選**
```http
GET /api/activities/status/{status}
Authorization: Required (ROLE_MANAGER or ROLE_ADMIN)
```

---

## 📊 資料庫設計

### 核心 Entity

#### User (使用者基類)
- 使用 JOINED 繼承策略
- Manager (幹部) 和 SuperManager (管理員) 繼承 User

#### Activity (活動)
```java
- activityId (Long, PK)
- title (String, 活動標題)
- description (Text, 活動描述)
- category (String, 活動類別)
- startTime (LocalDateTime, 開始時間)
- endTime (LocalDateTime, 結束時間)
- location (String, 地點)
- maxParticipants (Integer, 人數上限)
- cost (BigDecimal, 費用)
- qualifications (Text, 參加資格)
- imageUrl (String, 圖片 URL)
- status (ActivityStatus, 狀態)
- rejectionReason (Text, 退回原因)
- creator (Manager, 建立者)
- createdAt (LocalDateTime, 建立時間)
- updatedAt (LocalDateTime, 更新時間)
```

---

## ⚙️ 系統配置

### application.properties 設定

**切換至 MySQL**
```properties
# 取消註解以下配置
spring.datasource.url=jdbc:mysql://localhost:3306/diveclub?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

**郵件設定**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

---

## 🎯 核心功能實作

### 1. 活動建立與驗證
- 使用 `@Valid` 註解進行資料驗證
- 自訂驗證邏輯：結束時間必須晚於開始時間
- 初始狀態自動設為 DRAFTING

### 2. 審核流程
- 核准：檢查活動是否過期 → 更新為 PUBLISHED → 發送通知郵件
- 退回：必須提供原因 → 更新為 NEEDS_REVISION → 發送通知郵件

### 3. 自動化排程
- 使用 `@Scheduled` 每小時檢查已發布活動
- 自動將過期活動標記為 ENDED

### 4. 郵件通知
- 提交審核通知
- 核准通知
- 退回通知（含原因）
- 管理員新活動提醒

---

## 🔒 安全性設計

### Spring Security 配置
- 基於角色的方法級安全 (`@PreAuthorize`)
- BCrypt 密碼加密
- 自訂 UserDetailsService
- CSRF 保護（REST API 可選擇性關閉）

### API 權限矩陣

| Endpoint | Guest | Member | Manager | Admin |
|----------|-------|--------|---------|-------|
| GET /api/activities | ✅ | ✅ | ✅ | ✅ |
| GET /api/activities/{id} | ❌ | ✅ | ✅ | ✅ |
| POST /api/activities | ❌ | ❌ | ✅ | ✅ |
| PUT /api/activities/{id} | ❌ | ❌ | ✅ (owner) | ✅ |
| DELETE /api/activities/{id} | ❌ | ❌ | ✅ (owner) | ✅ |
| POST /api/activities/{id}/audit | ❌ | ❌ | ❌ | ✅ |
| GET /api/activities/pending | ❌ | ❌ | ❌ | ✅ |

---

## 📝 開發指南

### 新增活動類別
1. 在資料庫或配置檔定義新類別
2. 前端下拉選單同步更新
3. 確保分類查詢 API 支援新類別

### 擴展狀態機
1. 在 `ActivityStatus` enum 新增狀態
2. 在 `Activity` entity 新增狀態轉換方法
3. 更新 `ActivityService` 相關邏輯
4. 新增對應 API 端點

### 自訂驗證規則
在 DTO 類別使用 `@AssertTrue` 實作自訂驗證：
```java
@AssertTrue(message = "自訂錯誤訊息")
public boolean isValid() {
    // 驗證邏輯
}
```

---

## 🐛 除錯與測試

### 查看日誌
```bash
# 設定日誌級別
logging.level.com.dive.club=DEBUG
```

### H2 Console
- URL: http://localhost:8080/h2-console
- 可直接查詢資料庫內容

### API 測試工具
推薦使用：
- Postman
- cURL
- Thunder Client (VS Code 擴充套件)

---

## 📦 專案結構

```
src/main/java/com/dive/club/
├── config/             # 配置類別
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── controller/         # REST Controller
│   ├── ActivityController.java
│   └── ViewController.java
├── dto/               # 資料傳輸物件
│   ├── ActivityCreateDTO.java
│   ├── ActivityUpdateDTO.java
│   ├── AuditDecisionDTO.java
│   └── ActivityResponseDTO.java
├── entity/            # JPA Entity
│   ├── User.java
│   ├── Manager.java
│   ├── SuperManager.java
│   └── Activity.java
├── enums/             # 列舉類型
│   ├── ActivityStatus.java
│   └── UserRole.java
├── exception/         # 例外處理
│   ├── ActivityNotFoundException.java
│   ├── UnauthorizedException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── repository/        # JPA Repository
│   ├── UserRepository.java
│   └── ActivityRepository.java
├── scheduler/         # 排程任務
│   └── ActivityStatusScheduler.java
└── service/           # 業務邏輯
    ├── ActivityService.java
    ├── EmailService.java
    └── CustomUserDetailsService.java
```

---

## 🚧 未來擴展

- [ ] 報名功能實作
- [ ] 繳費金流整合
- [ ] 活動簽到系統
- [ ] 會員等級制度
- [ ] 活動評價系統
- [ ] 照片上傳與管理
- [ ] 整合中大 Portal OAuth
- [ ] 手機 APP 開發

---

## 📄 授權

本專案為系統分析課程期末專案。

---

## 👨‍💻 開發團隊

**系統分析與設計課程**  
國立中央大學

---

## 📞 聯絡方式

如有問題或建議，請透過以下方式聯絡：
- Email: admin@diveclub.com
- GitHub Issues: [提交問題](https://github.com/your-repo/issues)

---

**Happy Coding! 🏊‍♂️🤿**
