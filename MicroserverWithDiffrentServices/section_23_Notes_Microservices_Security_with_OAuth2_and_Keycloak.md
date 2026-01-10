## ---------- Microservices_Security_with_OAuth2_and_Keycloak with full notes 0 to 100 ---
Below are **complete, structured, PDF-style notes (0 → 100 level)** on

# 🔐 **Microservices Security with OAuth2 & Keycloak**

These notes are **beginner → advanced**, **interview-ready**, and **real-world architecture focused**.

---

# 📘 Microservices Security with OAuth2 & Keycloak

**(From Zero to Production Level)**

---

## 1️⃣ Why Security Is Critical in Microservices

In microservices:

* Services are **distributed**
* Communication happens over **networks**
* Many entry points exist

❌ Traditional session-based security **fails**

### Challenges:

* Multiple services
* Stateless APIs
* Scalability
* Secure inter-service communication
* Centralized authentication

✅ **Solution** → OAuth2 + OpenID Connect + Keycloak

---

## 2️⃣ What Is OAuth 2.0?

### 📌 Definition

**OAuth 2.0** is an **authorization framework** that allows applications to access protected resources **without sharing passwords**.

> OAuth2 answers:
> **“What can you access?”**

---

## 3️⃣ OAuth2 Key Terms (MUST KNOW)

| Term                 | Meaning        |
| -------------------- | -------------- |
| Resource Owner       | User           |
| Client               | Frontend / App |
| Authorization Server | Keycloak       |
| Resource Server      | Microservice   |
| Access Token         | JWT token      |
| Scope                | Permission     |
| Refresh Token        | Token renewal  |

---

## 4️⃣ OAuth2 Roles in Microservices

```
User → Client → Authorization Server (Keycloak)
                     ↓
              Access Token (JWT)
                     ↓
              Resource Server (Microservice)
```

---

## 5️⃣ OAuth2 Grant Types (IMPORTANT)

### 🔹 1. Authorization Code (MOST USED)

* Web & SPA apps
* Secure
* Uses redirect + token exchange

### 🔹 2. Client Credentials

* Service-to-service communication
* No user involved

### 🔹 3. Password Grant ❌ (Deprecated)

* Username/password directly
* Not recommended

### 🔹 4. Refresh Token

* Used to get new access tokens

---

## 6️⃣ What Is OpenID Connect (OIDC)?

📌 **OIDC = Authentication layer on OAuth2**

> OAuth2 → Authorization
> OIDC → Authentication (Who you are)

### Adds:

* ID Token
* User profile
* Standard login flow

---

## 7️⃣ What Is Keycloak?

### 📌 Definition

**Keycloak** is an **open-source Identity & Access Management (IAM)** tool by Red Hat.

### Features:

✔ OAuth2
✔ OpenID Connect
✔ JWT
✔ SSO
✔ Role-based access
✔ LDAP / Active Directory
✔ Social login

---

## 8️⃣ Why Keycloak for Microservices?

| Problem          | Keycloak Solves |
| ---------------- | --------------- |
| Central login    | ✅               |
| Token management | ✅               |
| Role-based auth  | ✅               |
| SSO              | ✅               |
| Scalability      | ✅               |

---

## 9️⃣ Keycloak Core Concepts

### 🔹 Realm

* Security domain
* Example: `ecommerce-realm`

### 🔹 Client

* Application registered in Keycloak
* Example: `api-gateway`, `frontend`

### 🔹 User

* End user

### 🔹 Role

* Permission (ADMIN, USER)

### 🔹 Group

* Collection of users

---

## 🔟 Keycloak Architecture

```
Frontend / Client
      |
      | OAuth2 / OIDC
      ↓
Keycloak (Auth Server)
      |
      | JWT Token
      ↓
API Gateway / Microservices
```

---

## 11️⃣ JWT (JSON Web Token) Explained

### Structure:

```
HEADER.PAYLOAD.SIGNATURE
```

### Payload contains:

* sub (user id)
* roles
* scopes
* expiry

### Why JWT?

✔ Stateless
✔ Fast
✔ No DB lookup

---

## 12️⃣ Security Architecture in Microservices

### Recommended Architecture

```
Client
  ↓
API Gateway (OAuth2 Resource Server)
  ↓
Microservices (JWT validation)
```

✔ Centralized security
✔ No token duplication
✔ Reduced complexity

---

## 13️⃣ API Gateway Security (BEST PRACTICE)

### Responsibilities:

* Validate JWT
* Forward user context
* Route requests
* Rate limiting

