# Hostinger VPS Deployment Guide - Step by Step

## 📋 Prerequisites

Before starting, ensure you have:
- ✅ Hostinger VPS access (SSH credentials)
- ✅ MySQL database already configured and accessible
- ✅ Domain name (optional but recommended)
- ✅ Your project code ready

---

## 🚀 PART 1: Setup Hostinger VPS

### Step 1: Connect to VPS via SSH

```bash
ssh root@YOUR_VPS_IP
# Enter your password when prompted
```

### Step 2: Update System

```bash
apt update && apt upgrade -y
```

### Step 3: Install Docker

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Start Docker service
systemctl start docker
systemctl enable docker

# Verify installation
docker --version
```

### Step 4: Install Docker Compose

```bash
# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Make it executable
chmod +x /usr/local/bin/docker-compose

# Verify installation
docker-compose --version
```

### Step 5: Install Git

```bash
apt install git -y
git --version
```

### Step 6: Create Project Directory

```bash
mkdir -p /var/www/attendance-app
cd /var/www/attendance-app
```

---

## 📁 PART 2: Prepare Your Local Project

### Step 1: Create Project Structure Locally

```
attendance-app/
├── attendance-payroll-backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── attendance-payroll-frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── .dockerignore
│   └── .env.production
└── docker-compose.yml
```

### Step 2: Create Backend Dockerfile

Create `Dockerfile` in Spring Boot project root:

```dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 3: Create Frontend Dockerfile

Create `Dockerfile` in React project root:

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Step 4: Create nginx.conf for React

Create `nginx.conf` in React project root:

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json;
}
```

### Step 5: Create .env.production for React

Create `.env.production` in React project root:

```env
REACT_APP_API_URL=http://YOUR_VPS_IP/api
```

**Replace `YOUR_VPS_IP` with your actual VPS IP address**

### Step 6: Update React api.js

Update `src/utils/api.js`:

```javascript
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ... rest remains same
```

### Step 7: Create docker-compose.yml

Create `docker-compose.yml` in project root:

```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./attendance-payroll-backend
      dockerfile: Dockerfile
    container_name: attendance-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://YOUR_DB_HOST:3306/attendance_payroll_db
      - SPRING_DATASOURCE_USERNAME=your_db_username
      - SPRING_DATASOURCE_PASSWORD=your_db_password
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
      - JWT_SECRET=your_super_secret_jwt_key_change_this
      - JWT_EXPIRATION=86400000
    restart: unless-stopped
    networks:
      - app-network

  frontend:
    build:
      context: ./attendance-payroll-frontend
      dockerfile: Dockerfile
    container_name: attendance-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

**Important:** Replace:
- `YOUR_DB_HOST` - Your database host (from Hostinger panel)
- `your_db_username` - Your database username
- `your_db_password` - Your database password
- `your_super_secret_jwt_key_change_this` - Generate a secure random key

### Step 8: Create .dockerignore Files

**Backend .dockerignore:**
```
target/
!target/*.jar
.mvn/
mvnw
mvnw.cmd
*.log
.git
```

**Frontend .dockerignore:**
```
node_modules
build
.git
.env.local
```

---

## 📤 PART 3: Upload to VPS

### Option A: Using Git (Recommended)

#### 1. Create GitHub Repository

```bash
# On your local machine
cd attendance-app
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/attendance-app.git
git push -u origin main
```

#### 2. Clone on VPS

```bash
# On VPS
cd /var/www/attendance-app
git clone https://github.com/YOUR_USERNAME/attendance-app.git .
```

### Option B: Using SCP (Direct Upload)

```bash
# On your local machine
cd attendance-app
scp -r * root@YOUR_VPS_IP:/var/www/attendance-app/
```

### Option C: Using FileZilla/WinSCP

1. Connect to your VPS using SFTP
2. Navigate to `/var/www/attendance-app/`
3. Upload all project files

---

## 🐳 PART 4: Build and Deploy with Docker

### Step 1: Navigate to Project Directory

```bash
cd /var/www/attendance-app
```

### Step 2: Build Docker Images

```bash
# Build both frontend and backend
docker-compose build

# This may take 5-10 minutes
```

### Step 3: Start Containers

```bash
docker-compose up -d
```

### Step 4: Check Container Status

```bash
docker-compose ps
```

You should see both containers running:
```
NAME                   STATUS              PORTS
attendance-backend     Up X minutes        0.0.0.0:8080->8080/tcp
attendance-frontend    Up X minutes        0.0.0.0:80->80/tcp
```

### Step 5: View Logs

```bash
# View all logs
docker-compose logs

# View backend logs
docker-compose logs backend

# View frontend logs
docker-compose logs frontend

# Follow logs in real-time
docker-compose logs -f
```

---

## 🔧 PART 5: Configure Firewall

### Allow Required Ports

