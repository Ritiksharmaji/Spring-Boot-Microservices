
# ------configuration managment system -------
1) ![img_52.png](img_52.png)
2) there are various type of configuration management system
3) ![img_53.png](img_53.png)
4) ![img_54.png](img_54.png)
5) ![img_55.png](img_55.png)
6) ![img_56.png](img_56.png)
7) ![img_57.png](img_57.png)
8) ![img_58.png](img_58.png)
9) ![img_59.png](img_59.png)

# --- why we need to talk about configuration management system(challenges of spring boot application) --
1) ![img_60.png](img_60.png)

## ------- Externalized Configuration in Spring Boot? --------
1) ![img_61.png](img_61.png)
2) ![img_62.png](img_62.png)
3) ![img_63.png](img_63.png)

## ------ configuration format ------
1) ![img_64.png](img_64.png)
2) ![img_65.png](img_65.png)
3) ![img_66.png](img_66.png)
4) ![img_67.png](img_67.png)
5) ![img_68.png](img_68.png)

## ------- create a new spring boot application and reload it in IDE --
## ---- Spring Boot Profiles -------------
1) ![img_69.png](img_69.png)
2) ![img_70.png](img_70.png)
3) benefit of spring boot profile
4) ![img_71.png](img_71.png)
5) now after creating new project modify the application.properties to application.yml and create one more for dev as well
6) ![img_72.png](img_72.png)
7) ![img_73.png](img_73.png)
8) ![img_74.png](img_74.png)
9) ![img_75.png](img_75.png)
10) ![img_76.png](img_76.png)
11) ![img_77.png](img_77.png)
12) ![img_78.png](img_78.png)
13) ![img_79.png](img_79.png)
14) 

# ---- how does spring boot load configuration -------
1) ![img_80.png](img_80.png)
2) 
Excellent question 👏 — understanding **how Spring Boot loads configuration** is *super important* for backend projects like yours.

Let’s go step by step and make it crystal clear 🔍

---

## ⚙️ 1️⃣ Overview

Spring Boot loads configuration from **multiple sources**, in a specific **order of precedence**.
These configurations define:

* Server settings (port, context path)
* Database credentials
* Logging, profiles, custom keys, etc.

---

## ⚙️ 2️⃣ Configuration Sources (Default Order)

When your Spring Boot application starts, it loads configuration in this order (highest to lowest priority):

| Priority | Source                                                                  | Example                                              |
| -------- | ----------------------------------------------------------------------- | ---------------------------------------------------- |
| 🥇 1     | **Command-line arguments**                                              | `--server.port=8085`                                 |
| 2        | **`SPRING_APPLICATION_JSON`** (environment variable)                    | `SPRING_APPLICATION_JSON={"server":{"port":8086}}`   |
| 3        | **OS environment variables**                                            | `export SERVER_PORT=8087`                            |
| 4        | **Java System properties**                                              | `-Dserver.port=8088`                                 |
| 5        | **`application.properties` or `application.yml`** (inside `/resources`) | `server.port=8089`                                   |
| 6        | **Profile-specific files**                                              | `application-dev.properties`, `application-prod.yml` |
| 7        | **Default values in code**                                              | `@Value("${server.port:8090}")` fallback value       |

✅ The **first one found** in the chain wins — higher priority overrides lower.

---

## ⚙️ 3️⃣ Configuration File Locations

Spring Boot automatically looks for configuration files in these paths (in this order):

1. `./config/` directory (outside the jar)
2. Current directory (`./`)
3. `classpath:/config/`
4. `classpath:/`

Example:

```
src/
 └── main/
     └── resources/
         ├── application.properties
         └── application.yml
```

---

## ⚙️ 4️⃣ Types of Configuration Files

You can use either:

### 🔸 `application.properties`

```
server.port=8081
spring.data.mongodb.uri=mongodb://localhost:27017/userdb
```

### 🔸 `application.yml`

```
server:
  port: 8081

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/userdb
```

Both are valid — YAML is just more structured.

---

## ⚙️ 5️⃣ Profile-specific Configuration

You can have different configs for environments:

```
application-dev.yml
application-prod.yml
```

Activate a profile via:

```
spring.profiles.active=dev
```

in your main `application.yml` or via CLI:

```
java -jar ecom.jar --spring.profiles.active=prod
```

Then only that environment’s config is loaded.

---

