import { defineConfig } from 'vite';
import preact from '@preact/preset-vite';

const servidor = 'http://localhost:8080';

// DEC-09: em desenvolvimento o navegador fala só com o Vite (5173), que encaminha
// a API e a documentação OpenAPI para o server (8080).
export default defineConfig({
  plugins: [preact()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': servidor,
      '/v3': servidor,
    },
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});
