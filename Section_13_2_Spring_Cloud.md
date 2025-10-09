## ---------- spring cloud ------
1) ![img_96.png](img_96.png)
2) ![img_97.png](img_97.png)
3) ![img_98.png](img_98.png)
4) ![img_99.png](img_99.png)
5) ![img_100.png](img_100.png)
6) ![img_101.png](img_101.png)
7) ![img_102.png](img_102.png)

## ------- setting up spring cloud config server for microservice---
1) you can read the content: https://docs.spring.io/spring-cloud-config/docs/current/reference/html/
2) ![img_103.png](img_103.png)
3) ![img_104.png](img_104.png)

## --- why we are using spring cloud --------
Excellent question, Ritik 👏 —
this is one of the **most important concepts** when you move from normal Spring Boot apps to **Microservices architecture**.

Let’s break it down clearly 👇

---

## 🌥️ **What is Spring Cloud?**

**Spring Cloud** is a collection of tools built on top of **Spring Boot** that helps you **develop, deploy, and manage distributed (microservice-based) systems** easily.

Spring Boot makes it easy to build **a single service**,
but **Spring Cloud** makes it easy to make **many services work together**.

---

## 🧩 **Why We Need Spring Cloud**

When you move to **microservices**, you face new challenges that don’t exist in monolithic applications:

| Problem in Microservices                                                                                    | How Spring Cloud Helps                                      |
| ----------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------- |
| 1️⃣ **Service Discovery** – How do services find each other when IPs keep changing?                         | 🧭 **Spring Cloud Netflix Eureka** (Service Registry)       |
| 2️⃣ **Configuration Management** – How do we keep environment configs (DB URL, API keys, etc.) centralized? | ⚙️ **Spring Cloud Config Server**                           |
| 3️⃣ **Load Balancing** – How do we distribute requests among service instances?                             | ⚖️ **Spring Cloud LoadBalancer / Ribbon**                   |
| 4️⃣ **API Gateway** – How to expose one entry point for all microservices?                                  | 🚪 **Spring Cloud Gateway / Zuul**                          |
| 5️⃣ **Fault Tolerance** – What if one service fails?                                                        | 💪 **Spring Cloud Circuit Breaker (Resilience4J, Hystrix)** |
| 6️⃣ **Distributed Tracing** – How to trace a request across multiple microservices?                         | 🔍 **Spring Cloud Sleuth + Zipkin**                         |
| 7️⃣ **Messaging / Event Streaming** – How to make services communicate asynchronously?                      | 📨 **Spring Cloud Stream (Kafka, RabbitMQ)**                |
| 8️⃣ **Centralized Security** – How to secure microservices easily?                                          | 🔐 **Spring Cloud Security + OAuth2**                       |

---

## 🧱 Example: Without vs With Spring Cloud

### ❌ Without Spring Cloud

You’d have to manually:

* Store configs inside each service.
* Manually update URLs when IPs change.
* Handle failures and retries by yourself.
* Write gateway logic manually.

It becomes **complex and error-prone** as the number of services grows.

---

### ✅ With Spring Cloud

You get:

* Central **Config Server** to update configs dynamically.
* **Eureka** service registry for auto service discovery.
* **Gateway** for routing and load balancing.
* **Resilience4J** for retries, circuit breakers, and fallbacks.
* **Sleuth + Zipkin** for tracing all service calls.

➡️ This makes your microservice system **robust, scalable, and easy to maintain**.

---

## ⚙️ Example Setup

Imagine a microservices system:

* **user-service**
* **order-service**
* **config-server**
* **eureka-server**
* **api-gateway**

Each service is a Spring Boot app.
Spring Cloud connects them all like this:

```
        +-------------------+
        |  Config Server    |
        +-------------------+
                 |
   +-------------+-------------+
   |                           |
+------+      +------+      +------+
|User  |<---->|Eureka|<---->|Order |
|Service|     |Server|     |Service|
+------+      +------+      +------+
                 |
         +---------------+
         | API Gateway    |
         +---------------+
```

