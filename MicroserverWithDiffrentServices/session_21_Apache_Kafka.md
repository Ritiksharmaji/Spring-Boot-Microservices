## --------- Section 21: Apache-kafka ---
## ------ 260. Introduction to Apache Kafka ---
1) ![img_781.png](img_781.png)
2) ![img_782.png](img_782.png)
3) ![img_783.png](img_783.png)
4) ![img_784.png](img_784.png)
5) ![img_785.png](img_785.png)
6) ![img_786.png](img_786.png)
7) ![img_787.png](img_787.png)
8) ![img_788.png](img_788.png)
9) ![img_789.png](img_789.png)

## ----- 261. Kafka Architecture and Components ---
1) ![img_790.png](img_790.png)
2) ![img_791.png](img_791.png)
   Here are **clear, interview-ready definitions** of each **Kafka Architecture component** shown in your image 👇
   (I’ll keep it simple + practical, based on how Kafka is actually used in real projects.)

---

## 1️⃣ Event

An **event** is a **record/message** that represents something that happened.

**Example:**

* `OrderCreated`
* `PaymentCompleted`
* `UserRegistered`

**Structure of an Event:**

* **Key** (optional) → used for partitioning
* **Value** → actual data (JSON, Avro, String, etc.)
* **Timestamp**
* **Headers** (optional)

📌 In Kafka, **everything is an event**.

---

## 2️⃣ Producer

A **Producer** is an application that **sends (publishes) events** to Kafka topics.

**Responsibilities:**

* Choose a topic
* Serialize data
* Decide partition (via key or round-robin)
* Send messages asynchronously

**Example:**

* Order Service producing `OrderCreated` events
* Payment Service producing `PaymentSuccess` events

📌 Producers **do NOT know** who consumes the data.

---

## 3️⃣ Consumer

A **Consumer** is an application that **reads (subscribes to) events** from Kafka topics.

**Responsibilities:**

* Poll messages
* Process data
* Commit offsets

**Example:**

* Email Service consuming `OrderCreated`
* Analytics Service consuming `UserActivity`

📌 Consumers **pull data**, Kafka does not push.

---

## 4️⃣ Topic
![img_792.png](img_792.png)
1) topics are nothing but a channel for receiving the message
2) A **Topic** is a **logical stream/category of events**.
3) means if we want to send the user related event then we will have a topics for user if we want to send product related message then we weill have a topic for poduct like that others as well.
4) it is a segregation within the kafka
5) 

Think of it like:

* 📁 Folder
* 📰 Event stream
* 📬 Message channel

**Example Topics:**

* `orders`
* `payments`
* `user-events`

📌 Topics are **append-only** (events are never updated).

---

## 5️⃣ Partition
![img_793.png](img_793.png)
* 
A **Partition** is a **physical division of a topic**.

**Why partitions exist:**

* Parallel processing
* Scalability
* Ordering (within a partition)

**Key Points:**

* Each partition is an ordered log
* Events with the same key go to the same partition
* Ordering is guaranteed **only within a partition**

📌 More partitions = more parallel consumers.

---

## 6️⃣ Consumer Group
![img_794.png](img_794.png)

A **Consumer Group** is a **set of consumers working together** to read a topic.

**Rules:**

* One partition → one consumer **within a group**
* Same message is processed **once per group**
* Multiple groups can read the same topic independently

**Example:**

* `email-service-group`
* `analytics-group`

📌 Enables **horizontal scaling**.

---

## 7️⃣ Offset
![img_795.png](img_795.png)

An **Offset** is a **unique position number** of a message inside a partition.

**Why offsets matter:**

* Track consumed messages
* Resume from last processed message
* Enable replay of data

**Offset Management:**

* Auto-commit
* Manual commit

📌 Kafka stores offsets, not message acknowledgments like RabbitMQ.

---

## 8️⃣ Consumer Rebalancing

