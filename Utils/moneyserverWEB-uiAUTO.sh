#!/bin/bash
cd /home/parth/serverUtilsForMoneyServer/moneyServerWEB/moneyserverweb
export PATH=/home/parth/.nvm/versions/node/v24.15.0/bin:$PATH
node -v
exec npm run dev -- --host
