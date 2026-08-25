# 🎵 SyncStream: Spotify-to-YouTube Playlist Transfer Engine

SyncStream is a high-performance full-stack web application built on **Java 21 LTS** and **React 18** to transfer Spotify playlists to YouTube and YouTube Music.

---

## 🚀 Quick Deployment Guide

You can deploy SyncStream using any of the following methods:

---

### Option 1: One-Click Cloud Deployment (Render / Railway / Fly.io)

SyncStream includes a production-grade multi-stage `Dockerfile` that builds both the React UI and Spring Boot backend into a single container.

#### **Deploy to Render**
1. Push your repository to GitHub / GitLab.
2. Go to [Render.com](https://render.com) and click **New + Web Service**.
3. Connect your repository.
4. Select **Docker** environment.
5. In the **Environment Variables** tab, add:
   ```env
   SPOTIFY_CLIENT_ID=your_spotify_client_id
   SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
   SPOTIFY_REDIRECT_URI=https://your-app-name.onrender.com/api/auth/spotify/callback
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   GOOGLE_REDIRECT_URI=https://your-app-name.onrender.com/api/auth/google/callback
   DEMO_MODE=true
   ```
6. Click **Create Web Service**. Render will build and deploy the container automatically!

#### **Deploy to Railway**
1. Go to [Railway.app](https://railway.app) and create a **New Project**.
2. Select **Deploy from GitHub Repo**.
3. Railway detects the `Dockerfile` automatically.
4. Set your environment variables in the **Variables** tab.
5. Click **Deploy**.

#### **Deploy to Fly.io**
1. Install Fly CLI: `flyctl auth login`
2. Run `fly launch` in the root folder.
3. Set your secrets:
   ```bash
   fly secrets set SPOTIFY_CLIENT_ID=... SPOTIFY_CLIENT_SECRET=... GOOGLE_CLIENT_ID=... GOOGLE_CLIENT_SECRET=...
   ```
4. Run `fly deploy`.

---

### Option 2: Docker & Docker Compose (VPS / AWS EC2 / DigitalOcean)

1. Clone repository on your server:
   ```bash
   git clone <your-repo-url> syncstream
   cd syncstream
   ```

2. Copy and fill the `.env` file:
   ```bash
   cp .env.example .env
   nano .env
   ```

3. Launch the containerized application with Docker Compose:
   ```bash
   docker compose up -d --build
   ```

4. Your application will be live at `http://your-server-ip:8080`!

---

### Option 3: Manual Jar Build & Local / Server Execution

1. Build frontend assets:
   ```bash
   cd client
   npm install
   npm run build
   cd ..
   ```

2. Copy built frontend into Spring Boot static directory:
   ```bash
   mkdir -p src/main/resources/static
   cp -r client/dist/* src/main/resources/static/
   ```

3. Package executable JAR:
   ```bash
   mvn clean package -DskipTests
   ```

4. Run the production JAR with Java 21:
   ```bash
   java -jar target/syncstream-backend-1.0.0.jar
   ```

---

## 🔑 Obtaining OAuth API Keys

### 1. Spotify Developer Dashboard
- Go to [developer.spotify.com/dashboard](https://developer.spotify.com/dashboard)
- Create a new app and set the **Redirect URI** to:
  `http://localhost:8080/api/auth/spotify/callback` (or your production URL `https://your-domain.com/api/auth/spotify/callback`)
- Copy **Client ID** and **Client Secret**.

### 2. Google Cloud Console (YouTube Data API v3)
- Go to [console.cloud.google.com](https://console.cloud.google.com)
- Create a project and enable **YouTube Data API v3**.
- Go to **Credentials -> Create Credentials -> OAuth Client ID (Web Application)**.
- Add Authorized Redirect URI:
  `http://localhost:8080/api/auth/google/callback` (or your production URL `https://your-domain.com/api/auth/google/callback`)
- Copy **Client ID** and **Client Secret**.
