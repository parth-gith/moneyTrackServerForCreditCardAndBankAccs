import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api-rupay': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-rupay/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/rupayhdfc'),
      },

      '/api-regaliagoldhdfc': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-regaliagoldhdfc/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/regaliagoldhdfc'),
      },

      '/api-swiggyhdfc': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => 
        path.replace(/^\/api-swiggyhdfc/, '/parth-moneyserver-services/moneyServer-web/getAllTxns/swiggyhdfc'),
      },

      '/api-push-moneyserver': {
        target: 'http://192.168.29.179:8080',
        changeOrigin: true,
        secure: false,
        rewrite: path =>
          path.replace(
            /^\/api-push-moneyserver/,
            '/parth-moneyserver-services/moneyServer/CreditCardTxnDetails/txn'
          )
      }

    },
  },
});
