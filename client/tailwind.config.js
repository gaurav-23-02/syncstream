/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        spotify: {
          DEFAULT: '#1DB954',
          dark: '#1aa34a',
          light: '#1ed760',
          base: '#121212',
          card: '#181818',
        },
        youtube: {
          DEFAULT: '#FF0000',
          dark: '#CC0000',
          light: '#FF3333',
        },
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow': 'spin 8s linear infinite',
      },
    },
  },
  plugins: [],
}
