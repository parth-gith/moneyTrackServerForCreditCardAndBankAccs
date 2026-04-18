source venv/bin/activate 
cd /home/parth/serverUtilsForMoneyServer/moneyServerWEB/moneyserverweb-backend 
/home/parth/serverUtilsForMoneyServer/venv/bin/uvicorn moneyserverwebAPI.main:app --host 0.0.0.0 --port 8000
