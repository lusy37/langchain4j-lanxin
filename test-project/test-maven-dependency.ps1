# LanXin Maven依赖测试脚本
Write-Host "=== LanXin Maven依赖集成测试 ===" -ForegroundColor Green

# 检查当前目录
Write-Host "当前目录: $(Get-Location)" -ForegroundColor Yellow

# 清理之前的构建
Write-Host "清理之前的构建..." -ForegroundColor Yellow
if (Test-Path "target") {
    Remove-Item -Recurse -Force "target"
}

# 编译项目
Write-Host "编译项目..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败!" -ForegroundColor Red
    exit 1
}

# 运行测试
Write-Host "运行集成测试..." -ForegroundColor Yellow
mvn test

if ($LASTEXITCODE -ne 0) {
    Write-Host "测试失败!" -ForegroundColor Red
    exit 1
}

# 启动应用程序（后台运行）
Write-Host "启动应用程序..." -ForegroundColor Yellow
$app = Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -PassThru -WindowStyle Hidden

# 等待应用启动
Write-Host "等待应用启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 测试API端点
Write-Host "测试API端点..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/chat/test" -Method GET
    Write-Host "API测试成功: $response" -ForegroundColor Green
} catch {
    Write-Host "API测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 停止应用程序
Write-Host "停止应用程序..." -ForegroundColor Yellow
Stop-Process -Id $app.Id -Force

Write-Host "=== 测试完成 ===" -ForegroundColor Green