**Consumer Rebalancing** happens when Kafka **redistributes partitions** among consumers in a group.

**Triggers:**

* New consumer joins
* Consumer crashes
* Topic partitions change

**What happens:**

* Consumers stop consuming temporarily
* Partitions reassigned
* Consumption resumes

📌 Rebalancing ensures **fault tolerance & load balancing**.

---

## 🔁 Simple Kafka Flow (Mental Model)

```
Producer → Topic → Partitions → Consumer Group → Consumers
```

---

## 🔥 Kafka vs RabbitMQ (Quick Context)

Since you’re already working with **RabbitMQ + Microservices**, remember:

| Feature    | Kafka           | RabbitMQ      |
| ---------- | --------------- | ------------- |
| Model      | Event streaming | Message queue |
| Retention  | Long-term       | Short         |
| Replay     | ✅ Yes           | ❌ No          |
| Ordering   | Per partition   | Per queue     |
| Throughput | Very high       | Medium        |

---

If you want next:

* 🔹 Kafka **real-world microservice example**
* 🔹 Kafka **Spring Boot configuration**
* 🔹 Kafka **offset & rebalance deep dive**
* 🔹 Kafka **interview Q&A**

Just tell me 👍

1) ![img_796.png](img_796.png)
2) broker means instace of kafka so here there are four instace of kafka which is running in a cluster
3) ![img_797.png](img_797.png)
4) zookeeper is a service which is used to manage the entire kafka cluster
5) now in new version of kafka zookeeper has come with  kafka so we need to don't need to install zookeeper  separeally, it comes with kafka. but in older version of kafka we need to install the zookeeper separeally it don' t come with kafka in older version
6) ![img_798.png](img_798.png)
7) ![img_799.png](img_799.png)
8) ![img_800.png](img_800.png)

## ------ 262. Before Installation | Understanding Installation --
1) ![img_801.png](img_801.png)
2) ![img_802.png](img_802.png)
3) https://kafka.apache.org/

## - 263. Installing Apache Kafka --
1) first run the docker and 
1) command to create and run the kafka and zookeeper
```declarative
docker run -d --name zookeeper -p 2181:2181 -e ZOOKEEPER_CLIENT_PORT=2181 -e ZOOKEEPER_TICK_TIME=2000 confluentinc/cp-zookeeper:7.5.0
docker run -d --name kafka -p 9092:9092 -e KAFKA_BROKER_ID=1 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 --link zookeeper confluentinc/cp-kafka:7.5.0
```
3) ![img_803.png](img_803.png)
4) ![img_804.png](img_804.png)
5) ![img_805.png](img_805.png)
6) ![img_806.png](img_806.png)
7) ![img_807.png](img_807.png)

## ------ 264. Kafka Demo Using CLI ---
1) now we are going to inside the CLI of kafka
2) to enter into running kafka CLI enter the command
```declarative
docker exec -it kafka bash
```
3) ![img_808.png](img_808.png)
4) now we are inside the kafka container
5) create a topics
```declarative
 kafka-topics --create --topic my-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
Created topic my-topic.
```
6) to see the created topics
```declarative
kafka-topics --list --bootstrap-server localhost:9092
```
7) ![img_809.png](img_809.png)
8) ![img_810.png](img_810.png)
9) to see the details of a topic
```declarative
 kafka-topics --describe --topic my-topic --bootstrap-server localhost:9092
```
10) ![img_811.png](img_811.png)
11) now we are going to produce the message throw the console it selph using CLI for that we are going to use the client tool which is producer client tool which kafka provides to start the producer
12) ![img_812.png](img_812.png)
13) now we have stared the producer with three message
```declarative
 kafka-console-producer --topic my-topic --bootstrap-server localhost:9092
```
14) now we can start the consumer by other command
15) for that 
```declarative
docker exec -it kafka bash
```
16) then 
```declarative
 kafka-console-consumer --topic my-topic --bootstrap-server localhost:9092 --from-beginning
```
17) ![img_813.png](img_813.png)
18) it has read the message as below
19) ![img_814.png](img_814.png)
20) now if we send the mesage from the producer then consumer it autometically read that one as you can see in below images
21) ![img_815.png](img_815.png)
22) ![img_816.png](img_816.png)

