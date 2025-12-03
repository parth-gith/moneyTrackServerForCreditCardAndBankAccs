@echo off
echo Starting backend server...
start cmd /k "cd /d basepath\moneyServer WEB\moneyserverweb-backend && uvicorn moneyserverwebAPI.main:app --reload"

timeout /t 2 > nul

echo Starting frontend app...
start cmd /k "cd /d basepath\moneyServer WEB\moneyserverweb && npm run dev"

echo Both servers started successfully!
