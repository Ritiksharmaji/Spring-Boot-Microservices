# User Service (`user-service`, port 8081)

Customer profiles (name, email, phone, address) linked to the Keycloak account by the JWT `sub`. Admins manage all users.

## Endpoints

| Method | Path | Notes |
|---|---|---|
| GET/PUT | `/api/users/me` | own profile (CUSTOMER) |
| GET/POST | `/api/users` | list / create (ADMIN) |
| GET/PUT | `/api/users/{id}` | read / update (ADMIN) |
| GET | `/internal/users/{keycloakId}` | lookup for order-service (not routed) |

Full request/response details: [docs/API-REFERENCE.md](../docs/API-REFERENCE.md).

## Data

MongoDB `userdb`, collection `user`; unique indexes on `email` and `keycloakId`; auditing timestamps.

## Events

Spring Cloud Bus only. See [docs/EVENTS.md](../docs/EVENTS.md).

## Depends on

MongoDB

## Configuration

`SPRING_DATA_MONGODB_URI`, `SPRING_DATA_MONGODB_DATABASE` — all variables: [docs/CONFIGURATION.md](../docs/CONFIGURATION.md).

## Run & test

```bash
# from backend/microservices/
./mvnw -pl user-service -am spring-boot:run        # needs config-server + infrastructure (see docs/LOCAL-DEVELOPMENT.md)
./mvnw -pl user-service test                       # tests: `UserServiceTest`, `UserControllerTest`, `UserRepositoryMongoContainerTest`
docker compose up -d --build user-service          # rebuild just this container
```