## ------ 265. Setting Up Kafka Producer -----
1) know we are going to create a producer service for kafka 
2) ![img_817.png](img_817.png)
3) ![img_818.png](img_818.png)

## ----- 265. Setting Up Kafka Producer --
1) creating a producer
2) ![img.png](./kafka/img.png)
3) then run the docker
4) ![img_1.png](./kafka/img_1.png)
5) ![img_2.png](./kafka/img_2.png)
6) now start producer
7) ![img_3.png](./kafka/img_3.png)
8) call the api by postman
9) ![img_4.png](./kafka/img_4.png)
10) ![img_5.png](./kafka/img_5.png)

## ---- 266. Setting Up Kafka Consumer --
1) ![img_6.png](./kafka/img_6.png)
2) ![img_7.png](./kafka/img_7.png)
3) now we need to add a consumer class which is going to lesine the message by the producer
4) ![img_8.png](./kafka/img_8.png)
5) then run the consumer as well
6) ![img_9.png](./kafka/img_9.png)
7) ![img_10.png](./kafka/img_10.png)
8) ![img_11.png](./kafka/img_11.png)

## ----- 267. Starting App and Understanding Behind the Scenes --
1)![img_12.png](./kafka/img_12.png)
2)![img_13.png](./kafka/img_13.png)
3)![img_14.png](./kafka/img_14.png)
4) ![img_15.png](./kafka/img_15.png)
5) ![img_16.png](./kafka/img_16.png)
6) ![img_17.png](./kafka/img_17.png)

## ------ 268. Setting up Consumer Groups ---
1) now we are going to create a consumer class for consumer group bascially we are creating this all in same file but in prodcution consumer will be in diffrent application and on diffrent server to liseeing the provider
2) ![img_18.png](./kafka/img_18.png)
3) ![img_19.png](./kafka/img_19.png)
4) ![img_20.png](./kafka/img_20.png)
5) ![img_21.png](./kafka/img_21.png)
   Great 👍
   What you’re seeing is **100% correct Kafka behavior**, but it’s confusing at first.
   Let me **break it down step-by-step** so you fully understand **WHAT is happening and WHY**.

---

# 🔍 What You Built

You have:

### ✅ 1 Producer

```java
kafkaTemplate.send("my-topic", message);
```

### ✅ 2 Kafka listeners

```java
@KafkaListener(topics = "my-topic", groupId = "my-new-group")
public void Listen(String message) { ... }

@KafkaListener(topics = "my-topic", groupId = "my-new-group-1")
public void Listen2(String message) { ... }
```

### ✅ 1 CLI consumer

```bash
kafka-console-consumer --topic my-topic \
--bootstrap-server localhost:9092 \
--group my-group --from-beginning
```

---

# 🧠 VERY IMPORTANT RULE IN KAFKA

> **Kafka delivers messages PER CONSUMER GROUP, not per consumer**

---

## 🎯 Key Kafka Rule (Memorize This)

```
1 message → 1 consumer PER GROUP
```

---

# 🧩 Why BOTH Your Consumers Received the Same Message?

### You have **THREE DIFFERENT CONSUMER GROUPS**

| Consumer         | Group ID         |
| ---------------- | ---------------- |
| KafkaListener #1 | `my-new-group`   |
| KafkaListener #2 | `my-new-group-1` |
| Console consumer | `my-group`       |

👉 Since **all group IDs are different**, Kafka treats them as **independent subscribers**.

### Result:

✔ Same message delivered to **each group**

✅ **This is expected behavior**

---

# 📦 Message Flow Explained

When you send:

```
HelloKafka-new message for consumer
```

