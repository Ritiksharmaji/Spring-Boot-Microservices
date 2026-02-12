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
1) ![img_1110.png](img_1110.png)
2) then we need to write a conveter into APIGateway securityConfig  which help to extract the user role from JWT token and give to us
3) ![img_1111.png](img_1111.png)
4) then restart apiGateway application
5) we will create three roles 
6) ![img_1112.png](img_1112.png)
7) ![img_1113.png](img_1113.png)
8) ![img_1114.png](img_1114.png)
9) then assign the role to user 
10) ![img_1115.png](img_1115.png)
11) ![img_1116.png](img_1116.png)
12) ![img_1117.png](img_1117.png)
11) now again generate the token and validate the role 
12) ![img_1118.png](img_1118.png)
13) ![img_1119.png](img_1119.png)
14) the above roles was on Realm Roles now we can create client level as well
15) ![img_1120.png](img_1120.png)
16) ![img_1121.png](img_1121.png)
17) ![img_1122.png](img_1122.png)
18) ![img_1123.png](img_1123.png)
19) now again regenerate the token and check it 
20) ![img_1125.png](img_1125.png)
21) ![img_1126.png](img_1126.png)
22) ![img_1127.png](img_1127.png)
23) ![img_1128.png](img_1128.png)
24) ![img_1129.png](img_1129.png)
25) below is Realm Roles 
20) ![img_1124.png](img_1124.png)
21)as of now we are allowing all the apis to access any role now we are going to restric it based on role 
22) ![img_1130.png](img_1130.png)
23) ![img_1131.png](img_1131.png)
24) now we will create three user to assing those role 
25) ![img_1132.png](img_1132.png)
26) ![img_1133.png](img_1133.png)
27) ![img_1134.png](img_1134.png)
28) ![img_1135.png](img_1135.png)
29) ![img_1136.png](img_1136.png)
30) ![img_1137.png](img_1137.png)
31) ![img_1138.png](img_1138.png)
32) ![img_1139.png](img_1139.png)
33) ![img_1140.png](img_1140.png)
34) now we will map the role to them
35) ![img_1141.png](img_1141.png)
36) ![img_1142.png](img_1142.png)
37) ![img_1143.png](img_1143.png)
38) now we wll generate token based on each type of user 
39) for user token 
40) ![img_1144.png](img_1144.png)
41) ![img_1145.png](img_1145.png)
42) ![img_1146.png](img_1146.png)
43) ![img_1147.png](img_1147.png)
44) ![img_1148.png](img_1148.png)
44) now we will call the product api using user type 
45) ![img_1149.png](img_1149.png)
46) ![img_1150.png](img_1150.png)
47) now we will call the product api using product type user
48) ![img_1151.png](img_1151.png)
49) ![img_1152.png](img_1152.png)
50) ![img_1153.png](img_1153.png)
51) ![img_1154.png](img_1154.png)
52) ![img_1155.png](img_1155.png)
53) now we will call the order service with order type of user 
54) ![img_1156.png](img_1156.png)
55) ![img_1157.png](img_1157.png)
56) ![img_1158.png](img_1158.png)
57) ![img_1159.png](img_1159.png)
58) 
