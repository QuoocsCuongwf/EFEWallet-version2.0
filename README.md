# EFEWallet System

Hệ thống ví điện tử theo kiến trúc microservices, gồm backend Spring Boot và frontend Next.js.

## 1. Thành phần dự án

| Module | Vai trò | Port mặc định |
|---|---|---|
| `GatewayService` | API Gateway, định tuyến request vào các service | `8080` |
| `AuthService` | Đăng ký/đăng nhập, OTP, JWT, profile | `8083` |
| `WalletService` | Ví, số dư, chuyển tiền, lịch sử giao dịch | `8084` |
| `NotificationService` | Gửi thông báo qua Kafka/Mail (độc lập) | `8085` |
| `EFE_Gui` | Frontend Next.js (UI ví điện tử) | `3000` |

## 2. Kiến trúc tổng quan

- Client gọi API qua `GatewayService`.
- `GatewayService` route:
  - `/api/v1/auth/**` -> `AuthService`
  - `/api/v1/wallet/**` -> `WalletService`
- `AuthService` và `WalletService` dùng PostgreSQL riêng.
- Redis/RabbitMQ/Kafka phục vụ cache, queue, event.
- Frontend gọi backend qua `NEXT_PUBLIC_API_BASE_URL` (mặc định `http://localhost:8080`).

## 3. Các API wallet chính đã có

- `GET /api/v1/wallet/` - thông tin ví.
- `GET /api/v1/wallet/balance` - số dư ví.
- `POST /api/v1/wallet/transfer` - chuyển tiền (có `Idempotency-Key`).
- `GET /api/v1/wallet/transactions` - lịch sử giao dịch (phân trang, filter `status`/`type`).
- `GET /api/v1/wallet/transactions/{id}` - chi tiết giao dịch.

## 4. Chạy nhanh bằng Docker (khuyên dùng)

```bash
docker compose up --build
```

Lệnh trên dựng:
- PostgreSQL (`auth-db`, `transaction-db`, `wallet-db`)
- Redis (`wallet-redis`)
- RabbitMQ (`wallet-rabbitmq`)
- `auth-service`, `wallet-service`, `gateway-service`

> Kafka/Zookeeper có file riêng: `docker-compose.kafka.yml`.

Chạy thêm Kafka nếu cần:

```bash
docker compose -f docker-compose.kafka.yml up -d
```

## 5. Chạy local từng service (không dùng Docker cho app)

### 5.1 Yêu cầu

- Java 21
- Maven Wrapper (`./mvnw` có sẵn từng service)
- PostgreSQL, Redis, RabbitMQ, Kafka (có thể dùng docker-compose cho infra)
- Node.js 20+

### 5.2 Chạy backend

```bash
# Terminal 1
cd AuthService
./mvnw spring-boot:run

# Terminal 2
cd WalletService
./mvnw spring-boot:run

# Terminal 3
cd GatewayService
./mvnw spring-boot:run
```

`NotificationService` chạy độc lập khi cần:

```bash
cd NotificationService
./mvnw spring-boot:run
```

### 5.3 Chạy frontend

Tại thư mục gốc repo:

```bash
npm install
npm run dev -- EFE_Gui
```

Frontend đọc biến môi trường từ `.env.example`:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## 6. Luồng sử dụng chính

1. Đăng ký/đăng nhập qua `AuthService` (qua gateway).
2. Frontend gọi wallet summary:
   - `GET /api/v1/wallet/`
   - `GET /api/v1/wallet/balance`
3. Chuyển tiền:
   - Verify OTP chuyển tiền
   - `POST /api/v1/wallet/transfer` với `Idempotency-Key`
4. Lịch sử giao dịch hiển thị từ:
   - `GET /api/v1/wallet/transactions`

## 7. Cấu trúc thư mục

```text
EFEWallet/
├── AuthService/
├── WalletService/
├── GatewayService/
├── NotificationService/
├── EFE_Gui/
├── docker-compose.yml
├── docker-compose.db.yml
└── docker-compose.kafka.yml
```

## 8. CI/CD (GitHub Actions)

### CI (`.github/workflows/ci.yml`)
Chạy trên mọi PR và push vào `main` / `main.cuong.dev`:
- Test `AuthService` (PostgreSQL + Redis)
- Test `WalletService`, `GatewayService`
- Compile `NotificationService`
- Build frontend Next.js (`EFE_Gui`)
- Smoke build Docker image cho các service

### CD (`.github/workflows/cd.yml`)
Chạy khi push `main` (hoặc chạy tay qua Actions):
- Build & push image lên GHCR:
  - `ghcr.io/<owner>/efewallet-auth-service`
  - `ghcr.io/<owner>/efewallet-wallet-service`
  - `ghcr.io/<owner>/efewallet-gateway-service`
  - `ghcr.io/<owner>/efewallet-notification-service`
- Tag: `latest`, `sha-<short>`

> Package GHCR mặc định private theo org/user. Cần quyền `packages: write` (workflow đã cấu hình với `GITHUB_TOKEN`).

## 9. Ghi chú phát triển

- Gateway mặc định cho phép CORS từ `http://localhost:3000,http://localhost:3001`.
- `WalletService` giới hạn page size lịch sử giao dịch tối đa 50.
- Hệ thống đang ưu tiên môi trường local/dev (`ddl-auto=update`).

## 10. License

Dự án phục vụ mục đích học tập và nghiên cứu.