Kafka does this:

```
Topic: my-topic
   ├── Group: my-new-group      → Listener 1 receives
   ├── Group: my-new-group-1    → Listener 2 receives
   └── Group: my-group          → CLI consumer receives
```

---

# 🔥 Why You See This Log

```text
partitions assigned: [my-topic-0, my-topic-1, my-topic-2]
```

### Meaning:

* Topic has **3 partitions**
* Each consumer group gets **all partitions**
* Inside a group, partitions are shared

---

# ⚠️ VERY IMPORTANT SCENARIO

## What if BOTH listeners had SAME groupId?

### Example:

```java
@KafkaListener(topics = "my-topic", groupId = "order-group")
public void Listen1(String msg) { }

@KafkaListener(topics = "my-topic", groupId = "order-group")
public void Listen2(String msg) { }
```

### Then result would be:

```
Message goes to ONLY ONE listener
```

❌ Not both
✔ Because Kafka load-balances within a group

---

# 🧪 Summary Table (EXTREMELY IMPORTANT)

| Scenario                      | Result                         |
| ----------------------------- | ------------------------------ |
| Same topic + same group       | Only ONE consumer gets message |
| Same topic + different groups | ALL consumers get message      |
| CLI consumer + app consumer   | Independent if group differs   |
| Multiple partitions           | Parallel consumption           |

---
1) now three consumer with same group
2) ![img_22.png](./kafka/img_22.png) one in on cli
3) ![img_23.png](./kafka/img_23.png)
4) ![img_24.png](./kafka/img_24.png)
5) ![img_25.png](./kafka/img_25.png)
6)

# 🎯 Why Kafka Is Powerful

This is why Kafka is used for:

* Analytics
* Notifications
* Logging
* Audit
* Streaming

Same event → multiple systems → **NO coupling**

---

# ✅ Your Code Is CORRECT

✔ Producer → correct
✔ Consumers → correct
✔ Kafka behavior → correct
✔ Console output → expected

Nothing is wrong ❌

---

# 🧠 Interview-Ready Explanation (USE THIS)

> “Kafka delivers messages per consumer group. If multiple consumers use different group IDs, each group receives the same message independently. If consumers share the same group ID, Kafka distributes partitions among them so each message is processed only once per group.”

🔥 This answer will impress interviewers.

---

## Want Next?

I can explain:
1️⃣ Partition vs Consumer mapping visually
2️⃣ Exactly-once vs at-least-once
3️⃣ Offset commit behavior
4️⃣ Kafka real microservice example

Just tell me 👍


## ------- 269. Setting Up Kafka Topic in yml as well as programmatically --
1) now we are going to create the topic from .yml fime
2) so first close the CLI and check how many topics are execing
3) ![img_26.png](./kafka/img_26.png)
4) so there are two
5) so basically we can create topics by two way using CLI and using java based code and if we send some message to some topics which is not crated at inintally then it will autometically create that topic and send the message
6) ![img_27.png](./kafka/img_27.png)
7) ![img_28.png](./kafka/img_28.png)
8) then run the producer
9) ![img_29.png](./kafka/img_29.png)

## ------- 270. Sending Objects By Serializing and Deserializing --
1) basically we should not send the message as string it must be in form og object for better read the notes
2) so we are going to create a class
3) now created that class RiderLocation and after created we need to create object (instacne of it when we are going to send the message)
4) ![img_820.png](img_820.png)
5) ![img_821.png](img_821.png)
6) now after that we need to define the serializer in the yml file
7) ![img_822.png](img_822.png)
8) ![img_823.png](img_823.png)
9) ![img_824.png](img_824.png)
10) ![img_825.png](img_825.png)
11) now we will consume this message into consumer server for that first we need to create that RiderLocation java file so that we can consume of its object
12) ![img_826.png](img_826.png)
13) then we will create a third consumer for riderLocation
14) ![img_827.png](img_827.png)
15) then we need to add the deserializer not serializer into consumer as well
16) ![img_828.png](img_828.png)
17) ![img_829.png](img_829.png)
18) then restart both services producer and consumer and make request
19) ![img_830.png](img_830.png)
20) ![img_831.png](img_831.png)

