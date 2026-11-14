# === 153. Challenge: Setup Config Server for an eCommerce Microservices Project =====
# ---- so we are going to create a config server for this project ---
1) we are going to make the file based configuration for this project so all the configuration related to this project will stored locally 
2) so for that we need to setup spring cloud config server which is itselph a application.
3) for that create new spring boot application
1) create a project for spring cloud config server 
2) ![img.png](img.png)
3) ![img_1.png](img_1.png)
4) make the config server as : @EnableConfigServer
5) ![img_5.png](img_5.png)
6) after that modify the application.propeties to application.yml and add the code for storing and getting configuration from local path
7) ![img_6.png](img_6.png)
8) then start the application (Config-server)
5) ![img_2.png](img_2.png)

# === 154. Challenge: Updating Configuration Across All Microservices ===
1) now paste all the application.yml file with there all code of three application(usermodel, productmodel and ordermodel into confi/ folder of SpringCloudConfigServer).
2) and then create refresh new again application.yml file those three projects
3) 
6) now add the config client dependecies to all three project
7) ![img_3.png](img_3.png)
8) ![img_7.png](img_7.png)
9) to all those three services
10) 
8) then start the docker:  docker compose up -d
9) ![img_4.png](img_4.png)
10) then run the product model
11) do the same to others as well
12) while staring the order and product on the postgresSQL then getting the error as product / order database not found so sifted to mongo
13) then working all ok 
14) ![img_8.png](img_8.png)
15) ![img_9.png](img_9.png)
16) ![img_10.png](img_10.png)
17) ![img_11.png](img_11.png)
18) so now all the services working fine ...

## ----------- 155. Challenge: Externalizing Configuration Using .env Files or Environment Variables --
1) so now we are going to Externalizing all the configuration as .env file and we may can use the git as well
2) so now we have to create all .env files for all the projects

Here are the **.env files** for both **Order Service** and **Product Service**, based on your Spring Cloud Config YAML.

---

# ✅ **Order Service – `.env` file**

```
SERVER_PORT=8083

SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/order
SPRING_DATA_MONGODB_DATABASE=order
```

✔ Matches your YAML
✔ Works with Spring Boot if you use `@ConfigurationProperties` OR load `.env` using `spring-dotenv` or Docker compose.

---

# ✅ **Product Service – `.env` file**

You asked to include **only MongoDB properties**:

```
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/product
SPRING_DATA_MONGODB_DATABASE=product
```

---

# ⚠️ **Important Notes**

* Spring Boot **does not read `.env` files by default**.
* Use one of the following:

    1. **`spring-boot-dotenv`** dependency

       ```
       <dependency>
           <groupId>me.paulschwarz</groupId>
           <artifactId>spring-dotenv</artifactId>
           <version>3.0.0</version>
       </dependency>
       ```
    2. OR use Docker Compose `env_file:`
    3. OR manually map environment variables using your OS or deployment platform.

---

If you want, I can generate:

✅ Docker Compose for both services
✅ Spring Cloud Config Server `.yml`
✅ Kubernetes secrets/config maps
✅ Complete microservice folder structure

Just tell me!
1) not working that depedencies so adding .env
2) ![img_12.png](img_12.png)
3) ![img_13.png](img_13.png)

## ---------- 156. Challenge: Dynamically Refresh Configurations with Spring Cloud Bus (No Restart)---
1) now we need to add the actucter to all four project
```declarative
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
```
2) then add dependeies for rabitMQ into all
```declarative
 <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```
3) one important thing the above dependeies comming from spring cloud so make should you should have version of spring cloud in pom.xml
```declarative
<spring-cloud.version>2025.0.0</spring-cloud.version>
```
as like:
```declarative
<properties>
		<java.version>21</java.version>
		<spring-cloud.version>2025.0.0</spring-cloud.version>
</properties>
```
4) and also you must include spring cloud as below
```declarative
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

```
5) then we need to add some additional configuration to all the application.properties (.yml )file. to connect with rabirMQ
6) ![img_14.png](img_14.png)
7) copy the command and paste to order-service.yml
8) so copy and paster configuration code of actular and rabitMQ to all four( order-service.yml, user-service.yml, product-service.yml and application.yml of spring coudConfigServer).
9) ![img_15.png](img_15.png)
10) so in to get the config we are using
```declarative
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config

```
so after making any change in .ynl file and you want to read it without restart the application then we need to make little bit change we need to use the external file not the classpath
so configserver will read it so we need to give the file path
11) ![img_16.png](img_16.png)
12) now we need to rin the RabirMQ 
13) ![img_17.png](img_17.png)
14) to run the :
    You already have a RabbitMQ container running with the **name `rabbitmq`**, so Docker will not allow starting another container with the same name.

