

1) create a project for spring cloud server 
2) ![img.png](img.png)
3) ![img_1.png](img_1.png)
4) make the config server as : @EnableConfigServer
5) ![img_2.png](img_2.png)
6) now add the config client dependecies to all three project
7) ![img_3.png](img_3.png)
8) then start the docker:  docker compose up -d
9) ![img_4.png](img_4.png)
10) then run the product model
11) do the same to others as well
12) while staring the order and product on the postgresSQL then getting the error as product / order database not found so sifted to mongo
13) then working all ok 