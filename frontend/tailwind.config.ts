import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./features/**/*.{js,ts,jsx,tsx,mdx}"
  ],
  theme: {
    extend: {
      colors: {
        ink: {
          950: "#102a2c",
          800: "#1d3b3d",
          600: "#486365"
        },
        brand: {
          50: "#effcf8",
          100: "#d7f7ed",
          200: "#b2eddd",
          300: "#7bdeca",
          400: "#43c6b0",
          500: "#1da994",
          600: "#128878",
          700: "#126d62",
          800: "#13584f",
          900: "#124941",
          950: "#082c28"
        },
        coral: {
          50: "#fff5f2",
          100: "#ffe8e1",
          200: "#ffd1c6",
          500: "#f4795f",
          600: "#df5c42"
        }
      },
      boxShadow: {
        soft: "0 18px 50px rgba(16, 42, 44, 0.08)",
        card: "0 8px 30px rgba(16, 42, 44, 0.06)"
      },
      borderRadius: {
        "4xl": "2rem"
      },
      keyframes: {
        "fade-up": {
          "0%": { opacity: "0", transform: "translateY(12px)" },
          "100%": { opacity: "1", transform: "translateY(0)" }
        },
        "pulse-soft": {
          "0%, 100%": { opacity: "0.45" },
          "50%": { opacity: "0.85" }
        }
      },
      animation: {
        "fade-up": "fade-up 500ms ease-out both",
        "pulse-soft": "pulse-soft 1.8s ease-in-out infinite"
      }
    }
  },
  plugins: []
};

export default config;