Here are the ways to fix it:

---

# ✅ **Option 1: Stop and Remove the Existing RabbitMQ Container**

Run these commands:

```bash
docker stop rabbitmq
docker rm rabbitmq
```

Then run your new container:

```bash
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management
```

---

# ✅ **Option 2: Start the Existing Container (if it is stopped)**

Check if it's stopped:

```bash
docker ps -a
```

If it’s stopped, start it:

```bash
docker start rabbitmq
```

---

# ✅ **Option 3: Run New RabbitMQ Container With a Different Name**

If you want to keep the existing container:

```bash
docker run -it --rm --name rabbitmq2 -p 5673:5672 -p 15673:15672 rabbitmq:4-management
```

But usually **Option 1** is best.

---

# ⚠️ Why This Error Happens?

Docker does not allow two containers with the same name:

```
Conflict. The container name "/rabbitmq" is already in use
```

You must stop, remove, or rename the old one before creating a new one.

---

If you want, I can also give you:

✅ Docker Compose for RabbitMQ
✅ Commands to check logs
✅ How to integrate RabbitMQ with Spring Boot microservices

Just tell me!
13) ![img_18.png](img_18.png)
14) ![img_19.png](img_19.png)
15) ![img_20.png](img_20.png)
16) started those three project
17) ![img_21.png](img_21.png)
18) ![img_22.png](img_22.png)
19) we can add the configuration of rabitMQ into docker-compose.yml file but for now all application working so we will not but if in later come any issuse then we will add as below image
20) ![img_23.png](img_23.png)
21) so aout springCloudConfigServer is running on 8888, user on:8081, product on: 8082, and order on:8083 
22) so now we will run the http://localhost:8081/actuator/busrefresh throw the post man then user service will refresh
23) ![img_24.png](img_24.png)
24) ![img_25.png](img_25.png)
25) ![img_26.png](img_26.png)
26) now we will change something then call the http://localhost:8081/actuator/busrefresh so
27) ![img_27.png](img_27.png)
28) without restart the application run the http://localhost:8081/actuator/busrefresh then see the logs
29) ![img_28.png](img_28.png)
30) now are creating a controller to send the respose if any change happend 
31) we have create one same controller to two service(product and order)
32) ![img_29.png](img_29.png)
33) and we are getting value from oder-service.yml and product-service.yml 
34) ![img_30.png](img_30.png)
35) ![img_31.png](img_31.png)
36) now after running all service 
37) ![img_32.png](img_32.png)
38) ![img_33.png](img_33.png)
39) now change the valude as 
40) ![img_34.png](img_34.png)
41) then run http://localhost:8083/actuator/busrefresh on postman
42) ![img_35.png](img_35.png)
43) now we have run the servcie 8083 on which order running but all the services will run with respect to there change it will show keys refreshed
44) ![img_36.png](img_36.png)
44) ![img_37.png](img_37.png)
45) ![img_38.png](img_38.png)
46) ![img_39.png](img_39.png)
47) call the browser request:
48) ![img_40.png](img_40.png)
49) ![img_41.png](img_41.png)

## ---------- 157. Best Practices for Configuration Management in Production Microservices --------
1) to be up-to date for best practies go to website offical spring.io(https://spring.io/) => then
2) https://spring.io/projects/spring-cloud : ![img_42.png](img_42.png)
3) ![img_43.png](img_43.png)
4) https://docs.spring.io/spring-cloud-config/reference/: ![img_44.png](img_44.png)
5) ![img_45.png](img_45.png)

Here are **Best Practices for Configuration Management in Production Microservices**, written clearly and concisely so you can apply them in your Spring Cloud Config + Microservices project.

---

# ✅ **Best Practices for Configuration Management in Production Microservices**

Configuration management is crucial in microservices because each service is independently deployable and runs in different environments (dev, test, stage, prod). The goal is to ensure **consistency, security, and reliability** across all environments.

---

# ⭐ **1. Externalize All Configurations**

Do **NOT hardcode** configs inside your `.jar` files.

### Use:

✔ Spring Cloud Config Server
✔ Kubernetes ConfigMaps / Secrets
✔ HashiCorp Vault
✔ AWS SSM / Parameter Store
✔ Azure Key Vault