## ⚙️ 6️⃣ How It’s Injected into Code

Spring Boot loads all configuration values into its **Environment**, making them available via:

✅ **`@Value` annotation**

```java
@Value("${server.port}")
private int port;
```

✅ **`@ConfigurationProperties`**

```java
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class MongoProperties {
  private String uri;
  private String database;
}
```

✅ **`Environment` object**

```java
@Autowired
Environment env;
System.out.println(env.getProperty("spring.data.mongodb.uri"));
```

---

## ⚙️ 7️⃣ The Loading Mechanism Internally

Under the hood:

1. Spring Boot uses `SpringApplication.run()`
2. It creates an **ApplicationContext**
3. A **ConfigDataEnvironmentPostProcessor** loads all configuration data
4. Values are bound to beans and environment variables before the context refreshes
5. Any conflicting values follow the **order of precedence** rules above

---

## ⚙️ 8️⃣ Example: How Your App Uses It

Your current config:

```properties
spring.application.name=ecom-application
server.port=8081

spring.data.mongodb.uri=mongodb://localhost:2717/userdb
spring.data.mongodb.database=userdb
```

When you start your app:

* It reads `application.properties`
* Binds `spring.data.mongodb.*` → `MongoTemplate` and `MongoRepository`
* Runs the embedded Tomcat on port 8081
* Loads the name as `ecom-application`

---

## 🧠 Summary Diagram

```
Command-line args
       ↓
SPRING_APPLICATION_JSON
       ↓
Environment Variables
       ↓
System Properties (-D)
       ↓
application.yml / .properties
       ↓
application-{profile}.yml
       ↓
Default values in code
```

---

Would you like me to show you **how to override configurations dynamically** (for example, changing MongoDB URI based on the profile like `dev` vs `prod`)?
 ---

 - yes:
   Perfect 👍 Ritik — let’s go **step by step** on how to dynamically override configurations in **Spring Boot** using **profiles** (like `dev`, `prod`, `test`), so that you can switch environments easily (for example, different MongoDB URIs, ports, etc.).

---

## ⚙️ 1️⃣ What Are Profiles?

Spring Profiles allow you to **isolate configurations per environment** —
so you can have one config for **development**, one for **production**, one for **testing**, etc.

✅ Example:

* `application-dev.yml` → for development
* `application-prod.yml` → for production
* `application-test.yml` → for testing

---

## ⚙️ 2️⃣ Step 1: Base Configuration File

Create your default `application.yml` or `application.properties` in
`src/main/resources/`.

### ✅ Example: `application.yml`

```yaml
spring:
  application:
    name: ecom-application
  profiles:
    active: dev   # 👈 active profile (can override this)
```

---

## ⚙️ 3️⃣ Step 2: Create Environment-Specific Config Files

Now create two more files in the same folder:

### 🧩 `application-dev.yml`

```yaml
server:
  port: 8081

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/userdb
      database: userdb
  config:
    activate:
      on-profile: dev

info:
  environment: development
```

### 🧩 `application-prod.yml`

```yaml
server:
  port: 8082

spring:
  data:
    mongodb:
      uri: mongodb+srv://ritik-prod:password@cluster0.mongodb.net/ecomDB
      database: ecomDB
  config:
    activate:
      on-profile: prod

info:
  environment: production
```

---

## ⚙️ 4️⃣ Step 3: Activate Profile (3 Options)

You can activate profiles in **three ways** 👇

### ✅ Option 1: In `application.yml`

```yaml
spring:
  profiles:
    active: dev
```

### ✅ Option 2: Command Line

When running your jar:

```bash
java -jar ecom-application.jar --spring.profiles.active=prod
```

### ✅ Option 3: Environment Variable

```bash
export SPRING_PROFILES_ACTIVE=prod
```

or in Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
```

---

## ⚙️ 5️⃣ Step 4: Using Profile-Specific Values in Code

You can inject config values dynamically from the active profile:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MongoConfigInfo {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${info.environment}")
    private String environment;

    public void printConfig() {
        System.out.println("Active Mongo URI: " + mongoUri);
        System.out.println("Running in environment: " + environment);
    }
}
```

When you switch from `dev` → `prod`,
the values will automatically change.

---

## ⚙️ 6️⃣ Step 5: Verify Active Profile

You can check which profile is active in logs:

```
The following profiles are active: dev
```

Or programmatically:

```java
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileChecker {

    private final Environment env;

    public ProfileChecker(Environment env) {
        this.env = env;
    }

    public void checkProfile() {
        System.out.println("Active Profile: " + String.join(", ", env.getActiveProfiles()));
    }
}
```

---

## ⚙️ 7️⃣ Example Folder Structure

```
src/
 └── main/
     └── resources/
         ├── application.yml
         ├── application-dev.yml
         └── application-prod.yml
```

---

## ⚙️ 8️⃣ Common Real-World Use Case

| Property      | Dev Environment                    | Prod Environment                                     |
| ------------- | ---------------------------------- | ---------------------------------------------------- |
| MongoDB URI   | `mongodb://localhost:27017/userdb` | `mongodb+srv://user:pass@cluster.mongodb.net/ecomDB` |
| Server Port   | 8081                               | 80                                                   |
| Logging Level | DEBUG                              | ERROR                                                |
| CORS Policy   | Allow all                          | Restrict domain                                      |

---

## 🧠 Summary

✅ **Profiles** let you easily switch between environments.
✅ Spring Boot **automatically merges** the base config with the active one.
✅ You can activate a profile from:

* `application.yml`
* CLI
* Environment variable

---

Would you like me to show you **how to use multiple MongoDB URIs dynamically** — for example, if you want to connect to **different databases based on the active profile** (like `userDB` for dev and `mainDB` for prod)?

## ----- what is externalization configuration and why it iis used--
Excellent question, Ritik 👏

Let’s go step-by-step —
You’ll understand **what Externalized Configuration is**, **why it’s used**, and **how it works in Spring Boot** with real examples.

---

## 🧩 **1️⃣ What is Externalized Configuration in Spring Boot?**

**Externalized Configuration** means:

> Instead of hardcoding configuration values (like DB URLs, API keys, server ports, etc.) inside your code, you keep them outside the application (in external files or environment variables).

When the application runs, Spring Boot automatically loads those values and injects them wherever needed.

---

### ✅ Example (Before Externalization)

```java
public class DatabaseService {
    private String dbUrl = "mongodb://localhost:27017/userdb"; // hardcoded ❌
}
```

If you move this app to production —
you’ll need a different database URL, credentials, etc.
You’d have to **edit and rebuild your code** — that’s bad.

---

### ✅ Example (After Externalization)

**application.properties**

```properties
spring.data.mongodb.uri=mongodb://prod-server:27017/userdb
```

**Java code**

```java
@Value("${spring.data.mongodb.uri}")
private String dbUrl;
```

Now you can change the configuration **without touching the code**.
Just change the property file or environment variable.

---

## 🧠 **2️⃣ Why is Externalized Configuration used?**

| Reason              | Description                                                                                            |
| ------------------- | ------------------------------------------------------------------------------------------------------ |
| **Portability**     | Easily move the app between environments (Dev → Test → Prod) without code changes.                     |
| **Security**        | Sensitive data (passwords, API keys) can be stored in environment variables or external vaults.        |
| **Flexibility**     | Override configurations at runtime via CLI, environment variables, or config servers.                  |
| **Maintainability** | Keep code clean and configurations organized.                                                          |
| **DevOps Friendly** | Works smoothly with Docker, Kubernetes, or CI/CD systems where environment configs change dynamically. |

---

## ⚙️ **3️⃣ How does Spring Boot Load Configurations (Order of Priority)**

Spring Boot reads configuration from many places, in a specific **priority order**:

| Priority | Source                                       | Example                                        |
| -------- | -------------------------------------------- | ---------------------------------------------- |
| 1️⃣      | **Command-line arguments**                   | `--server.port=8081`                           |
| 2️⃣      | **Environment variables**                    | `SERVER_PORT=8082`                             |
| 3️⃣      | **application.properties / application.yml** | in `src/main/resources`                        |
| 4️⃣      | **application-{profile}.properties**         | like `application-dev.properties`              |
| 5️⃣      | **Default properties in code**               | via `SpringApplication.setDefaultProperties()` |

👉 Highest priority wins — if multiple sources define the same property.

---

## 🏗 **4️⃣ Example — Different Environments**

**application.properties**

```properties
spring.profiles.active=dev
```

**application-dev.properties**

```properties
server.port=8081
spring.data.mongodb.uri=mongodb://localhost:27017/devdb
```