```bash
# Allow HTTP
ufw allow 80/tcp

# Allow HTTPS (if using SSL)
ufw allow 443/tcp

# Allow backend (optional, if accessing directly)
ufw allow 8080/tcp

# Enable firewall
ufw enable

# Check status
ufw status
```

---

## 🌐 PART 6: Access Your Application

### Test Backend API

```bash
curl http://YOUR_VPS_IP:8080/api/auth/login
```

### Access Frontend

Open browser and go to:
```
http://YOUR_VPS_IP
```

You should see your login page!

---

## 🔒 PART 7: Setup SSL (Optional but Recommended)

### Install Certbot

```bash
apt install certbot python3-certbot-nginx -y
```

### Get SSL Certificate

```bash
# Stop nginx container temporarily
docker-compose stop frontend

# Get certificate
certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# Start container again
docker-compose start frontend
```

### Update nginx.conf for SSL

Update `nginx.conf`:

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Update docker-compose.yml for SSL

```yaml
frontend:
  build:
    context: ./attendance-payroll-frontend
    dockerfile: Dockerfile
  container_name: attendance-frontend
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - /etc/letsencrypt:/etc/letsencrypt:ro
  depends_on:
    - backend
  restart: unless-stopped
  networks:
    - app-network
```

### Rebuild and Restart

```bash
docker-compose down
docker-compose build frontend
docker-compose up -d
```

---

## 🔄 PART 8: Common Docker Commands

### View Running Containers

```bash
docker ps
```

### Stop Containers

```bash
docker-compose down
```

### Restart Containers

```bash
docker-compose restart
```

### Rebuild After Code Changes

```bash
docker-compose down
docker-compose build
docker-compose up -d
```

### View Container Logs

```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Execute Commands in Container

```bash
# Access backend container
docker exec -it attendance-backend sh

# Access frontend container
docker exec -it attendance-frontend sh
```

### Remove All Containers and Images

```bash
docker-compose down --rmi all
```

---

## 🐛 PART 9: Troubleshooting

### Backend Not Starting

```bash
# Check logs
docker-compose logs backend

# Common issues:
# 1. Database connection - verify credentials in docker-compose.yml
# 2. Port already in use - change port mapping
# 3. Build errors - check Java version and dependencies
```

### Frontend Not Loading

```bash
# Check logs
docker-compose logs frontend

# Common issues:
# 1. API URL wrong - check .env.production
# 2. Build failed - check node modules
# 3. nginx config error - validate nginx.conf syntax
```

### Database Connection Failed

```bash
# Test database connection from VPS
mysql -h YOUR_DB_HOST -u your_username -p

# If connection fails:
# 1. Check if DB allows remote connections
# 2. Verify firewall rules
# 3. Check credentials
```

### Can't Access Application

```bash
# Check if containers are running
docker ps

# Check firewall
ufw status

# Check ports
netstat -tulpn | grep :80
netstat -tulpn | grep :8080

# Test locally on VPS
curl http://localhost:80
curl http://localhost:8080/api/auth/login
```

---

## 📊 PART 10: Monitoring

### Check Container Resource Usage

```bash
docker stats
```

### Check Disk Usage

```bash
df -h
docker system df
```

### Clean Up Unused Docker Resources

```bash
# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Clean everything
docker system prune -a
```

---

## 🔄 PART 11: Update Deployment

### When You Make Code Changes

```bash
# On your local machine
git add .
git commit -m "Update message"
git push origin main

# On VPS
cd /var/www/attendance-app
git pull origin main
docker-compose down
docker-compose build
docker-compose up -d
```

---

## ✅ Quick Deployment Checklist

- [ ] VPS SSH access configured
- [ ] Docker and Docker Compose installed
- [ ] MySQL database accessible from VPS
- [ ] All Dockerfiles created
- [ ] docker-compose.yml configured with correct DB credentials
- [ ] .env.production updated with VPS IP
- [ ] Project uploaded to VPS
- [ ] Docker images built successfully
- [ ] Containers running
- [ ] Firewall configured
- [ ] Application accessible from browser
- [ ] SSL certificate installed (optional)

---

## 🎉 Success!

Your application should now be live at:
- **Frontend**: `http://YOUR_VPS_IP` or `https://your-domain.com`
- **Backend API**: `http://YOUR_VPS_IP:8080/api` or `https://your-domain.com/api`

**Default Login:**
- Admin: `admin@company.com` / `admin123`
- HR: `hr@company.com` / `hr123`
- Employee: `employee@company.com` / `emp123`

---

## 📞 Need Help?

If you encounter issues:
1. Check container logs: `docker-compose logs`
2. Verify database connection
3. Check firewall rules
4. Ensure all environment variables are correct
5. Verify ports are not blocked by hosting provider

Good luck with your deployment! 🚀