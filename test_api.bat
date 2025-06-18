@echo off
echo ========================================
echo LanXin模型API测试脚本
echo ========================================

echo.
echo 1. 测试健康检查接口
curl -X GET http://localhost:8080/api/lanxin/health
echo.

echo.
echo 2. 测试模型信息接口
curl -X GET http://localhost:8080/api/lanxin/info
echo.

echo.
echo 3. 测试简单聊天接口
curl -X POST http://localhost:8080/api/lanxin/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"message\": \"你好，请介绍一下自己\", \"systemPrompt\": \"你是一个友好的AI助手\"}"
echo.

echo.
echo 4. 测试流式聊天接口
curl -X POST http://localhost:8080/api/lanxin/stream-chat ^
  -H "Content-Type: application/json" ^
  -d "{\"message\": \"请用一句话介绍人工智能\"}"
echo.

echo.
echo 5. 测试多轮对话接口
curl -X POST http://localhost:8080/api/lanxin/multi-turn ^
  -H "Content-Type: application/json" ^
  -d "{\"conversation\": [\"你好\", \"你好！我是AI助手\", \"请介绍你的能力\"], \"systemPrompt\": \"你是一个专业的AI助手\"}"
echo.

echo.
echo ========================================
echo 测试完成
echo ========================================
pause
