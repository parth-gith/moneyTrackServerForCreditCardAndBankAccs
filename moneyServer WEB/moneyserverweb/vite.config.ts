import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api-rupay': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-rupay/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/rupayhdfc'),
      },

      '/api-regaliagoldhdfc': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-regaliagoldhdfc/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/regaliagoldhdfc'),
      },

      '/api-swiggyhdfc': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-swiggyhdfc/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/swiggyhdfc'),
      },

      '/api-yesreserv': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-yesreserv/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/yesreserv'),
      },

      '/api-hdfcsavings': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-hdfcsavings/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/hdfcsavings'),
      },

      '/update-swiggy-cookie': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/update-swiggy-cookie/, '/parth-moneyserver-services/moneyServer-web/updateSwiggyCookie'),
      },

      '/api-push-moneyserverbank': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: path =>
          path.replace(
            /^\/api-push-moneyserverbank/,
            '/parth-moneyserver-services/moneyServer/BankTxnDetails/txn'
          )
      },

      '/api-push-moneyserver': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: path =>
          path.replace(
            /^\/api-push-moneyserver/,
            '/parth-moneyserver-services/moneyServer/CreditCardTxnDetails/txn'
          )
      },

      '/saveStateBase': {
        target: 'http://ip:port',
        changeOrigin: true,
        secure: false,
        rewrite: path =>
          path.replace(
            /^\/saveStateBase/,
            '/parth-moneyserver-services/moneyServer/saveState'
          )
      }

    },
  },
});
