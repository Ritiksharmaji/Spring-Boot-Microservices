## --------- 207. Introduction to Distributed Tracing in Microservices -------
1) ![img_351.png](img_351.png)
2) ![img_352.png](img_352.png)
3) ![img_353.png](img_353.png)
4) ![img_354.png](img_354.png)
5) ![img_355.png](img_355.png)
6) ![img_356.png](img_356.png)

## ------------ 208. Distributed Tracing with Zipkin & Spring Boot --------
1) ![img_357.png](img_357.png)
2) https://zipkin.io/
3) explain about zipkin, configuring zipkin then visualize the trees 
4) ![img_358.png](img_358.png)
5) there is various way to run the zipkin you can refer the doc: https://zipkin.io/pages/quickstart.html
6) but we will go with docker one for that we will make change 
7) ![img_359.png](img_359.png)
8) so first define the zipkin service into docker compose file as below 
9) import we have removed the rabitMQ configuration details from docker compose of project directory be remember it shloud not when you are try to work with rabitMQ 
10) ![img_360.png](img_360.png)
    Your `docker-compose.yml` has **multiple syntax + naming issues**. I’ll fix it cleanly and explain **what was wrong**.

---

## ❌ Problems in your file

1. **`rabitmq` spelling is wrong** → should be `rabbitmq`
2. **RabbitMQ image name is wrong**

    * ❌ `rabitmq:3-management`
    * ✅ `rabbitmq:3-management`
3. **MySQL not attached to any network**
4. **Zipkin has empty `networks:`**
5. **Network name says `postgres_network` but you’re using MySQL**
6. **YAML indentation is broken**
7. **Single backend network is enough**

---

## ✅ FIXED & WORKING `docker-compose.yml`

```yaml
version: "3.8"

services:
  mysql:
    image: mysql:8.0
    container_name: mysql_container
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ecomdb
      MYSQL_USER: embarkx
      MYSQL_PASSWORD: embarkx
      TZ: Asia/Kolkata
    ports:
      - "3308:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    command: >
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
    networks:
      - backend

  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq_container
    restart: unless-stopped
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - backend

  zipkin:
    image: openzipkin/zipkin
    container_name: zipkin
    restart: unless-stopped
    ports:
      - "9411:9411"
    networks:
      - backend

volumes:
  mysql_data:

networks:
  backend:
    driver: bridge
```

---

## 🚀 How to run cleanly (IMPORTANT)

Run these commands **once** to avoid conflicts:

```bash
docker compose down -v
docker compose up -d
```

---

## 🌐 Access URLs