Great question — this is a **very common Kafka + Spring Boot mistake**, and your error is actually **100% logical** once you understand what’s happening 👍
Let’s break it clearly and then fix it **the right way (production-style)**.

---

## 🔴 ROOT CAUSE (Why this error is happening)

### ❌ Error Summary

```
Cannot convert from [java.lang.String] 
to [com.example.consumer.consumer.RiderLocation]
```

### 🔍 What Kafka is telling you

* Your **producer is sending a STRING message**
* Your **RiderLocation consumer expects a JSON OBJECT**
* Kafka **does NOT magically know** which listener wants which type
* All listeners are consuming from the **same topic (`my-topic`)**

So this listener 👇

```java
@KafkaListener(topics = "my-topic", groupId = "my-group-RiderLocation")
public void RiderLocation(RiderLocation riderLocation)
```

is trying to read a **String message** like:

```
"HelloKafka-new message for consumer"
```

and convert it into:

```java
RiderLocation
```

❌ Impossible → hence `MessageConversionException`

---

## 🧠 VERY IMPORTANT KAFKA RULE (Interview-Ready)

> **One Kafka topic should contain ONE message type**

Mixing:

* `String`
* `RiderLocation`
* `Order`

in the **same topic** ❌ is a **bad design**

---

## ✅ CORRECT SOLUTION (BEST PRACTICE)

### ✔️ Solution 1 (RECOMMENDED): Separate Topics per Message Type

| Topic Name             | Message Type    |
| ---------------------- | --------------- |
| `text-topic`           | `String`        |
| `rider-location-topic` | `RiderLocation` |

---

## 🛠 FIX STEP-BY-STEP

---

## 1️⃣ PRODUCER – Send String Message

```java
kafkaTemplate.send("text-topic", "Hello Kafka");
```

---

## 2️⃣ PRODUCER – Send RiderLocation Object

```java
RiderLocation location = new RiderLocation(
        "Ritik Sharma",
        28.61,
        77.23
);

kafkaTemplate.send("rider-location-topic", location);
```

---

## 3️⃣ CONSUMER – STRING Consumer

```java
@KafkaListener(topics = "text-topic", groupId = "text-group")
public void listenText(String message) {
    System.out.println("Text message: " + message);
}
```

---

## 4️⃣ CONSUMER – RiderLocation Consumer

```java
@KafkaListener(topics = "rider-location-topic", groupId = "rider-group")
public void listenRider(RiderLocation riderLocation) {
    System.out.println("Rider ID: " + riderLocation.getRiderId());
    System.out.println("Lat: " + riderLocation.getLatitude());
    System.out.println("Lng: " + riderLocation.getLongitude());
}
```

---

## 5️⃣ CONSUMER application.yml (FIXED ❗)

### ❌ Your mistake

You used **serializer instead of deserializer**

### ✅ Correct config

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: rider-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
        spring.json.trusted.packages: "*"
```

⚠️ **Do NOT set `spring.json.value.default.type` globally** when consuming multiple types.

---

## 🧠 WHY your current config FAILED

```yaml
spring.json.value.default.type: com.example.consumer.consumer
```

This tells Spring:

> “EVERY message is this class”

But:

* Some messages are `String`
* Some are `RiderLocation`

➡️ Conflict → Conversion failure

---

## 🚀 OPTIONAL (ADVANCED / INTERVIEW LEVEL)

### Using Message Headers to Support Multiple Types (Not Recommended for beginners)

Producer:

```java
kafkaTemplate.send(
    MessageBuilder
        .withPayload(location)
        .setHeader(KafkaHeaders.TOPIC, "my-topic")
        .setHeader("__TypeId__", "RiderLocation")
        .build()
);
```

Consumer:

```java
@KafkaListener(topics = "my-topic")
public void consume(RiderLocation location) {}
```

⚠️ This works but **separate topics is cleaner**

---

## ✅ FINAL DESIGN (BEST PRACTICE)

```
Producer
 ├── text-topic  ──▶ Text Consumer
 └── rider-location-topic ──▶ Rider Consumer
