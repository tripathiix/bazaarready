import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In development, API calls and shop pages are forwarded to the Spring Boot app, which Docker publishes on port 8090.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8090",
      "/s": "http://localhost:8090",
      "/actuator": "http://localhost:8090",
    },
  },
});