| Service         | URL                                              |
| --------------- | ------------------------------------------------ |
| **MySQL**       | `localhost:3308` (NOT browser)                   |
| **RabbitMQ UI** | [http://localhost:15672](http://localhost:15672) |
| **Zipkin UI**   | [http://localhost:9411](http://localhost:9411)   |

**RabbitMQ login**

* Username: `guest`
* Password: `guest`

---

## 🔗 Spring Boot Connection (IMPORTANT)

### MySQL (from Spring Boot)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3308/ecomdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
    username: embarkx
    password: embarkx
```

### RabbitMQ

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### Zipkin

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

---

## ✅ Final Result

✔ MySQL container
✔ RabbitMQ with Web UI
✔ Zipkin tracing UI
✔ All services on one backend network
✔ Spring Boot ready

---

If you want next:

* Add **Spring Boot service** into same compose
* Add **Eureka + Gateway**
* Add **Prometheus + Grafana**

Just say 👍
## ---------- why we are using network as backend or postgre_network ----
Great question 👍
This is **core Docker networking knowledge**, so let’s make it **simple + practical**.

---

## 🧠 Why do we use a Docker network (`backend`, `postgres_network`, etc.)?

In Docker, a **network** is used so that **containers can talk to each other safely and easily**.

> 👉 Without a network, containers behave like isolated machines.

---

## ✅ What happens when you use a custom network?

When containers are on the **same Docker network**:

✔ They can talk to each other using **service names**
✔ Docker provides **built-in DNS**
✔ No need to expose ports internally
✔ Better security & isolation
✔ Cleaner microservice architecture

---

## 📦 Example (your case)

```yaml
networks:
  backend:
    driver: bridge
```

```yaml
services:
  mysql:
    networks:
      - backend

  rabbitmq:
    networks:
      - backend

  zipkin:
    networks:
      - backend
```

Now:

| From          | To       | Hostname   |
| ------------- | -------- | ---------- |
| Order Service | MySQL    | `mysql`    |
| Order Service | RabbitMQ | `rabbitmq` |
| Order Service | Zipkin   | `zipkin`   |

📌 **NOT** `localhost`
📌 **NOT** IP address

---

## ❌ What if you don’t use a network?

* Containers **cannot discover each other by name**
* You must expose ports everywhere
* Hard-coded IPs ❌
* Not microservice-friendly

---

## 🏷 Why names like `backend` or `postgres_network`?

### It’s just a **logical name**

You choose based on **purpose**:

| Network Name        | Use case                             |
| ------------------- | ------------------------------------ |
| `backend`           | Databases, queues, internal services |
| `frontend`          | UI, gateway, public APIs             |
| `postgres_network`  | Only PostgreSQL-related containers   |
| `microservices_net` | Entire microservice system           |

👉 **Name does NOT affect behavior**, only readability.

---

## 🔐 Security Benefit

Containers **on the same network** can talk
Containers **outside the network** ❌ cannot

Example:

* MySQL on `backend` → ❌ not reachable from outside containers
* Only exposed via port `3308` if you want

---

## 🔥 Real Microservice Setup (Best Practice)

```yaml
networks:
  frontend:
  backend:
```

* **Frontend network** → API Gateway
* **Backend network** → DB, MQ, Zipkin, Services

---

## 🧩 TL;DR

✔ Networks allow **container-to-container communication**
✔ Service name = hostname
✔ No hard-coded IPs
✔ Clean microservice architecture
✔ `backend` / `postgres_network` is just a **logical name**

---

If you want, next I can explain:

* Difference between `bridge`, `host`, `overlay`
* How Spring Boot resolves `mysql` → IP
* Why `localhost` fails inside Docker

Just tell me 🚀

1) ![img_361.png](img_361.png)
2) ![img_362.png](img_362.png)
3) ![img_363.png](img_363.png)
4) as we can see the  page thre is nothing because our application is not sending anydata to it for that we need to add Zipkin  dependecies to all three services
5) ![img_364.png](img_364.png)
5) ![img_365.png](img_365.png)
```declarative
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-micrometer-tracing-brave</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-zipkin</artifactId>
    </dependency>
    <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-tracing-bridge-brave</artifactId>
    </dependency>
```
6) getting error so change the version
7) ![img_366.png](img_366.png)
8) ![img_367.png](img_367.png)
```declarative
<dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-tracing-bridge-brave</artifactId>
    </dependency>
    <dependency>
      <groupId>io.zipkin.reporter2</groupId>
      <artifactId>zipkin-reporter-brave</artifactId>
    </dependency>
```
9) ![img_368.png](img_368.png) 
10) do for others as well
11) now after that we need to add a properties of zipkin into those three services properties file that propery called as sampling property of zipkin
12) ![img_369.png](img_369.png)
13) do for those remaining two services as well and then restart the those threee services and go back to that zipkin url and make refresh or run query then you will see
14) ![img_370.png](img_370.png)
15) by default  the traces throw out the requrest(by restclient request) is not propally align as you can verified by calling cart request which will call first user identifed then product avaialbleity then cart request but in the UI it will not show like that as in below iamge
16) ![img_371.png](img_371.png)
17) ![img_372.png](img_372.png)
18) ![img_373.png](img_373.png)
19) now we need to create a function which will tracking the intersecter 
20) ![img_374.png](img_374.png)
21) then again make request: 
22) ![img_375.png](img_375.png)
23) then refres the page 
24) ![img_376.png](img_376.png)
25) ![img_377.png](img_377.png)

## ----------- 209. Distributed Tracing with Grafana for Microservices ------
1) to tracing distrubeted zipkin and garafana must be run so we have three docker compose file one is on the project directory and two others are into the additional directory
2) so run the garana and zipkin 
3) ![img_378.png](img_378.png)
4) ![img_379.png](img_379.png)
5) even those we can combine more than two and two docker compose file into one 
6) now we are going to add one datasource as zipkin
7) ![img_380.png](img_380.png)
8) ![img_381.png](img_381.png)
9) enter url where zipkin running 
10) ![img_382.png](img_382.png)
11) ![img_383.png](img_383.png) then click on save and test 
12) ![img_384.png](img_384.png) getting error so change the localhost with service name as : http://zipkin:9411/
13) ![img_385.png](img_385.png)
14)![img_386.png](img_386.png) 
14) now getting error as : Get "http://zipkin:9411/api/v2/services": dial tcp: lookup zipkin on 127.0.0.11:53: no such host
15) beacuse zipkin is not able to communicate with grafana beacuse of network they are not on same network call  beacuse one docker compose is usring backend as netwrok and second one is using loki as netwok so move the zipkin config dicker code into second one docker compose file 
16) ![img_387.png](img_387.png)
16) then close the docker on running the both directory and then restart them
17) ![img_388.png](img_388.png)
    This error is **100% Docker basics**, nothing wrong with Zipkin itself 👍
    Let’s fix it cleanly.

---

## ❌ What the error means

```
Conflict. The container name "/zipkin" is already in use
```

➡ A **Zipkin container already exists** on your system
➡ Docker **does not allow two containers with the same name**

Even if it’s **stopped**, the name is still reserved.

---

## ✅ OPTION 1 (FASTEST): Reuse the existing Zipkin container

If Zipkin already exists and you just want it running:

```bash
docker start zipkin
```

Then re-run compose **without Zipkin**, or remove Zipkin from compose temporarily.

---

## ✅ OPTION 2 (RECOMMENDED): Remove old Zipkin container

### 1️⃣ Stop and remove the existing container

```bash
docker rm -f zipkin
```

### 2️⃣ Now start compose

```bash
docker compose up -d
```

---

## ✅ OPTION 3: Rename Zipkin in `docker-compose.yml`

If you want **multiple Zipkin stacks**:

```yaml
zipkin:
  image: openzipkin/zipkin
  container_name: zipkin_eval
```

Then:

```bash
docker compose up -d
```

---

## 🔍 How to see what exists (useful)

```bash
docker ps -a | findstr zipkin
```

---

## 🧠 Best Practice (Important for you)

### ❌ Avoid `container_name` in compose for microservices

Docker Compose already namespaces containers:

```
evaluate-prometheus_zipkin_1
```

✔ No conflict
✔ Easier scaling
✔ Multiple environments possible

**Recommended:** remove `container_name: zipkin`

---

## 🌐 Verify Zipkin

After successful start:

```
http://localhost:9411
```

---

## 🔥 TL;DR

| Fix           | Command                 |
| ------------- | ----------------------- |
| Reuse         | `docker start zipkin`   |
| Delete        | `docker rm -f zipkin`   |
| Rename        | Change `container_name` |
| Best practice | Remove `container_name` |

---

If you want, I can also:

* Clean your full docker setup
* Add Prometheus + Loki + Grafana correctly
* Wire Zipkin with Spring Boot tracing

Just say 👍
1) ![img_389.png](img_389.png)
2) ![img_390.png](img_390.png)
3) ![img_391.png](img_391.png)
4) ![img_392.png](img_392.png)
5) now we can trace the id ![img_393.png](img_393.png)so 
6) ![img_394.png](img_394.png)
6) ![img_395.png](img_395.png)
7) ![img_396.png](img_396.png)
8) ![img_397.png](img_397.png)
9) ![img_398.png](img_398.png)
10) ![img_399.png](img_399.png)
11) 