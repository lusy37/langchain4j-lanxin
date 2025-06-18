# LanXin API 测试脚本

Write-Host "========================================" -ForegroundColor Green
Write-Host "LanXin模型API测试脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 1. 测试健康检查
Write-Host "`n1. 测试健康检查接口" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/lanxin/health" -Method GET
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应内容: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. 测试模型信息
Write-Host "`n2. 测试模型信息接口" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/lanxin/info" -Method GET
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应内容: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 测试聊天接口
Write-Host "`n3. 测试聊天接口" -ForegroundColor Yellow
try {
    $body = @{
        message = "你好，请介绍一下自己"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/lanxin/chat" -Method POST -ContentType "application/json" -Body $body
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应内容: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "测试完成" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