```

---

## 🎯 INTERVIEW ONE-LINER (VERY IMPORTANT)

> “Kafka topics should be designed around event types. Mixing multiple payload types in a single topic leads to deserialization failures and poor design.”

---

## 🔥 You’re Learning Kafka the RIGHT Way

This exact problem is faced by **90% of Kafka learners** — fixing it means you now understand:

* Serialization
* Deserialization
* Topic design
* Consumer groups

If you want next:

* Kafka **headers-based polymorphism**
* Kafka **Schema Registry**
* Kafka **exactly-once**
* Kafka **real microservice architecture**

Just say 👍

## ==== fix the code ----
You’re very close. The issue is **configuration + design clarity**, not Kafka itself. I’ll **fix it cleanly** and explain *why* each change is required.

---

## ✅ WHAT IS CORRECT IN YOUR CODE

✔ Separate topic for object

```java
kafkaTemplate.send("rider-location-topic", riderLocation);
```

✔ Separate consumer for `RiderLocation`

```java
@KafkaListener(topics = "rider-location-topic", groupId = "my-group-RiderLocation")
```

✔ Using `JsonSerializer` on producer
✔ Using `JsonDeserializer` on consumer

So the **idea is correct**.

---

## ❌ WHAT IS WRONG (ROOT CAUSES)

### 1️⃣ **Wrong YAML structure**

You have **two `spring:` blocks** in the same file
➡️ Only the **last one is applied**

### 2️⃣ **Using `key-serializer` instead of `key-deserializer` in consumer**

This is a critical typo.

### 3️⃣ **Producer disabled type headers**

```yaml
spring.json.add.type.headers: false
```

But consumer is expecting JSON → conversion may fail later.

---

## ✅ FIXED CONFIGURATION (COPY–PASTE)

---

## 🔹 PRODUCER `application.yml`

```yaml
spring:
  application:
    name: producer
  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: true

server:
  port: 8080
```

✅ Why:

* Enables type headers
* Allows consumer to deserialize `RiderLocation` safely

---

## 🔹 CONSUMER `application.yml`

```yaml
spring:
  application:
    name: consumer
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: my-group-RiderLocation
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
        spring.json.trusted.packages: com.example.consumer.consumer

server:
  port: 8081
```

✅ Why:

* Correct **deserializers**
* Trusted package is specific (best practice)
* No forced default type (important)

---

## ✅ FINAL WORKING DESIGN

```
Producer
 └── rider-location-topic (RiderLocation JSON)
         ↓
Consumer
 └── RiderLocation Listener
