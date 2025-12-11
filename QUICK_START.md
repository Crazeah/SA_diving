# 🚀 快速開始指南

## 1️⃣ 啟動系統（3 步驟）

### Step 1: 建構專案
```powershell
cd d:\repos\SA_diving
mvn clean install
```

### Step 2: 啟動應用程式
```powershell
mvn spring-boot:run
```

### Step 3: 訪問系統
開啟瀏覽器：http://localhost:8080

---

## 2️⃣ 測試帳號

登入使用以下帳號：

| 角色 | Email | Password | 功能 |
|------|-------|----------|------|
| 🔑 管理員 | admin@diveclub.com | admin123 | 審核活動 |
| 👔 幹部1 | manager1@diveclub.com | manager123 | 建立活動 |
| 👔 幹部2 | manager2@diveclub.com | manager123 | 建立活動 |
| 👤 會員 | member@diveclub.com | member123 | 瀏覽活動 |

---

## 3️⃣ 快速測試 API

### 使用 PowerShell 測試

#### 1. 取得所有活動（無需認證）
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/activities" -Method Get | ConvertTo-Json -Depth 10
```

#### 2. 建立活動（以 manager1 身分）
```powershell
$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    title = "測試潛水活動"
    description = "這是一個測試活動"
    category = "潛水訓練"
    startTime = "2025-12-25T09:00:00"
    endTime = "2025-12-25T17:00:00"
    location = "墾丁"
    maxParticipants = 20
    cost = 3500.00
    qualifications = "無限制"
} | ConvertTo-Json

$credential = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("manager1@diveclub.com:manager123"))
$headers["Authorization"] = "Basic $credential"

Invoke-RestMethod -Uri "http://localhost:8080/api/activities" -Method Post -Headers $headers -Body $body | ConvertTo-Json
```

#### 3. 查看待審核活動（以 admin 身分）
```powershell
$credential = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("admin@diveclub.com:admin123"))
$headers = @{
    "Authorization" = "Basic $credential"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/activities/pending" -Method Get -Headers $headers | ConvertTo-Json -Depth 10
```

#### 4. 審核活動（核准）
```powershell
$credential = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("admin@diveclub.com:admin123"))
$headers = @{
    "Authorization" = "Basic $credential"
    "Content-Type" = "application/json"
}

$body = @{
    action = "APPROVE"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/activities/3/audit" -Method Post -Headers $headers -Body $body | ConvertTo-Json
```

#### 5. 審核活動（退回）
```powershell
$credential = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("admin@diveclub.com:admin123"))
$headers = @{
    "Authorization" = "Basic $credential"
    "Content-Type" = "application/json"
}

