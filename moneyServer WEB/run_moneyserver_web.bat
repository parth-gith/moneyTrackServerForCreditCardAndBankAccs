@echo off
echo Starting backend server...
start cmd /k "cd /d C:\Users\parth\OneDrive\Desktop\Workspace\moneyServer WEB\moneyserverweb-backend && uvicorn moneyserverwebAPI.main:app --port 8000 --reload"

timeout /t 2 > nul

echo Starting frontend app...
start cmd /k "cd /d C:\Users\parth\OneDrive\Desktop\Workspace\moneyServer WEB\moneyserverweb && npm run dev"

echo Both servers started successfully!
