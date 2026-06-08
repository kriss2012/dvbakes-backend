# DvBakes SaaS - Spring Boot Backend

## Architecture Overview
```
bakery-slider/
├── spring-backend/           ← NEW: Spring Boot SaaS Backend (Java 21)
│   ├── src/main/java/com/dvbakes/
│   │   ├── controller/       ← REST API endpoints
│   │   ├── service/          ← Business logic + DB monitor
│   │   ├── entity/           ← JPA entities (Product, Order, ApiMetric)
│   │   ├── repository/       ← Spring Data JPA repositories
│   │   ├── security/         ← JWT auth + request filter
│   │   ├── config/           ← Security, WebSocket, CORS, Seeder
│   │   └── dto/              ← Data Transfer Objects
│   └── src/main/resources/
│       └── application.properties
├── backend/                  ← Original Node.js/SQLite backend (still works)
└── vite-project/             ← React frontend (enhanced)
    └── src/components/
        └── DBMonitor.jsx     ← NEW: Live DB connection monitor
```

## What's New (SaaS Enhancement)

### 🏗️ Spring Boot Backend
- **JWT Security** — Token-based authentication for all admin endpoints
- **HikariCP Connection Pool** — Production-grade connection pool (20 max, 5 min idle)
- **WebSocket (STOMP)** — Real-time DB metrics pushed to frontend every 2 seconds
- **Spring Security** — Role-based access control (ADMIN vs PUBLIC)
- **Rate Limiting** — 60 req/min per IP, returns 429 Too Many Requests
- **Spring Cache** — Product list cached for performance
- **API Metrics** — Every request logged to DB with response time
- **Spring Actuator** — Health, metrics endpoints at `/actuator/*`
- **Server Scalability** — Tomcat thread pool (200 max), connection pool auto-scaling

### 📊 Live DB Monitor (Frontend)
- Real-time HikariCP pool stats via WebSocket
- Sparkline charts for active/idle connections
- Pool bar visualization (Active/Idle/Free segments)
- JVM memory usage gauge
- Connection activity history log
- Fallback to REST polling if WebSocket unavailable

### 🔒 Security Features
- JWT tokens (24hr expiry) for admin authentication
- Spring Boot validates PIN against config (not hardcoded)
- Security headers on every response (X-Frame-Options, CSP, etc.)
- CORS allowlist for known frontend origins
- Soft deletes for products (audit trail)

## Quick Start

### Option 1: Spring Boot (Recommended)
```bash
# Prerequisites: Java 21+, Maven 3.8+
cd spring-backend
start.bat          # Windows
# OR: mvn spring-boot:run
```

### Option 2: Node.js (Legacy)
```bash
cd backend
npm start
```

### Frontend
```bash
cd vite-project
npm run dev
```

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products` | Public | Get all products |
| POST | `/api/products` | ADMIN | Add product |
| PUT | `/api/products/:id/stock` | ADMIN | Update stock |
| GET | `/api/cart` | Public | Get cart |
| POST | `/api/cart` | Public | Add to cart |
| POST | `/api/orders` | Public | Place order |
| GET | `/api/orders` | Public | Get all orders |
| PUT | `/api/orders/:id/status` | ADMIN | Update status |
| POST | `/api/auth/validate-pin` | Public | PIN auth → JWT |
| POST | `/api/auth/login` | Public | Login → JWT |
| GET | `/api/admin/dashboard` | ADMIN | Full dashboard data |
| GET | `/api/admin/db-metrics` | ADMIN | Live pool metrics |
| GET | `/api/admin/server-health` | ADMIN | JVM health |
| WS | `/ws` (STOMP) | Public | Real-time metrics |
| GET | `/actuator/health` | Public | Spring health |

## Configuration
Edit `src/main/resources/application.properties`:
```properties
# Change admin credentials
dvbakes.admin.username=owner
dvbakes.admin.password=DvBakes@2024!
dvbakes.admin.passcode=1234

# Scale connection pool
spring.datasource.hikari.maximum-pool-size=20

# Change port
server.port=8080
```