❌ Do NOT authenticate in every service

---

## 14️⃣ Securing API Gateway with Keycloak

### Flow:

1. Client logs in via Keycloak
2. Gets JWT
3. Sends token to API Gateway
4. Gateway validates token
5. Gateway forwards request

---

## 15️⃣ Securing Microservices

### Two Approaches

### 🔹 Option 1: Gateway-Only Security

* Services trust gateway
* Faster
* Less secure

### 🔹 Option 2: Gateway + Service Security (BEST)

* Each service validates JWT
* Defense-in-depth

---

## 16️⃣ Role-Based Access Control (RBAC)

### Example:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin")
public String adminOnly() {}
```

### Roles come from:

* JWT token
* Keycloak realm roles

---

## 17️⃣ Method-Level Security

Enable:

```java
@EnableMethodSecurity
```

Use:

* `@PreAuthorize`
* `@PostAuthorize`
* `@Secured`

---

## 18️⃣ Client Credentials Flow (Service-to-Service)

### Use Case:

* Order → Inventory service

### Flow:

```
Service A → Keycloak → Token → Service B
```

✔ No user context
✔ Secure backend communication

---

## 19️⃣ Token Validation in Spring Boot

Spring Security automatically:

* Verifies signature
* Checks expiry
* Validates issuer

Using:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/myrealm
```

---

## 20️⃣ Token Propagation Between Services

Gateway forwards token:

```http
Authorization: Bearer eyJhbGciOi...
```

Services extract:

```java
SecurityContextHolder.getContext()
```

---

## 21️⃣ Refresh Token Flow

✔ Long sessions
✔ Short-lived access tokens

Client uses refresh token to request new access token.

---

## 22️⃣ Securing Public vs Protected APIs

| API Type | Security   |
| -------- | ---------- |
| Login    | Public     |
| Register | Public     |
| Orders   | Protected  |
| Admin    | Role-based |

---

## 23️⃣ Multi-Tenant Security

* One realm per tenant OR
* One realm + tenant claim

Used in SaaS applications.

---

## 24️⃣ Token Customization in Keycloak

Add:

* Custom claims
* Roles
* Organization info

---

## 25️⃣ Common Security Threats & Mitigation

| Threat              | Solution      |
| ------------------- | ------------- |
| Token theft         | HTTPS         |
| Replay attack       | Short expiry  |
| CSRF                | Stateless JWT |
| Brute force         | Rate limiting |
| Unauthorized access | RBAC          |

---

## 26️⃣ Best Practices (VERY IMPORTANT)

✅ Use short-lived tokens
✅ Secure gateway first
✅ Use HTTPS everywhere
✅ Centralize authentication
✅ Use Client Credentials for services
✅ Enable method-level security
✅ Avoid storing tokens in DB

---

## 27️⃣ Common Mistakes

❌ Validating tokens manually
❌ Using password grant
❌ No gateway security
❌ Hardcoding secrets
❌ Long token expiry

---

## 28️⃣ Interview Questions (MUST PREPARE)

1. OAuth2 vs OpenID Connect?
2. JWT vs Session?
3. Why Keycloak?
4. How does API Gateway handle security?
5. How do services communicate securely?
6. What is client credentials flow?
7. How is token validated?

---

## 29️⃣ Real-World Example (E-Commerce)

```
User → Login → Keycloak
        ↓
     JWT Token
        ↓
API Gateway → Order Service → Inventory Service
```

✔ Secure
✔ Scalable
✔ Production-ready

---

## 🔟 30️⃣ One-Line Summary

> **OAuth2 + Keycloak provide centralized, scalable, and secure authentication and authorization for microservices using JWT-based stateless security.**

---

If you want next:
✅ **Spring Boot + Keycloak full code project**
✅ **API Gateway + Keycloak integration**
✅ **PDF download format**
✅ **Architecture & sequence diagrams**
✅ **Interview Q&A only**

Just tell me 👍

## ---------- Spring Boot + Keycloak full code project ----
Perfect 👍
Below is a **complete Spring Boot + Keycloak full code project (production-style)** with **API Gateway + Microservice security**, explained step-by-step.

I’ll cover:

1. **Architecture**
2. **Keycloak setup**
3. **API Gateway (secured)**
4. **Microservice (secured)**
5. **JWT role mapping**
6. **Testing with Postman**
7. **Common issues & fixes**

---

# 🔐 Spring Boot + Keycloak Full Code Project

## 🏗️ Architecture

