## --- Section 18: Mastering API Gateway in Spring Boot Microservices ---
## ---- 210. Key Challenges in Microservices Architecture ----
1) ![img_400.png](img_400.png)
2) ![img_401.png](img_401.png)

## ------- 211. What is an API Gateway? A Complete Guide ---
1) ![img_402.png](img_402.png)
2) ![img_403.png](img_403.png)
3) ![img_404.png](img_404.png)

## ---- 212. Key Functions & Benefits of an API Gateway ---
1) ![img_405.png](img_405.png)
2) ![img_406.png](img_406.png)

## ------ 213. Introduction to Spring Cloud Gateway----
1) https://spring.io/projects/spring-cloud-gateway
2) ![img_407.png](img_407.png)
3) ![img_408.png](img_408.png)
4) ![img_409.png](img_409.png)
5) ![img_410.png](img_410.png)

## ------- 214. Step-by-Step Guide: Setting Up API Gateway ---
1) create a spring boot cloud gateway project
2) ![img_411.png](img_411.png)
3) then generate it and copy and paste to directory and remove the zip folder
4) ![img_412.png](img_412.png)
5) then load it as moven project
6) change the application.propratis to application.yml a
7) ![img_413.png](img_413.png)
8) then now we need to define the routing login inside the application.yml file
9) ![img_414.png](img_414.png)

## ------- 
1) first start the docker becuase we are using mysql throw the docker
2) ![img_415.png](img_415.png)
3) ![img_419.png](img_419.png)
3) then start all the services first config-server, then eureka-server, then all the clients(user, product,order) then apiGateway
4) ![img_416.png](img_416.png)
5) ![img_417.png](img_417.png)
6) ![img_418.png](img_418.png)

## ---- 216. Recommended Order to Start Microservices ---
1) so first start the config server then eureka server then all the serives after all the services at the last start the apiGateway services