---

# ⭐ **2. Separate Environment-Specific Configs**

Use separate configuration files:

```
application-dev.yml
application-test.yml
application-prod.yml
```

For Spring Cloud Config:

```
product-service-dev.yml
product-service-prod.yml
```

This avoids accidental use of dev settings in production.

---

# ⭐ **3. Version Control Your Configuration**

Store configs in **Git** (or a repo) so you get:

✔ Change history
✔ Rollbacks
✔ Code reviews
✔ CI/CD integration

Even if using *native file system* today, production should always use **Git backend**.

---

# ⭐ **4. Secure Sensitive Configuration**

**Never store secrets in Git**.

Use:
✔ Spring Cloud Vault
✔ AWS Secrets Manager
✔ GCP Secret Manager
✔ Kubernetes Secrets

Sensitive values:

* DB passwords
* JWT secret keys
* OAuth client secrets
* RabbitMQ passwords

---

# ⭐ **5. Use Encryption for Sensitive Values**

Spring Cloud Config supports **symmetric/asymmetric** encryption:

```
encrypt.key=your-secret-key
```

Encrypted property example:

```
spring.datasource.password: "{cipher}AHDKJH213kdahdj1928..."
```

---

# ⭐ **6. Enable Dynamic Refresh (Real-Time Config Updates)**

Enable hot reload without restarting microservices:

In microservices:

```java
@RefreshScope
public class MyBean { ... }
```

Trigger refresh:

```
POST /actuator/refresh
```

If using **Spring Cloud Bus**, config changes propagate automatically.

---

# ⭐ **7. Validate Configurations During Startup**

Fail fast if configuration is missing.

In `application.yml`:

```yaml
spring:
  main:
    fail-fast: true
```

This prevents services with partial configs from entering production.

---

# ⭐ **8. Use Hierarchical Structure**

Organize configs logically:

```
microservice/
   database/
   logging/
   cache/
   messaging/
```

---

# ⭐ **9. Avoid Duplicated Config in Each Service**

Use shared configuration:

```
shared-config.yml
product-service.yml
order-service.yml
payment-service.yml
```

Shared configs include:

* Logging rules
* Kafka / RabbitMQ endpoints
* Common retry policies

---

# ⭐ **10. Manage Config Through CI/CD Pipelines**

Never manually edit config on servers.

Automate:
✔ Update → Commit → Pipeline → Config server reload
✔ Validate configs with scripts

---

# ⭐ **11. Use Feature Flags**

Use config to turn features ON/OFF at runtime.

Example:

```yaml
feature:
  enableDiscount: true
```

---

# ⭐ **12. Use Immutable Infrastructure**

If config changes:
✔ Deploy new version
✔ Containers remain stateless

This avoids "configuration drift".

---

# ⭐ **13. Monitor Configuration Changes**

Use logs + audit:

✔ Who changed config
✔ What changed
✔ When
✔ Which service reloaded

Config server Git history is your audit log.

---

# ⭐ **14. Backup and Disaster Recovery**

Always keep:

* Git repository backup
* Encrypted secrets backup
* Version snapshot of configs

---

# ⭐ **15. Enforce Naming Standards**

Example:

```
<service-name>-<environment>.yml
```

✔ `product-service-dev.yml`
✔ `order-service-prod.yml`
✔ `payment-service-uat.yml`

This reduces confusion during deployment.

---

# ⭐ **16. Validate Config Schema (Optional but recommended)**

Use tools like:
✔ JSON Schema
✔ OpenAPI Validator
✔ Custom Validators

---

# ⭐ Summary Table

| Best Practice         | Why Important                   |
| --------------------- | ------------------------------- |
| Externalize configs   | Avoid hardcoding / easy updates |
| Separate environments | Safety and correctness          |
| Use Git               | Versioning and rollback         |
| Secure secrets        | Prevent breaches                |
| Hot reload            | No server downtime              |
| Shared configs        | Reduce duplication              |
| CI/CD management      | Zero manual errors              |
| Feature flags         | Controlled rollouts             |
| Monitoring            | Auditable changes               |
| Backups               | Disaster recovery               |

---

# If you want, I can also provide:

✅ Complete Spring Cloud Config Architecture Diagram
✅ Best folder structure for configs
✅ Dev/Test/Prod config templates
✅ Secrets handling with Vault
✅ CI/CD automation example
Just tell me!
