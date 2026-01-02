## ------- Section 20: Asynchronous Communication in Microservices Using RabbitMQ
 -------
## ---247. Introduction to Asynchronous Communication --
1) ![img_657.png](img_657.png)
2) ![img_658.png](img_658.png)
3) ![img_660.png](img_660.png)
4) ![img_661.png](img_661.png)
## ---- 248. What are Message Queues? ---
1) ![img_662.png](img_662.png)
2) ![img_663.png](img_663.png)
3) ![img_664.png](img_664.png)
4) ![img_665.png](img_665.png)
5) some message queues stystem
6) ![img_666.png](img_666.png)
7) ![img_667.png](img_667.png)
8) ![img_668.png](img_668.png)

## - 249. Messaging Exchanges and its types --
1) ![img_669.png](img_669.png)
2) ![img_670.png](img_670.png)
3) ![img_671.png](img_671.png)
4) ![img_672.png](img_672.png)
5) ![img_673.png](img_673.png)
6) ![img_674.png](img_674.png)
7) ![img_675.png](img_675.png)
8) ![img_676.png](img_676.png)

## ------ 250. What are we going to build? --
1) ![img_677.png](img_677.png)

## -------- 251. Setting up RabbitMQ --------
1) https://www.rabbitmq.com/docs/download
2) ![img_678.png](img_678.png)
```declarative
# latest RabbitMQ 4.x
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management
```
3) and we define it in docker compose file as well
```declarative
  rabbitmq:
    image: rabbitmq:4-management
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

```
4) ![img_679.png](img_679.png)
5) so simplally run the docker compose file and 
6) ![img_680.png](img_680.png)
7) ![img_681.png](img_681.png)
8) ![img_682.png](img_682.png)
9) ![img_683.png](img_683.png)
10) ![img_684.png](img_684.png)

## ---------- 252. Integrating RabbitMQ with Spring Boot ---
1) add the rabitMQ dependeis into that service
2) ![img_685.png](img_685.png)
```declarative
 <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
```
3) ![img_686.png](img_686.png)
4) then we need to add the configuration of rabitMQ into that service application.yml file 
5) ![img_687.png](img_687.png)
6) after that we need to define the configuration definition of RabbitMQ 
7) ![img_688.png](img_688.png)
8) ![img_689.png](img_689.png)
9) ![img_690.png](img_690.png)
10) ![img_691.png](img_691.png)
11) now start everything
12) ![img_692.png](img_692.png)
13) ![img_693.png](img_693.png)
14) ![img_694.png](img_694.png)

## ------- 253. Publishing Messages to RabbitMQ ----
1) now we are going to configure the order service for sending the message to rabbitMQ
2) so basically when order will place then we are going to send the message or when cart will delete we can make it on any action based on our requrenmnt
3) ![img_695.png](img_695.png)
4) ![img_696.png](img_696.png)
5) ![img_697.png](img_697.png)
6) ![img_698.png](img_698.png)
7) ![img_699.png](img_699.png)
8) ![img_700.png](img_700.png)
9) ![img_701.png](img_701.png)
10) ![img_702.png](img_702.png)
11) ![img_703.png](img_703.png)
12) ![img_704.png](img_704.png)
13) ![img_705.png](img_705.png)

## ------------ 254. Creating Notification Service ---
1) ![img_706.png](img_706.png)

## -------- 255. Consuming Messages from RabbitMQ -----
1) now set the configuration of rabbitMQ into notification service
2) ![img_707.png](img_707.png)
3) now make the java code based configuration into notification service as order service
4) now we need create a consumer who will lesson the message
5) ![img_708.png](img_708.png)
6) ![img_709.png](img_709.png)
7) now start all the services 
8) ![img_710.png](img_710.png)
9) all services are running now start creating a order and then see the logs in notification service.
10) ![img_711.png](img_711.png)
11) ![img_712.png](img_712.png)
12) ![img_713.png](img_713.png)
13) ![img_714.png](img_714.png)
14) now we will call the order place then see the logs of notifications
15) ![img_715.png](img_715.png)
16) ![img_716.png](img_716.png)
17) we can see on the rabbiMQ also
18) ![img_717.png](img_717.png)
19) because message already send to notification service. and message has consumed by notifcation service 
20) ![img_718.png](img_718.png)
20) ![img_719.png](img_719.png)
21) ![img_720.png](img_720.png)
22) ![img_721.png](img_721.png)
23) ![img_722.png](img_722.png)
24) ![img_723.png](img_723.png)
25) ![img_724.png](img_724.png)
26) now we will start the notification services then after start it will start the consuming the message
27) ![img_725.png](img_725.png)
28) ![img_726.png](img_726.png)
6) so if even notification service is down it is not going to impact on order service api call becasue it is asyn call but if it is sysn call then it will impact and ones the notifcation service will start then it will start the consuming the messaging from the RabbitMQ as you can see the below

## --------- 256. Setting up DTO for Communication Between Services -----
1) so till now we are sending that data to rabbitMQ in form of key value map hardcoded which is ok but for bettter we can use the DTO as well
2) ![img_727.png](img_727.png)
3) ![img_728.png](img_728.png)
4) ![img_729.png](img_729.png)
5) ![img_730.png](img_730.png)
6) ![img_731.png](img_731.png)
7) ![img_732.png](img_732.png)
8) now if we start the notification service then it will start consuming the message 
9) ![img_733.png](img_733.png)
10) now you can see the logs for better we are going to update the consumer to consume the message proper way
11) ![img_734.png](img_734.png)
12) ![img_735.png](img_735.png)