**application-prod.properties**

```properties
server.port=9090
spring.data.mongodb.uri=mongodb://prod-server:27017/proddb
```

Run production mode with:

```
java -jar ecom-app.jar --spring.profiles.active=prod
```

---

## 🔒 **5️⃣ Real-world Uses**

* Store DB credentials or API keys in `.env` files or environment variables.
* Load configuration dynamically using **Spring Cloud Config Server** (for microservices).
* Override configs easily when deploying via Docker/Kubernetes.

---

## 🧾 **Summary**

| Concept        | Description                                                   |
| -------------- | ------------------------------------------------------------- |
| **Definition** | Keeping configuration data outside the compiled code          |
| **Purpose**    | To allow flexible and environment-specific configuration      |
| **Sources**    | `.properties`, `.yml`, env vars, CLI args, config servers     |
| **Benefits**   | Easier deployment, better security, cleaner code, reusability |

---

Would you like me to show a **practical example** of how Spring Boot loads externalized configuration from:

* `.env` file
* Docker environment variable
* and application.properties

so you can see the priority in action?

-yes:
Perfect 💪 Ritik — let’s see a **complete practical example** of how **Spring Boot loads externalized configuration** from multiple sources —
including `.env`, Docker environment variables, and `application.properties`.

---

## ⚙️ **1️⃣ Setup**

Let’s say you’re building an **E-commerce User Service** (`ecom-application`).

You want to configure:

* MongoDB connection URL
* Application name
* Port number

We’ll use three ways to externalize these configs:

1. `application.properties`
2. `.env` file
3. Docker environment variables

---

## 🗂 **2️⃣ application.properties (default config)**

File: `src/main/resources/application.properties`

```properties
spring.application.name=ecom-application
server.port=8081

# Default MongoDB URL (used only if not overridden)
spring.data.mongodb.uri=mongodb://localhost:27017/userdb
```

✅ This is your **base configuration** (works locally).

---

## 📁 **3️⃣ .env File (Environment Variables)**

In your **project root**, create a file named `.env`:

```bash
# .env file
SERVER_PORT=9090
SPRING_DATA_MONGODB_URI=mongodb://env-db:27017/userdb_env
SPRING_APPLICATION_NAME=ecom-application-env
```

Now, when you run Spring Boot, you can tell it to **load this file**.

If using **IntelliJ or VS Code**, you can set:

```
Environment variables from .env
```

or run via terminal:

```bash
export $(cat .env | xargs)
mvn spring-boot:run
```

✅ This will override the properties from `application.properties`.

---

## 🐳 **4️⃣ Docker Environment Variables**

If you run your app inside a Docker container, you can override the properties using environment variables.

Example `docker-compose.yml`:

```yaml
version: "3.8"

services:
  user-service:
    image: ecom-user-service
    ports:
      - "8085:8080"
    environment:
      - SERVER_PORT=8080
      - SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/dockerdb
      - SPRING_APPLICATION_NAME=ecom-application-docker
```

✅ When Spring Boot starts inside this container,
the Docker **environment variables** take precedence over `.env` and `application.properties`.

---

## 🧠 **5️⃣ How Spring Boot Resolves Configuration (Order)**

| Priority | Source                      | Example Value                   |
| -------- | --------------------------- | ------------------------------- |
| **1️⃣**  | Command Line Argument       | `--server.port=7070`            |
| **2️⃣**  | Docker Environment Variable | `SERVER_PORT=8080`              |
| **3️⃣**  | .env File Variable          | `SERVER_PORT=9090`              |
| **4️⃣**  | application.properties      | `server.port=8081`              |
| **5️⃣**  | Default value in code       | `@Value("${server.port:8080}")` |

> The top one wins if duplicates exist.

---

## 👨‍💻 **6️⃣ Verification Example**

Let’s verify what value is loaded at runtime.

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${server.port}")
    private String port;

    @GetMapping("/config")
    public String getConfig() {
        return "App: " + appName + "<br>" +
               "Port: " + port + "<br>" +
               "Mongo URI: " + mongoUri;
    }
}
```

Run your app and open:
👉 `http://localhost:8081/config`

You’ll see the actual loaded values, depending on which source is active.

---

## 🧾 **Summary**

