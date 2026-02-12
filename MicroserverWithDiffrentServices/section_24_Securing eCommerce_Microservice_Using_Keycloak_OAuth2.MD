## ------------ Section 24: Securing eCommerce Microservices Project with Keycloak + OAuth2 -----
## ----- 312. Securing eCommerce Microservices - What to Expect ----
1) ![img_1082.png](img_1082.png)
2) so we are going to secure the API-Gateway using keycloak 

## ====== 313. Setting up Keycloak Realm and Client for eCommerce Application ==
1) login to keycloak and create a realm for e-commrce app
2) ![img_1083.png](img_1083.png)
3) ![img_1084.png](img_1084.png)
4) then create client for that realm 
5) ![img_1085.png](img_1085.png)
6) ![img_1086.png](img_1086.png)
7) ![img_1087.png](img_1087.png)
8) ![img_1088.png](img_1088.png)
9) ![img_1089.png](img_1089.png)
10) now create a new user for ecomm 
11) ![img_1090.png](img_1090.png)
12) ![img_1091.png](img_1091.png)
13) ![img_1092.png](img_1092.png)

## --------- 314. Implementing PKCE and Testing Flow ----
1) till now client is ready for our ecomm application
2) now we need to configure the our backend
3) so now we need to add the resource server dependecy to apiGateWay service
4) ![img_1093.png](img_1093.png)
```declarative
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```
5) ![img_1094.png](img_1094.png)
6) then we need to add the security config into it
7) ![img_1095.png](img_1095.png)
8) now add the some properties to yml file 
9) ![img_1096.png](img_1096.png)
10) now run all the services 
11) ![img_1097.png](img_1097.png)
12) ![img_1098.png](img_1098.png)
13) ![img_1099.png](img_1099.png)
14) ![img_1100.png](img_1100.png)
15) ![img_1101.png](img_1101.png)
16) ![img_1102.png](img_1102.png)
17) ![img_1103.png](img_1103.png)
18) ![img_1104.png](img_1104.png)
19) ![img_1105.png](img_1105.png)
20) ![img_1106.png](img_1106.png)
21) ![img_1107.png](img_1107.png)
22) ![img_1108.png](img_1108.png)

## ======== 315. Accommodate Keycloak User Id ====
1) ![img_1109.png](img_1109.png)
## ======= 316. Role Based Authorization in Microservices ==