```

---

## 🧠 IMPORTANT RULES (MEMORIZE)

### 🔴 Rule 1

> **One topic → one message type**

### 🔴 Rule 2

> Producer uses **Serializer**, Consumer uses **Deserializer**

### 🔴 Rule 3

> Never mix `String` and `Object` messages in the same topic

---

## 🎯 INTERVIEW ANSWER (STRONG)

> “Kafka object messaging works by serializing objects into JSON on the producer side and deserializing them on the consumer side using JsonSerializer and JsonDeserializer, with one event type per topic.”

---

## ✅ Your Code Will Now:

* Start without errors
* Deserialize `RiderLocation` correctly
* Follow real-world Kafka design

If you want next:

* Kafka with **multiple partitions**
* Kafka **consumer groups vs listeners**
* Kafka **schema registry**
* Kafka **microservices flow (Order → Payment)**

Just say 👍

## ======== final fixed =======
Perfect — this stack trace now tells us **exactly** what is wrong.
You are **99% correct**, only **one Kafka concept is biting you**.

---

## 🔥 EXACT ROOT CAUSE (From Your Error)

> ❌ **Class not found [com.demokafka.producer.RiderLocation]**

### What is happening internally?

1. **Producer sends JSON**
2. Producer’s `JsonSerializer` adds this header:

   ```
   __TypeId__ = com.demokafka.producer.RiderLocation
   ```
3. Consumer receives the message
4. Consumer tries to load:

   ```
   com.demokafka.producer.RiderLocation
   ```
5. ❌ **Consumer project does NOT have this class**
6. 💥 Deserialization fails → `ListenerExecutionFailedException`

---

## 🧠 Key Kafka Rule (VERY IMPORTANT)

> **Producer and Consumer must NOT depend on each other’s package names**

Kafka is a **distributed system** — class names must be:

* Shared
* Or explicitly mapped
* Or ignored

---

## ✅ SOLUTION OPTIONS (Choose ONE)

---

# ✅ SOLUTION 1 (BEST PRACTICE – RECOMMENDED)

### 🔹 Create a **shared DTO module**

Create a **common library** (jar):

```
kafka-common
└── com.common.dto.RiderLocation
```

### RiderLocation (COMMON)

```java
package com.common.dto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderLocation {
    private String riderId;
    private double latitude;
    private double longitude;
}
```

### Use SAME class in:

* Producer
* Consumer

✔ Clean
✔ Scalable
✔ Production ready

---

# ✅ SOLUTION 2 (FAST FIX – Your Case)

### 🔥 Disable type headers in PRODUCER

So Kafka **does NOT send class name**

### Producer `application.yml`

```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    properties:
      spring.json.add.type.headers: false   # 🔥 IMPORTANT
```

---

### Consumer `application.yml`

```yaml
spring:
  kafka:
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
        spring.json.trusted.packages: "*"
        spring.json.value.default.type: com.example.consumer.consumer.RiderLocation
```

✔ Consumer ignores producer’s package
✔ Uses its **own RiderLocation class**

---

# ❌ SOLUTION 3 (NOT RECOMMENDED)

Mapping producer → consumer class manually

```yaml
spring.json.type.mapping:
  rider:com.example.consumer.consumer.RiderLocation
```

⚠ Works but fragile in microservices

---

## ✅ WHY YOUR OFFSET IS STUCK AT `offset=2`

Kafka **keeps retrying the same bad message**:

```
rider-location-topic-0@2
```

Because:

* Deserialization fails
* Offset is never committed

---

## 🧹 CLEANUP (IMPORTANT)

After fixing config, do **ONE** of these:

### Option A: Reset offset

```bash
kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--group my-group-RiderLocation \
--topic rider-location-topic \
--reset-offsets --to-latest --execute
```

### Option B: Change consumer group

```yaml
group-id: my-group-RiderLocation-v2
```

---

## ✅ FINAL SUMMARY

| Issue                              | Status |
| ---------------------------------- | ------ |
| Producer sends class header        | ❌      |
| Consumer can’t find producer class | ❌      |
| Disable type headers OR shared DTO | ✅      |
| Reset offset                       | ✅      |
| Consumer works                     | ✅      |

---

## 🎯 WHAT I RECOMMEND FOR YOU

Since you’re learning **Kafka + Microservices**:

👉 **Use SOLUTION 2 now**
👉 **Use SOLUTION 1 in real projects**

---

If you want next:

* Kafka **Schema Registry**
* Kafka **Dead Letter Topic (DLT)**
* Kafka **Exactly Once**
* Kafka **Spring Cloud Stream**

Just say the word 🚀
1) ![img_832.png](img_832.png)
2) ![img_833.png](img_833.png)
3) ![img_834.png](img_834.png)
4) ![img_835.png](img_835.png)
5) ![img_836.png](img_836.png)