| Environment | Loaded From              | Example Mongo URI                   |
| ----------- | ------------------------ | ----------------------------------- |
| Local Dev   | `application.properties` | `mongodb://localhost:27017/userdb`  |
| Local Test  | `.env`                   | `mongodb://env-db:27017/userdb_env` |
| Docker      | `docker-compose.yml`     | `mongodb://mongo:27017/dockerdb`    |
| CLI         | Command line args        | `mongodb://cli:27017/cli_db`        |

---

Would you like me to extend this example to show **profile-based external configuration** (like `application-dev.properties`, `application-prod.properties`), which is used in microservices for different environments?

Excellent 🔥 Ritik — let’s now go **one level deeper** into how **Spring Boot handles environment-specific configuration** using **Profiles** (`dev`, `test`, `prod`, etc.)

This is a **real-world best practice** — especially for **microservices** like your `ecom-application`.

---

## ⚙️ 1️⃣ What Are Spring Profiles?

Profiles allow you to define **different configurations** for **different environments** such as:

* 🧑‍💻 `application-dev.properties` → for local development
* 🧪 `application-test.properties` → for testing
* 🚀 `application-prod.properties` → for production deployment

When you activate a profile, **Spring Boot automatically loads** the corresponding file **in addition to** the base `application.properties`.

---

## 🗂 2️⃣ Folder Structure Example

```
src/
 └─ main/
     └─ resources/
         ├─ application.properties
         ├─ application-dev.properties
         ├─ application-test.properties
         └─ application-prod.properties
```

---

## 🧩 3️⃣ Base Configuration (common to all)

**application.properties**

```properties
spring.application.name=ecom-application
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always

# Default profile
spring.profiles.active=dev
```

✅ This is the **default** configuration shared across all environments.
Here we set `dev` as the default profile.

---

## 🧑‍💻 4️⃣ Development Configuration

**application-dev.properties**

```properties
server.port=8081
spring.data.mongodb.uri=mongodb://localhost:27017/dev_db
logging.level.org.springframework=DEBUG
```

✅ Used when you’re coding locally.

---

## 🧪 5️⃣ Testing Configuration

**application-test.properties**

```properties
server.port=8082
spring.data.mongodb.uri=mongodb://localhost:27017/test_db
logging.level.org.springframework=INFO
```

✅ Used when running integration or unit tests.

---

## 🚀 6️⃣ Production Configuration

**application-prod.properties**

```properties
server.port=8080
spring.data.mongodb.uri=mongodb+srv://prod_user:password@cluster.mongodb.net/prod_db
logging.level.org.springframework=ERROR
management.endpoints.web.exposure.include=health,info
```

✅ Used when deploying your app to the cloud or server.

---

## 🧠 7️⃣ How to Activate a Profile

You can activate a profile in multiple ways 👇

### 🅰️ In `application.properties`

```properties
spring.profiles.active=prod
```

---

### 🅱️ Command Line

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

or if using `java -jar`:

```bash
java -jar ecom-application.jar --spring.profiles.active=prod
```

---

### 🐳 Docker Environment Variable

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

---

### 🌍 System Environment Variable

```bash
export SPRING_PROFILES_ACTIVE=dev
```

---

## 🔍 8️⃣ Verification Example (same as before)

You can verify which profile is active at runtime:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${server.port}")
    private String port;

    @GetMapping("/profile-info")
    public String getProfileInfo() {
        return """
            <h3>Active Profile:</h3> %s <br>
            <h3>Mongo URI:</h3> %s <br>
            <h3>Port:</h3> %s
            """.formatted(activeProfile, mongoUri, port);
    }
}
```

Then open →
👉 `http://localhost:8081/profile-info`
and you’ll see exactly which configuration Spring loaded.

---

## 🧾 9️⃣ Priority Order (Final)

When Spring Boot starts, it loads properties in this order (highest to lowest):

| Priority | Source                                     | Example                         |
| -------- | ------------------------------------------ | ------------------------------- |
| **1️⃣**  | Command-line args                          | `--spring.profiles.active=prod` |
| **2️⃣**  | OS environment variables                   | `SPRING_PROFILES_ACTIVE=dev`    |
| **3️⃣**  | `.env` file                                | `SPRING_PROFILES_ACTIVE=test`   |
| **4️⃣**  | `application-{profile}.properties`         | `application-prod.properties`   |
| **5️⃣**  | `application.properties`                   | default values                  |
| **6️⃣**  | Code defaults (`@Value("${key:default}")`) | fallback                        |