$body = @{
    action = "REJECT"
    reason = "活動描述不夠詳細，請補充更多資訊"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/activities/3/audit" -Method Post -Headers $headers -Body $body | ConvertTo-Json
```

---

## 4️⃣ 訪問 H2 資料庫控制台

1. 開啟瀏覽器：http://localhost:8080/h2-console
2. 輸入以下資訊：
   - **JDBC URL**: `jdbc:h2:mem:diveclub`
   - **User Name**: `sa`
   - **Password**: (留空)
3. 點擊「Connect」

### 實用 SQL 查詢

```sql
-- 查看所有活動
SELECT * FROM ACTIVITIES ORDER BY CREATED_AT DESC;

-- 查看所有使用者
SELECT * FROM USERS;

-- 查看待審核的活動
SELECT ACTIVITY_ID, TITLE, STATUS, CREATOR_ID 
FROM ACTIVITIES 
WHERE STATUS = 'PENDING_REVIEW';

-- 查看已發布的活動
SELECT ACTIVITY_ID, TITLE, START_TIME, END_TIME, MAX_PARTICIPANTS, COST
FROM ACTIVITIES 
WHERE STATUS = 'PUBLISHED'
ORDER BY START_TIME;

-- 查看每個狀態的活動數量
SELECT STATUS, COUNT(*) AS COUNT
FROM ACTIVITIES
GROUP BY STATUS;
```

---

## 5️⃣ 完整功能流程測試

### 🎯 情境：建立並審核一個活動

#### Step 1: Manager 建立活動
```powershell
# 使用上面的「建立活動」指令
# 活動會處於 DRAFTING 狀態
```

#### Step 2: Manager 提交審核
```powershell
# 假設新建立的活動 ID 是 6
$credential = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("manager1@diveclub.com:manager123"))
$headers = @{
    "Authorization" = "Basic $credential"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/activities/6/submit" -Method Post -Headers $headers | ConvertTo-Json
```

#### Step 3: Admin 查看待審核列表
```powershell
# 使用上面的「查看待審核活動」指令
```

#### Step 4: Admin 核准活動
```powershell
# 使用上面的「審核活動（核准）」指令
```

#### Step 5: 所有人都可以看到已發布的活動
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/activities" -Method Get | ConvertTo-Json -Depth 10
```

---

## 6️⃣ 系統狀態驗證

### 檢查活動狀態流轉

```powershell
# 查看特定活動的狀態
Invoke-RestMethod -Uri "http://localhost:8080/api/activities/1" -Method Get | ConvertTo-Json
```

### 測試搜尋功能

```powershell
# 搜尋包含「墾丁」的活動
Invoke-RestMethod -Uri "http://localhost:8080/api/activities/search?keyword=墾丁" -Method Get | ConvertTo-Json -Depth 10
```

### 測試分類篩選

```powershell
# 取得「潛水訓練」類別的活動
Invoke-RestMethod -Uri "http://localhost:8080/api/activities/category/潛水訓練" -Method Get | ConvertTo-Json -Depth 10
```

---

## 7️⃣ 常見問題

### Q: 如何停止系統？
**A:** 在執行 `mvn spring-boot:run` 的視窗按 `Ctrl + C`

### Q: 如何清除資料庫重新開始？
**A:** 系統使用 H2 記憶體資料庫，重啟後會自動清除並重新初始化

### Q: 如何切換到 MySQL？
**A:** 修改 `application.properties`：
```properties
# 註解掉 H2 設定
#spring.datasource.url=jdbc:h2:mem:diveclub

# 取消註解 MySQL 設定
spring.datasource.url=jdbc:mysql://localhost:3306/diveclub
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### Q: 郵件功能報錯怎麼辦？
**A:** 郵件功能預設使用 Gmail SMTP，需要：
1. 設定正確的 Gmail 帳號和 App Password
2. 或暫時忽略郵件錯誤（系統會繼續運作）

---

## 8️⃣ 開發工具推薦

### Postman Collection
匯入以下 API 到 Postman 進行測試：
- Base URL: `http://localhost:8080`
- 設定 Basic Auth：`manager1@diveclub.com` / `manager123`

### VS Code 擴充套件
- **Extension Pack for Java** - Java 開發支援
- **Spring Boot Extension Pack** - Spring Boot 工具
- **Thunder Client** - API 測試工具
- **Database Client** - 資料庫查詢工具

### IntelliJ IDEA
- 內建完整的 Spring Boot 支援
- HTTP Client 可直接測試 API
- Database Tools 可直接連接 H2

---

## 9️⃣ 系統健康檢查

### 檢查應用程式是否正常運行
```powershell
# 訪問根路徑
Invoke-RestMethod -Uri "http://localhost:8080/" -Method Get
```

### 檢查資料是否正確初始化
```powershell
# 應該能看到 5 個範例活動
Invoke-RestMethod -Uri "http://localhost:8080/api/activities" -Method Get | ConvertTo-Json -Depth 10
```

---

## 🎉 完成！

現在您已經成功啟動並測試了潛水社社團系統的所有核心功能！

### 下一步建議：
1. 📖 閱讀完整 API 文件：`README_API.md`
2. 🔍 查看實作總結：`IMPLEMENTATION_SUMMARY.md`
3. 💻 開始開發新功能或改進現有功能
4. 🧪 撰寫單元測試和整合測試

### 需要幫助？
- 查看日誌：檢查控制台輸出
- 查看資料庫：使用 H2 Console
- 閱讀文件：README_API.md

**祝開發順利！🏊‍♂️🤿**