```
Client (Postman / Frontend)
        |
        |  JWT Token
        ↓
API Gateway (Spring Cloud Gateway)
        |
        ↓
Product Service / Order Service
        |
        ↓
Database
```

✔ Keycloak = Authorization Server
✔ Gateway + Services = Resource Servers
✔ JWT-based security

---

## 1️⃣ Keycloak Setup (MANDATORY FIRST)

### 🔹 Step 1: Start Keycloak (Docker – Recommended)

```bash
docker run -p 8080:8080 \
-e KEYCLOAK_ADMIN=admin \
-e KEYCLOAK_ADMIN_PASSWORD=admin \
quay.io/keycloak/keycloak:24.0.1 start-dev
```

Access:

```
http://localhost:8080
```

---

### 🔹 Step 2: Create Realm

* Realm name: `ecommerce-realm`

---

### 🔹 Step 3: Create Client (API Gateway)

| Field            | Value          |
| ---------------- | -------------- |
| Client ID        | api-gateway    |
| Client Type      | OpenID Connect |
| Access Type      | Confidential   |
| Authorization    | ON             |
| Service Accounts | ON             |

---

### 🔹 Step 4: Create Roles

Realm Roles:

* `USER`
* `ADMIN`

---

### 🔹 Step 5: Create User

* Username: `ritik`
* Password: `1234`
* Assign roles: `USER`

---

## 2️⃣ API Gateway – Spring Boot Project

### 🔹 Dependencies (`pom.xml`)

```xml
<dependencies>

    <!-- Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>

    <!-- OAuth2 Resource Server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Eureka Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

</dependencies>
```

---

### 🔹 application.yml (API Gateway)

```yaml
server:
  port: 8081

spring:
  application:
    name: api-gateway

  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/products/**
          filters:
            - RewritePath=/products(?<segment>/?.*), /api/products${segment}

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ecommerce-realm

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

### 🔹 Security Configuration (Gateway)

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {

        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/products/**").hasAnyRole("USER", "ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}
```

✔ JWT validated at Gateway
✔ Role-based routing

---

## 3️⃣ Product Service (Microservice)

### 🔹 Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

---

### 🔹 application.yml (Product Service)

```yaml
server:
  port: 8082

spring:
  application:
    name: product-service

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ecommerce-realm
```

---

### 🔹 Security Config (Product Service)

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/products/public").permitAll()
                .requestMatchers("/api/products/admin").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}
```

---

### 🔹 Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public String products() {
        return "Products for USER";
    }

    @GetMapping("/admin")
    public String adminProducts() {
        return "Admin Products";
    }
}
```

---

## 4️⃣ Role Mapping (IMPORTANT)

Keycloak sends roles like:

```
realm_access.roles = ["USER"]
```

Spring expects:

```
ROLE_USER
```

### 🔹 Add Converter

```java
@Bean
JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter converter =
        new JwtGrantedAuthoritiesConverter();
    converter.setAuthorityPrefix("ROLE_");
    converter.setAuthoritiesClaimName("realm_access.roles");

    JwtAuthenticationConverter jwtConverter =
        new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

    return jwtConverter;
}
```

---

## 5️⃣ Test Flow (Postman)

### 🔹 Step 1: Get Token

```
POST http://localhost:8080/realms/ecommerce-realm/protocol/openid-connect/token
```

Body (x-www-form-urlencoded):

```
client_id=api-gateway
username=ritik
password=1234
grant_type=password
```

---

### 🔹 Step 2: Call API Gateway

```
GET http://localhost:8081/products
Authorization: Bearer <TOKEN>
```

✅ Works

```
GET http://localhost:8081/products/admin
```

❌ 403 (if USER)

---

## 6️⃣ Service-to-Service Security (Client Credentials)

* Create Keycloak client: `order-service`
* Enable **Service Accounts**
* Use `client_credentials` grant

Used when:

```
Order Service → Inventory Service
```

---

## 7️⃣ Common Errors & Fixes

| Error            | Fix                   |
| ---------------- | --------------------- |
| 401 Unauthorized | Wrong issuer-uri      |
| 403 Forbidden    | Role mapping issue    |
| No roles         | Missing JWT converter |
| Token invalid    | Realm mismatch        |

---

## 8️⃣ Best Practices (Production)

✅ Secure Gateway first
✅ Short token expiry
✅ HTTPS everywhere
✅ Client credentials for services
✅ Validate JWT at services
✅ Avoid password grant

---

## 🎯 Final Summary

> **Spring Boot + Keycloak provides centralized, scalable, and stateless security for microservices using OAuth2, JWT, and role-based access control.**