---

## ✅ 10️⃣ Summary

| Environment | File Loaded                   | Profile | Mongo URI                           |
| ----------- | ----------------------------- | ------- | ----------------------------------- |
| Local       | `application-dev.properties`  | dev     | `mongodb://localhost:27017/dev_db`  |
| Test        | `application-test.properties` | test    | `mongodb://localhost:27017/test_db` |
| Production  | `application-prod.properties` | prod    | `mongodb+srv://.../prod_db`         |

---

Would you like me to show you how to use **`application.yml`** format (instead of `.properties`) with **multiple profiles in a single file** — a cleaner and more professional approach for microservices?

## --------- give example to set the environment variable trow the command line ---
Excellent question 👏 — this is **one of the most important Spring Boot skills** — setting environment variables **from the command line** (for different environments like dev, test, prod).

Let’s go step by step 🔽

---

## 🧩 1️⃣ Why use environment variables?

In Spring Boot, **externalized configuration** lets you override settings from:

* `application.yml` or `application.properties`
* Environment variables
* Command-line arguments
* System properties (`-Dkey=value`)
* `.env` files (if using Docker)

---

## 🧩 2️⃣ Example: Default config in `application.yml`

```yaml
spring:
  application:
    name: ConfifurationDemo
  profiles:
    active: dev

server:
  port: 8080

build:
  id: 1
  version: 1.0.0
  name: default
  type: development
```

---

## 🧩 3️⃣ ✅ Example — Set Environment Variables via Command Line

### 🧠 Option 1: Using **temporary environment variable (PowerShell / Bash)**

### For **Windows PowerShell**

```bash
$env:SPRING_PROFILES_ACTIVE="prod"
$env:SERVER_PORT="9090"
$env:BUILD_ID="500"
mvn spring-boot:run
```

### For **Linux / macOS / Git Bash**

```bash
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=9090
export BUILD_ID=500
mvn spring-boot:run
```

These will override any values from `application.yml`.

---

## 🧩 4️⃣ Option 2: Pass environment variables **inline**

You can pass them **directly** in one line:

### Windows:

```bash
set SPRING_PROFILES_ACTIVE=prod && mvn spring-boot:run
```

### Linux / macOS:

```bash
SPRING_PROFILES_ACTIVE=prod SERVER_PORT=9090 mvn spring-boot:run
```

---

## 🧩 5️⃣ Option 3: Using **System Properties**

You can also use `-D` syntax (Java system property):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod -Dspring-boot.run.arguments="--server.port=9090 --build.id=500"
```

or

```bash
java -jar target/ConfifurationDemo.jar --spring.profiles.active=prod --server.port=9090
```

---

## 🧩 6️⃣ Verify

If your `application-prod.yml` has:

```yaml
build:
  id: ${BUILD_ID:101}
  version: 1.2.3
  name: default-build
  type: production
