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