---

If you want next 👇
✔ **API Gateway + Keycloak diagram**
✔ **Docker Compose for full setup**
✔ **Refresh token flow**
✔ **PDF-ready notes**
✔ **Interview Q&A**

Just say 👍
## -------  **API Gateway + Keycloak diagram** ---
![img_894.png](img_894.png)

## --- Docker Compose for full setup --
Below is a **complete, production-style Docker Compose setup** for a **Microservices system with**:

✅ **Spring Cloud API Gateway**
✅ **Keycloak (OAuth2 / OpenID Connect)**
✅ **Microservices (User, Product)**
✅ **PostgreSQL for Keycloak**
✅ **Docker networking (single bridge)**

This is a **real-world reference architecture** you can directly run and extend.

---

## 🧱 Architecture (What this setup contains)

```
Client
  ↓
API Gateway (Spring Cloud Gateway)
  ↓
Keycloak (Auth Server)
  ↓
Microservices (User / Product)
```

---

# 1️⃣ Folder Structure

```
microservices-docker/
│
├── docker-compose.yml
│
├── gateway/
│   └── Dockerfile
│
├── user-service/
│   └── Dockerfile
│
├── product-service/
│   └── Dockerfile
│
└── keycloak/
    └── realm-export.json
```

---

# 2️⃣ docker-compose.yml (FULL SETUP)

```yaml
version: '3.9'

services:

  postgres:
    image: postgres:15
    container_name: keycloak-postgres
    restart: always
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: keycloak
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - microservices-net

  keycloak:
    image: quay.io/keycloak/keycloak:24.0.3
    container_name: keycloak
    command: start-dev --import-realm
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: keycloak
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    volumes:
      - ./keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json
    networks:
      - microservices-net

  gateway:
    build: ./gateway
    container_name: api-gateway
    ports:
      - "8081:8081"
    depends_on:
      - keycloak
    environment:
      SPRING_PROFILES_ACTIVE: docker
    networks:
      - microservices-net

  user-service:
    build: ./user-service
    container_name: user-service
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: docker
    networks:
      - microservices-net

  product-service:
    build: ./product-service
    container_name: product-service
    ports:
      - "8083:8083"
    environment:
      SPRING_PROFILES_ACTIVE: docker
    networks:
      - microservices-net

volumes:
  postgres_data:

networks:
  microservices-net:
    driver: bridge
```

---

# 3️⃣ Dockerfile (Common for All Spring Boot Apps)

📌 Use **same Dockerfile** in `gateway`, `user-service`, `product-service`

```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
```

---

# 4️⃣ API Gateway Configuration (application.yml)

```yaml
server:
  port: 8081

spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://user-service:8082
          predicates:
            - Path=/api/users/**

        - id: product-service
          uri: http://product-service:8083
          predicates:
            - Path=/api/products/**

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/microservices
```

---

# 5️⃣ User/Product Service Security Config

```yaml
server:
  port: 8082

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/microservices
```

---

# 6️⃣ Spring Security Config (Microservices)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

        return http.build();
    }
}
```

---

# 7️⃣ Keycloak Realm (realm-export.json – minimal)

```json
{
  "realm": "microservices",
  "enabled": true,
  "clients": [
    {
      "clientId": "gateway-client",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": true,
      "redirectUris": ["*"]
    }
  ]
}
```

---

# 8️⃣ Run Everything 🚀

```bash
docker-compose up --build
```

---

# 9️⃣ Access URLs

| Service         | URL                                            |
| --------------- | ---------------------------------------------- |
| Keycloak Admin  | [http://localhost:8080](http://localhost:8080) |
| API Gateway     | [http://localhost:8081](http://localhost:8081) |
| User Service    | [http://localhost:8082](http://localhost:8082) |
| Product Service | [http://localhost:8083](http://localhost:8083) |

---

# 🔐 Authentication Flow

1️⃣ Login via **Keycloak**
2️⃣ Get **JWT Token**
3️⃣ Send token to **API Gateway**
4️⃣ Gateway forwards to Microservice
5️⃣ Microservice validates token

---

# 🧠 Industry Best Practices Used

✅ OAuth2 Resource Server
✅ Centralized API Gateway
✅ Stateless JWT
✅ Docker Network Isolation
✅ Externalized Config
✅ Realm Import Automation

---

If you want next 👇
✅ **Kafka/RabbitMQ added**
✅ **Eureka + Load Balancing**
✅ **Resilience4J + Circuit Breaker**
✅ **Kubernetes version**

Just tell me 👍