Everything is automatically discoverable, configurable, and scalable 🔥

---

## 🚀 Common Spring Cloud Components

| Component                        | Purpose                                 |
| -------------------------------- | --------------------------------------- |
| **Spring Cloud Config Server**   | Centralized configuration management    |
| **Eureka Server**                | Service registry (discovery)            |
| **Spring Cloud Gateway**         | Routing + API Gateway                   |
| **Resilience4J / Hystrix**       | Fault tolerance, circuit breaker        |
| **Spring Cloud Sleuth + Zipkin** | Distributed tracing                     |
| **Spring Cloud Stream**          | Event-driven messaging (Kafka/RabbitMQ) |

---

## 💡 In Short

| Spring Boot                                   | Spring Cloud                                                    |
| --------------------------------------------- | --------------------------------------------------------------- |
| Helps you build a **single service** quickly. | Helps you make **multiple services work together** effectively. |
| Focus: app-level development.                 | Focus: system-level coordination.                               |

---

Would you like me to give you a **small example architecture** showing how Spring Cloud Config + Eureka + Gateway work together in one mini project?

yes:
Perfect 🔥
Let’s build a **mini microservices architecture** using **Spring Boot + Spring Cloud**, step by step — this will make everything click for you clearly, Ritik 👇

---

## 🌐 **Overview: Our Mini Architecture**

We’ll create **3 microservices** + **2 Spring Cloud services**

```
+-------------------+
| Config Server     |  ← Centralized configs
+-------------------+

+-------------------+
| Eureka Server     |  ← Service discovery
+-------------------+

       ↓ Registered here ↓

+-------------------+      +-------------------+
| User Service      |<---->| Order Service     |
+-------------------+      +-------------------+
       ↑                          ↑
       |                          |
       +----------+---------------+
                  |
         +-------------------+
         | API Gateway       |  ← Single entry point for clients
         +-------------------+
```

---

## 🧱 **Step-by-Step Setup**

---

### 🧩 **1️⃣ Config Server (central config management)**

#### 📁 `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
    </dependency>
</dependencies>
```

#### 🏗️ `application.yml`

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/ritiksharma/config-repo  # or local folder
```

#### 🚀 `ConfigServerApplication.java`

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

This server fetches config for all microservices from one repo (Git/local).

---

### 🧩 **2️⃣ Eureka Server (service registry)**

#### 📁 `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>
```

#### ⚙️ `application.yml`

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

#### 🚀 `EurekaServerApplication.java`

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

Now you can see all registered services at:
👉 `http://localhost:8761`

---

### 🧩 **3️⃣ User Service**

#### ⚙️ `application.yml`

```yaml
server:
  port: 8081
spring:
  application:
    name: user-service
  config:
    import: "optional:configserver:http://localhost:8888"

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

#### 🧠 `UserController.java`

```java
@RestController
@RequestMapping("/users")
public class UserController {
    @GetMapping("/info")
    public String userInfo() {
        return "User Service running!";
    }
}
```

---

### 🧩 **4️⃣ Order Service**

#### ⚙️ `application.yml`

```yaml
server:
  port: 8082
spring:
  application:
    name: order-service
  config:
    import: "optional:configserver:http://localhost:8888"

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

#### 🧠 `OrderController.java`

```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    @GetMapping("/info")
    public String orderInfo() {
        return "Order Service running!";
    }
}
```

---

### 🧩 **5️⃣ API Gateway (single entry point)**

#### 📁 `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