```

Then when you run:

```bash
export BUILD_ID=999
mvn spring-boot:run
```

👉 Output in logs or `/build-info` endpoint:

```
Build ID: 999
Version: 1.2.3
Name: default-build
Type: production
```

---

## ✅ Summary

| Method                  | Example Command                                   | When to Use              |
| ----------------------- | ------------------------------------------------- | ------------------------ |
| **PowerShell Variable** | `$env:SPRING_PROFILES_ACTIVE="prod"`              | For local dev on Windows |
| **export (Linux/Mac)**  | `export SPRING_PROFILES_ACTIVE=prod`              | For terminals & servers  |
| **Inline**              | `SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run` | For quick one-liners     |
| **System property**     | `java -jar app.jar --spring.profiles.active=prod` | When deploying JARs      |

---

1) ![img_81.png](img_81.png)
2) 
Got it 👍 — this error means **Maven is not installed** or **not added to your system PATH** yet.

Let’s fix this completely 👇 (takes ~5 minutes)

---

## 🧩 Step-by-Step: Install Maven on Windows (PowerShell)

### **1️⃣ Download Maven**

Go to the official Apache Maven site:
👉 [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)

Scroll down to **"Files" → Binary zip archive**, and click:
➡️ **apache-maven-3.9.9-bin.zip**

Direct link (official mirror):
🔗 [https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip](https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip)

---

### **2️⃣ Extract Maven**

After downloading, extract the ZIP file to a simple location like:

```
C:\Program Files\Apache\maven-3.9.9
```

or

```
C:\maven
```

Make sure inside that folder, you see:

```
bin\
boot\
conf\
lib\
README.txt
```

---

### **3️⃣ Set Environment Variables**

1. Open **Start → Search “Environment Variables” → Click “Edit the system environment variables”**
2. In the dialog, click **Environment Variables...**

#### Create MAVEN_HOME:

* Under **System variables**, click **New**
* Variable name: `MAVEN_HOME`
* Variable value: `C:\Program Files\Apache\maven-3.9.9`
  *(or your path)*

#### Add Maven to PATH:

* Under **System variables**, find `Path` → select → **Edit**
* Click **New**
* Add:

  ```
  %MAVEN_HOME%\bin
  ```

Click **OK** on all dialogs to save.

---

### **4️⃣ Verify Installation**

Now **close PowerShell** completely and reopen it (to reload PATH).
Then type:

```powershell
mvn -version
```

✅ If installed correctly, you’ll see:

```
Apache Maven 3.9.9 (...)
Java version: 17.0.x
```

---

### **5️⃣ Run Your Spring Boot App**

Once `mvn` works, navigate to your project folder:

```powershell
cd D:\Personal\CODE\Java-Code\MicroService\ConfigurationDemo
mvn spring-boot:run
```

---
1) ![img_82.png](img_82.png)
2) ![img_83.png](img_83.png)
3) now by running we will get the jar file then we have to run that jar file
4) ![img_84.png](img_84.png)
5) ![img_85.png](img_85.png)
   Perfect — this is a **common Maven packaging issue**, and you’re very close to running your Spring Boot app 🚀

Let’s fix the error:

> `no main manifest attribute, in target/ConfifurationDemo-0.0.1-SNAPSHOT.jar.original`

---

### 🧩 Why This Happens

When you run:

```
java -jar target/ConfifurationDemo-0.0.1-SNAPSHOT.jar.original
```

you’re trying to execute the **intermediate JAR** file that **doesn’t have the Spring Boot manifest** (the main class info).

Spring Boot creates **two JARs** when using Maven:

* `ConfifurationDemo-0.0.1-SNAPSHOT.jar` ✅ (the runnable JAR)
* `ConfifurationDemo-0.0.1-SNAPSHOT.jar.original` ❌ (the plain, non-executable one)

---

### ✅ **Fix: Run the Correct JAR**

Try this command instead:

```powershell
java -jar target/ConfifurationDemo-0.0.1-SNAPSHOT.jar
```

That’s the **Spring Boot executable JAR** containing:

* `META-INF/MANIFEST.MF` with the `Main-Class`
* Embedded Tomcat server
* Your compiled code

---

### ⚙️ If the Correct JAR Isn’t There

If you only see the `.jar.original` file, it means the **Spring Boot repackage step didn’t run**.

Fix it by running:

```powershell
mvn clean package
```

After build success, you’ll see:

```
target/
 ├── ConfifurationDemo-0.0.1-SNAPSHOT.jar   ✅
 └── ConfifurationDemo-0.0.1-SNAPSHOT.jar.original
```

Now execute:

```powershell
java -jar target/ConfifurationDemo-0.0.1-SNAPSHOT.jar
```

---

### 🧠 Optional: Verify the Main Class Exists

You can check what main class Maven put inside the JAR:

```powershell
jar tf target/ConfifurationDemo-0.0.1-SNAPSHOT.jar | findstr Main
```

If it’s missing, your `pom.xml` may not have the `spring-boot-maven-plugin`.

---
![img_86.png](img_86.png)
![img_87.png](img_87.png)

## -------- other and easy way -------
1) ![img_88.png](img_88.png)
2) ![img_89.png](img_89.png)
3) ![img_90.png](img_90.png)
4) ![img_91.png](img_91.png)
5) ![img_92.png](img_92.png)
6) 
Would you like me to show you **what your `pom.xml` should contain** to ensure Maven always builds an executable JAR?

Would you like me to give you a **visual step-by-step screenshot guide** for setting Maven in environment variables? It’ll make it easier to follow.

Would you like me to show how to do the same using a **`.env` file** (for Docker or local development)?


## --------------- setting up .env file for spring boot -----------
1) ![img_93.png](img_93.png)
2) ![img_94.png](img_94.png)
3) ![img_95.png](img_95.png)
4) 