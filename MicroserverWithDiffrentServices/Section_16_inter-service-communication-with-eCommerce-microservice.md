## ------------ Section 16: Inter-Service Communication in Spring Boot eCommerce Microservices Project
---
1) now we need to create a eureka server for our ecommerce application for that again create a spring boot application as below
2) ![img_177.png](img_177.png)
3) ![img_178.png](img_178.png)

## ------------ 
1) now we need to add the eureka client dependecy to all client
2) ![img_179.png](img_179.png)
```declarative
<properties>
<java.version>21</java.version>
<spring-cloud.version>2024.0.2</spring-cloud.version>
</properties>

    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
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

```
3) after given the dependecy add propery for eureka in application.yml 
4) ![img_180.png](img_180.png)
5) then start the docker application and run it throw the : docker compose up -d
6) ![img_181.png](img_181.png)
7) then first run the eureka server then configuration server then product service
8) ![img_182.png](img_182.png)
9) ![img_183.png](img_183.png)
10) now product service has successfully connected to eureka server
11) now do same thing for user and order as well
12) then run those two servcies as well user and order after adding code
13) ![img_184.png](img_184.png)
14) now all three services connected to eureka server
15) now we will move these all the services to configServer
16) now move configuration details of eureka from each services properties to configserver/config/service.yml file
17) ![img_185.png](img_185.png)
18) ![img_186.png](img_186.png)
19) ![img_187.png](img_187.png)
20) ![img_188.png](img_188.png)

## ====  188. Challenge: Validate Product via Product Service===