#### ⚙️ `application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/users/**
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/orders/**
```

Now your gateway acts as a **router** to backend microservices through Eureka.

---

## ⚡ **Final Flow**

1. Config Server loads environment configs for all services.
2. Each service registers itself to **Eureka Server**.
3. The **API Gateway** routes all incoming requests to the right service.
4. You can now call:

    * `http://localhost:8080/users/info`
    * `http://localhost:8080/orders/info`

All through one entry point 🎯

---

## 🌟 **Benefits You Gain**

✅ Centralized config (no duplicate config in every service)
✅ Service discovery (dynamic IPs, no hardcoded URLs)
✅ Unified gateway (security, rate limit, auth, logging)
✅ Load balancing (via Eureka + Gateway)
✅ Scalability and resilience

---

Perfect 👍 — let’s go deeper.

---

### 🌩️ **Spring Cloud — In-Depth Overview**

Spring Cloud is a **powerful framework built on top of Spring Boot** that provides **tools for building distributed, cloud-native microservices**.
It simplifies the **complex challenges** that arise when you move from a monolithic application to a **microservice architecture**.

---

## 🚀 1. Why We Need Spring Cloud

When you have **multiple microservices**, each running independently, you face challenges such as:

| Problem                      | Example                                                              | Spring Cloud Solution                           |
| ---------------------------- | -------------------------------------------------------------------- | ----------------------------------------------- |
| **Configuration Management** | Each service needs its own properties (DB URLs, credentials, ports). | ✅ *Spring Cloud Config Server*                  |
| **Service Discovery**        | Services need to find and talk to each other dynamically.            | ✅ *Eureka (Netflix Service Registry)*           |
| **Load Balancing**           | Multiple instances of the same service.                              | ✅ *Spring Cloud LoadBalancer / Ribbon*          |
| **API Gateway / Routing**    | One entry point for all microservices.                               | ✅ *Spring Cloud Gateway / Zuul*                 |
| **Fault Tolerance**          | One service failing should not crash others.                         | ✅ *Resilience4j / Hystrix*                      |
| **Distributed Tracing**      | Track a request across many services.                                | ✅ *Spring Cloud Sleuth + Zipkin*                |
| **Centralized Logging**      | Collect logs from all microservices.                                 | ✅ *ELK Stack (ElasticSearch, Logstash, Kibana)* |
| **Messaging & Events**       | Communicate asynchronously between services.                         | ✅ *Spring Cloud Stream (Kafka/RabbitMQ)*        |

---

## 🧠 2. Core Components of Spring Cloud

| Component                                       | Description                                                                    |
| ----------------------------------------------- | ------------------------------------------------------------------------------ |
| **Spring Cloud Config**                         | Centralized configuration management (application settings in Git or Vault).   |
| **Spring Cloud Netflix Eureka**                 | Service registry and discovery (register and locate microservices).            |
| **Spring Cloud Gateway**                        | API Gateway — routes client requests to services.                              |
| **Spring Cloud LoadBalancer**                   | Client-side load balancing (distributes requests evenly).                      |
| **Spring Cloud Sleuth**                         | Adds unique trace IDs to logs for distributed tracing.                         |
| **Spring Cloud Bus**                            | Propagates config changes across services dynamically using Kafka or RabbitMQ. |
| **Spring Cloud Circuit Breaker (Resilience4j)** | Prevents cascading failures using fallback methods.                            |
| **Spring Cloud Stream**                         | Event-driven communication (Kafka / RabbitMQ integration).                     |

---

## 🏗️ 3. Example — Simple Spring Cloud Architecture

```
           ┌──────────────────────┐
           │   Spring Cloud API   │
           │       Gateway        │
           └──────────┬───────────┘
                      │
    ┌─────────────────┴──────────────────┐
    │                                    │
┌───────────────┐                 ┌───────────────┐
│  User Service │◄───Eureka──────►│  Product Svc  │
└───────────────┘                 └───────────────┘
          ▲                               ▲
          │                               │
  ┌─────────────┐                 ┌─────────────┐
  │ Config Svr  │                 │ MongoDB/Postgres │
  └─────────────┘                 └─────────────┘
```

✅ **Flow Example:**

1. Client sends request → **Gateway**
2. Gateway checks Eureka for available service instance.
3. Gateway routes to **User Service**.
4. User Service fetches config from **Config Server**.
5. Config Server fetches from Git or local `.yml` file.

---

## ⚙️ 4. Example: Config Server Setup

### `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

### `application.yml`

```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/ritiksharma/config-repo
```

### `@SpringBootApplication`

```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

---

## 🌍 5. Benefits of Spring Cloud

✅ Centralized and dynamic configuration
✅ Fault tolerance and resilience
✅ Easier scalability of microservices
✅ Simplified inter-service communication
✅ Monitoring and distributed tracing support
✅ Works seamlessly with Docker and Kubernetes

---

Would you like me to show a **real microservice example** using:
👉 Config Server,
👉 Eureka Discovery, and
👉 Gateway (API Gateway setup)?

That will help you understand Spring Cloud end-to-end.

Would you like me to show how to **add Spring Cloud Config repo setup** (with Git) — so you can see how config updates automatically propagate to services?
---
yes: but latter

## ---------- 141. Configuring a Git Repository for Spring Cloud config server ----
1) create a repo in the git and after created create a file as well
2) ![img_105.png](img_105.png)
3) so we will create a git repos of configDemo project and then we will map that to SpringCLoudConfigServer
4) ![img_106.png](img_106.png)
5) name must be same 
6) ![img_107.png](img_107.png)
## --- now mapping config server to git Repository for version controllerd configuration
1) now we have to map the SpringCloudConfigServer so add the details as below 
2) ![img_108.png](img_108.png)
3) start the server
4) ![img_109.png](img_109.png)
-- to fix
   Perfect 👀 this is a **common Spring Cloud Config Server startup error** — your error means:

> ❌ The Config Server started, but it **could not find or access your Git repository** that contains the configuration files.

---

### 🧠 Let’s break it down

Error summary:

```
Invalid config server configuration.
If you are using the git profile, you need to set a Git URI in your configuration.
```

This means your application’s `application.yml` (or `application.properties`) for the **Spring Cloud Config Server** is either **misconfigured**, or **the Git repo URL is not accessible**.

---

### ✅ Correct Configuration Example

Here’s how your `application.yml` should look inside the **Config Server** project:

```yaml
server:
  port: 8888

spring:
  application:
    name: SpringCloudConfigServer
  cloud:
    config:
      server:
        git:
          uri: https://github.com/Ritiksharmaji/SpringBoot-Cloud-Config-Server
          clone-on-start: true
          default-label: main
```

---

### 🧩 Common Issues & Fixes

1. **❌ Wrong key name**

    * You used `url:` but it should be `uri:` under `spring.cloud.config.server.git`.
    * ✅ **Fix:** change

      ```yaml
      url: https://github.com/...
      ```

      to

      ```yaml
      uri: https://github.com/...
      ```

---

2. **❌ Missing or incorrect folder structure in Git repo**

    * Your Git repo must contain configuration files like:

      ```
      application.yml
      microservice-name.yml
      ```
    * Example:

      ```
      ├── application.yml
      ├── user-service.yml
      └── order-service.yml
      ```

---

3. **❌ Private Git repo without credentials**

    * If your repo is private, you must provide credentials:

      ```yaml
      spring:
        cloud:
          config:
            server:
              git:
                uri: https://github.com/Ritiksharmaji/SpringBoot-Cloud-Config-Server
                username: YOUR_GITHUB_USERNAME
                password: YOUR_PERSONAL_ACCESS_TOKEN
      ```
    * To generate a token: [GitHub → Settings → Developer Settings → Personal Access Tokens → Fine-grained Tokens].

---

4. **✅ Add `@EnableConfigServer`**

    * In your main class (e.g., `SpringCloudConfigServerApplication.java`), add:

      ```java
      @EnableConfigServer
      @SpringBootApplication
      public class SpringCloudConfigServerApplication {
          public static void main(String[] args) {
              SpringApplication.run(SpringCloudConfigServerApplication.class, args);
          }
      }
      ```

---

5. **🔍 Enable debug for detailed log**

    * Run the app with:

      ```
      mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
      ```
    * or add `debug: true` to your `application.yml` temporarily.

---

6. **✅ Check Git branch**

    * Ensure your repo’s default branch is `main`.
    * If not, update the config:

      ```yaml
      default-label: master
      ```

---

### ✅ Example Final `application.yml`

```yaml
server:
  port: 8888

spring:
  application:
    name: SpringCloudConfigServer
  cloud:
    config:
      server:
        git:
          uri: https://github.com/Ritiksharmaji/SpringBoot-Cloud-Config-Server
          clone-on-start: true
          default-label: main
```

---

If you share your **GitHub repo link structure** (like what files are inside it), I can check whether the repo setup is correct for Spring Cloud Config use.

Would you like me to verify your repo’s structure now?

1) ![img_110.png](img_110.png)
2) ![img_111.png](img_111.png)
3) ![img_112.png](img_112.png)

## --------- Accessing Different profile Configuration from the config server --
1) create a new other file in that git as: SpringConfigDemo-Dev.yml
2) ![img_113.png](img_113.png)
3) ![img_114.png](img_114.png)
4) now restart the application and see the URL and hit it based on the profile and see the result
5) ![img_115.png](img_115.png)
6) ![img_116.png](img_116.png)
7) ![img_117.png](img_117.png)


## -------- Setting Up a spring CLoud config client for Microservices ------
1) so first we need to encloud the spring cloud based maven configuration as below
   ```
   	<properties>
		<java.version>21</java.version>
        <spring-cloud.version>2025.0.0</spring-cloud.version>
	</properties>
     <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
   <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

2) then  we need to add the spring cloud config details into applocation.yml of clientConfig project
3) ![img_118.png](img_118.png)

```declarative
22:19:01.554 [main] ERROR org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter -- 

***************************
APPLICATION FAILED TO START
***************************

Description:

Failed to bind properties under 'spring.config' to org.springframework.boot.context.config.ConfigDataProperties:

    Reason: org.springframework.core.convert.ConverterNotFoundException: No converter found capable of converting from type [java.lang.String] to type [org.springframework.boot.context.config.ConfigDataProperties]

Action:

Update your application's configuration


Process finished with exit code 1  while making the 
spring:
  application:
    name:SpringConfigDemo
  config:
    import:SpringCloudConfigServer:http://localhost:8888

#  profiles:
#    active: prod

server:
  port: ${SERVER_PORT:8080}
build:
  id: ${BUILD_ID:000}
  version: ${BUILD_VERSION:1.0.0}
  name: ${BUILD_NAME:default}
  

Excellent — you’re **very close**, but this error:

> `No converter found capable of converting from type [java.lang.String] to type [org.springframework.boot.context.config.ConfigDataProperties]`

means that the syntax in your YAML is **slightly wrong** for importing a config server.
Let’s fix it properly 👇

---

## ❌ Your Current YAML (has syntax issues)

```yaml
spring:
  application:
    name:SpringConfigDemo
  config:
    import:SpringCloudConfigServer:http://localhost:8888
```

### Issues:

1. Missing **space after colons** (`name: SpringConfigDemo`).
2. The value of `spring.config.import` is **missing quotes**.
3. The correct import key should include `optional:` if you don’t want startup to fail when server is down.
4. You used **SpringCloudConfigServer:** — but it should be **configserver:** (lowercase, no space).

---

## ✅ Corrected `application.yml`

```yaml
spring:
  application:
    name: SpringConfigDemo

  config:
    import: "optional:configserver:http://localhost:8888"

server:
  port: ${SERVER_PORT:8080}

build:
  id: ${BUILD_ID:000}
  version: ${BUILD_VERSION:1.0.0}
  name: ${BUILD_NAME:default}
```

---

### 🧠 Why This Works

* `spring.config.import` tells your Spring Boot app to **fetch configuration** from the Config Server.
* The syntax must be:

  ```
  spring.config.import: "optional:configserver:<url>"
  ```
* The `optional:` prefix makes startup succeed even if config server is temporarily unreachable.
* `configserver:` is the fixed keyword — not your project name.

---

### ✅ Extra Checks

* Ensure your **Config Server** is already running on port `8888`.
  Test in browser:

  ```
  http://localhost:8888/SpringConfigDemo/default
  ```

  It should return a JSON with configuration data.

* In your **Config Server repo**, make sure you have a file:

  ```
  SpringConfigDemo.yml
  ```

  or

  ```
  SpringConfigDemo.properties
  ```

---

### ⚙ Example `SpringConfigDemo.yml` (in your Git repo)

```yaml
server:
  port: 9090

message:
  text: "Hello from Config Server!"
```

---

Once everything is correct:

1. Start your **Config Server** first.
2. Then start your **SpringConfigDemo** app.

It should fetch configs automatically 🎯

---
1) ![img_119.png](img_119.png)
2) ![img_120.png](img_120.png)
3) ![img_121.png](img_121.png)
4) 

Got it ✅ — you want your **Spring Cloud Config Client** setup fixed so that:

* It properly fetches data from the **Spring Cloud Config Server** at `http://localhost:8888`.
* It correctly uses fallback values if the config server or environment variables are not available.

Here’s the **fixed, working version** 👇

---

### ✅ 1️⃣ `BuildInfoController.java`

```java
package config_info.ConfifurationDemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuildInfoController {

    // These values will come from Config Server if available, otherwise default values will be used
    @Value("${build.id:1001}")
    private String buildId;

    @Value("${build.version:1.0.0}")
    private String buildVersion;

    @Value("${build.name:Default-Build}")
    private String buildName;

    @GetMapping("/build-info")
    public String getBuildInfo() {
        return "BuildId: " + buildId + ", version: " + buildVersion + ", build name: " + buildName;
    }
}
```

---

### ✅ 2️⃣ `bootstrap.yml` (place in `src/main/resources/`)

```yaml
spring:
  application:
    name: SpringConfigDemo   # must match the file name in Git repo (SpringConfigDemo.yml)
  config:
    import: "optional:configserver:http://localhost:8888"

server:
  port: 8080

# Optional fallback values (only used if config server not reachable)
build:
  id: 1001
  version: 1.0.0
  name: Default-Build
```

---

### ✅ 3️⃣ Ensure your Config Server is running

Start your **Spring Cloud Config Server** (port 8888) first.
Then check in your browser:

```
http://localhost:8888/SpringConfigDemo/default
```

You should see something like:

```json
{
  "propertySources": [
    {
      "source": {
        "build.id": 500,
        "build.version": "1.0.0",
        "build.name": "Dev Build git"
      }
    }
  ]
}
```

---

### ✅ 4️⃣ Then start your Client project

Run your **Config Client** (`ConfifurationDemoApplication`).
Now visit:

```
http://localhost:8080/build-info
```

If config server is connected →
👉 `BuildId: 500, version: 1.0.0, build name: Dev Build git`

If not →
👉 `BuildId: 1001, version: 1.0.0, build name: Default-Build`

---

Would you like me to also show how to print **which source** (Git vs local fallback) the properties came from in your response?
1) ![img_122.png](img_122.png)
2) ![img_123.png](img_123.png)
3) ![img_124.png](img_124.png)
4) if you want to change the profile then
5) ![img_125.png](img_125.png)
6) ![img_126.png](img_126.png)
7) ![img_127.png](img_127.png)
8) there will be a possibility the config server is down then your application will cresh to ovide that we use "optional:configserver:http://localhost:8888" option where Optional fallback values (only used if config server not reachable)
9) ![img_128.png](img_128.png)
10) see on above image config server is stoped now we are going to start the config client
11) ![img_129.png](img_129.png)
12) ![img_130.png](img_130.png)
13) ![img_131.png](img_131.png)
14) ![img_132.png](img_132.png)


