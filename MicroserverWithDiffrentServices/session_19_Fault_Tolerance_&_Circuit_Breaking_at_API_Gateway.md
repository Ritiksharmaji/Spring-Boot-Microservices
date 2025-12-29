## ------ Section 19: Fault Tolerance and Circuit Breaking at Gateway and Microservices Level ---
## --- 229. Introduction to Fault Tolerance ---
1) ![img_486.png](img_486.png)
2) ![img_487.png](img_487.png)
3) ![img_488.png](img_488.png)
4) ![img_489.png](img_489.png)
5) ![img_490.png](img_490.png)
6) ![img_491.png](img_491.png)

## ---- 230. Understanding Failures and Strategies for Fault Tolerance
1) ![img_492.png](img_492.png)
2) ![img_493.png](img_493.png)
3) ![img_494.png](img_494.png)
4) ![img_495.png](img_495.png)
5) ![img_496.png](img_496.png)

## ----- 231. Introduction to Resilience4J Modules and Features ---
1) ![img_497.png](img_497.png)
2) ![img_498.png](img_498.png)
3) ![img_499.png](img_499.png)
4) ![img_500.png](img_500.png)
5) ![img_501.png](img_501.png)
6) ![img_502.png](img_502.png)
7) ![img_503.png](img_503.png)
8) ![img_504.png](img_504.png)
9) ![img_505.png](img_505.png)
10) ![img_506.png](img_506.png)
11) ![img_507.png](img_507.png)

## --------- 232. Introduction to Circuit Breaking  ---
1) ![img_508.png](img_508.png)
2) ![img_509.png](img_509.png)

## ----- 233. Integrating Resilience4J with Spring Boot --
1) add the below two dependeies to order service
2) ![img_510.png](img_510.png)
```declarative
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
    </dependency>

```
## ----- 234. Implementing Circuit Breaking with Resilience4J --
1) now we are going to implement the Resilience4J to the order service
2) first run all the service 
3) ![img_511.png](img_511.png)
4) ![img_512.png](img_512.png)
5) ![img_513.png](img_513.png)
6) so here we are using order service beacuse we need to define the use of Resilience4J and order service is dependeed on product and user so it is best exmaple that if any of both service is not woking then how it will handle it
7) now we will shotdown the user service then make a call for order then we will see the error
8) ![img_514.png](img_514.png)
9) ![img_515.png](img_515.png)
10) ![img_516.png](img_516.png)
11) to see the logs related to 
12) ![img_517.png](img_517.png)
13) ![img_518.png](img_518.png)
14) ![img_519.png](img_519.png)
15) ![img_520.png](img_520.png)

You’re **very close** 👍
The only broken part is the **Resilience4j circuit breaker configuration** at the bottom.
Right now it’s just a comment-like sentence, which makes the YAML **invalid**.

Since you’re working on **Spring Boot Microservices (ORDER-SERVICE)** with **Eureka + Gateway + Resilience4j**, here is the **correct and production-ready configuration**.

---

## ✅ Correct `resilience4j` Circuit Breaker Configuration

Replace this ❌

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
         configrathe circute bracnker here
```

With this ✅

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        registerHealthIndicator: true

    instances:
      productService:
        baseConfig: default
```

---

## 🔍 What this Configuration Means (Important for Interviews)

| Property                                       | Meaning                            |
| ---------------------------------------------- | ---------------------------------- |
| `slidingWindowSize: 10`                        | Last 10 calls are considered       |
| `minimumNumberOfCalls: 5`                      | CB activates only after 5 calls    |
| `failureRateThreshold: 50`                     | If ≥50% calls fail → circuit opens |
| `waitDurationInOpenState: 10s`                 | Circuit stays OPEN for 10 sec      |
| `permittedNumberOfCallsInHalfOpenState: 3`     | Test calls in HALF_OPEN            |
| `automaticTransitionFromOpenToHalfOpenEnabled` | Auto recovery                      |
| `registerHealthIndicator`                      | Visible in `/actuator/health`      |

---

## ✅ How to Use This Circuit Breaker in Code

### Order Service → Calling Product Service

```java
@CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
public ProductResponse getProduct(Long productId) {
    return productClient.getProduct(productId);
}

public ProductResponse productFallback(Long productId, Exception ex) {
    return new ProductResponse(productId, "Product Service Down", 0);
}
```

---

## 🔥 Actuator Endpoints (You Already Enabled Them)

You can now access:

* Health:

```
http://localhost:8083/actuator/health
```

* Circuit Breaker status:

```
http://localhost:8083/actuator/health/circuitbreakers
```

---

## ✅ Your Config Is Now:

✔ Valid YAML
✔ Production-ready
✔ Microservices standard
✔ Interview-safe

If you want next:

* Retry + TimeLimiter config
* CircuitBreaker with **Feign Client**
* Gateway-level circuit breaker
* Full **Order → Product → Inventory** flow

Just tell me 👍
Good question — **your configuration is correct**, but the reason you **don’t see the circuit breaker in `/actuator/health`** is **NOT a YAML issue**.
This is a **very common Resilience4j + Actuator misunderstanding** 👍

Let’s break it down clearly.

---

## ✅ Why Circuit Breaker Is NOT Showing

### 1️⃣ Circuit Breakers are **LAZY**

Resilience4j **does NOT create a circuit breaker at startup**.

👉 A circuit breaker instance is created **ONLY when it is used at least once**.

Right now:

* You defined `productService`
* But **no request has triggered it yet**
* So Actuator doesn’t show it

✅ **This is expected behavior**

---

## 2️⃣ You Are Checking the Wrong Endpoint

You checked:

```
/actuator/health
```

❌ That endpoint **does NOT list individual circuit breakers**

---

## ✅ Correct Endpoints to Check Circuit Breaker

### 🔥 1. List all circuit breakers

```
http://localhost:8083/actuator/circuitbreakers
```

### 🔥 2. Details of a specific circuit breaker

```
http://localhost:8083/actuator/circuitbreakers/productService
```

### 🔥 3. Circuit breaker health

```
http://localhost:8083/actuator/health/circuitbreakers
```

⚠️ `/health` alone will **never** show it

---

## 3️⃣ Mandatory Dependency Check (MOST IMPORTANT)

Make sure this dependency exists in **ORDER-SERVICE**

### ✅ Required dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

If missing → **Circuit breaker will never appear**

---

## 4️⃣ Make Sure You Actually USE the Circuit Breaker

Example:

```java
@CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
public ProductResponse getProduct(Long productId) {
    return productClient.getProduct(productId);
}
```

⚠️ If this method is **never called**, circuit breaker will not exist.

---

## 5️⃣ Force Circuit Breaker to Appear (Test)

### Step 1: Stop PRODUCT-SERVICE

### Step 2: Call ORDER-SERVICE endpoint that hits product-service

(do this 5–6 times)

Now check:

```
/actuator/circuitbreakers
```

You WILL see:

```json
{
  "circuitBreakers": [
    "productService"
  ]
}
```

---

## 6️⃣ Why Your Current `/health` Output Is Still OK

Your output proves:

* ✅ Actuator working
* ✅ Eureka connected
* ✅ DB UP
* ✅ RabbitMQ UP
* ❌ CircuitBreaker not triggered yet → not created

This line confirms Actuator is enabled correctly:

```yaml
management:
  health:
    circuitbreakers:
      enable: true
```

---

## ✅ Summary (Interview-Ready Answer)

> **Circuit breakers in Resilience4j are created lazily and appear in Actuator only after the annotated method is invoked at least once. They are visible via `/actuator/circuitbreakers`, not `/actuator/health`.**

---

If you want next, I can help you:

* ✅ Force OPEN state for testing
* ✅ Add Retry + TimeLimiter
* ✅ Feign Client Circuit Breaker
* ✅ API Gateway level circuit breaker

Just say the word 👍
![img_521.png](img_521.png)
![img_522.png](img_522.png)
2) now make a request :
3) ![img_523.png](img_523.png)
4) ![img_524.png](img_524.png)
5) make one more
6) ![img_525.png](img_525.png)
7) now make ata leat four and five request so that it hsould go to haif
8) ![img_526.png](img_526.png)
9) then after 10 second it will goes to haif just refresh the page
10) ![img_527.png](img_527.png)
11) now start the user service
12) then after fews request or first also 
13) ![img_528.png](img_528.png)
14) ![img_529.png](img_529.png)
15) make more quest then it will close also
16) ![img_530.png](img_530.png)
17) ![img_531.png](img_531.png)

## ----- 236. Adding Fall Back Mechanism ---
1) ones a server is down then we are getting as internal server error which is not good user experieans for that we will add fallback
2) ![img_532.png](img_532.png)
3) ![img_533.png](img_533.png)
4) close the user service and make the request:
5) ![img_534.png](img_534.png)
6) ![img_535.png](img_535.png)
7) for more details: https://resilience4j.readme.io/docs/circuitbreaker
8) ![img_536.png](img_536.png)
9) ![img_537.png](img_537.png)

## ----- 237. Circuit Breaker Advanced Configurations ---
1) ![img_538.png](img_538.png)
2) ![img_539.png](img_539.png)

## ----- 238. Circuit Breaker Using Spring Cloud Gateway--
1) as we know that apiGateway is used to handle the incomming request based on that request it forward to respective service so it is better to handle that handle the fault tolerance here it selph because apigateway know that which service is running or not so from apigateway we can handle the fallback
2) for that we need to do some configuration on the gateway level
3) we need to add the dependecy of circuitbreaker-reactor-resilience4j
```declarative
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```
4) after adding the dependecies we need to add some configuration rleated to circuit breaking
5) two types of confguration one is confige the circuit breack into the configuration file (GateWayconfig file)
6) and second is add the configuration of gateway to application.yml file
```declarative
management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway,routes
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enable: true
    shutdown:
      enabled: true
```
6) ![img_540.png](img_540.png)
7) ![img_541.png](img_541.png)
8) without adding above the setting 
9) ![img_542.png](img_542.png)
10) and after adding run the application then hit the url:
11) now see
12) ![img_543.png](img_543.png)
13) ![img_544.png](img_544.png)
14) so still not circulte bracke configure at
15) now we need to configure the circuit breacker at gateway level
16) ![img_545.png](img_545.png)
17) then start the both configserver then apiGateway service and hit the url:
18) ![img_546.png](img_546.png)
19) now after having the circuit breacker we need to add it 
20) ![img_547.png](img_547.png)
21) now restart the apigateway service and make the request when product is running
21) ![img_548.png](img_548.png)
22) ![img_549.png](img_549.png)
23) now close the product service and make the request
24) ![img_550.png](img_550.png)
25) ![img_551.png](img_551.png)
26) make more requst so that it should goes to open and see the response as well
27) ![img_552.png](img_552.png)
28) ![img_553.png](img_553.png)
29) then after 10 second it will goes to haif as we have configure
30) ![img_554.png](img_554.png)
31) now start the prooduct service
32) ![img_555.png](img_555.png)
33) ![img_556.png](img_556.png)
34) now if we close the product service then we are getting error as internal service error an timput to make it as usefull error
35) we need to add a fallback url and response as well
36) for that we will create a contoller 
37) ![img_557.png](img_557.png)
38) ![img_558.png](img_558.png)
39) so till now we can configure the circuit breacker at apigateway level as well method level also 
40) but when we are calling post method
41) ![img_559.png](img_559.png)
42) so fix it

You’re **99% correct** 👍
This issue is a **classic Spring Cloud Gateway fallback + HTTP method mismatch**.

---

## ❌ Problem (Why POST says *Method Not Allowed*)

Your fallback controller is:

```java
@GetMapping("/fallback/products")
public ResponseEntity<List<String>> productsFallback() {
    ...
}
```

But when **PRODUCT-SERVICE is down** and you send a **POST request** like:

```
POST /api/products
```

Gateway forwards the request to:

```
POST /fallback/products
```

⚠️ Spring then says:

> ❌ **405 Method Not Allowed**
> because `/fallback/products` only supports **GET**, not **POST**

---

## ✅ Root Cause (Interview Explanation)

> **Spring Cloud Gateway forwards the original HTTP method to the fallback URI.
> If the fallback endpoint does not support that HTTP method, a 405 error occurs.**

---

## ✅ FIX 1 (BEST & RECOMMENDED)

### ✔ Accept **ALL HTTP METHODS** in fallback

### 🔥 Change `@GetMapping` → `@RequestMapping`

```java
@RestController
public class FallbackController {

    @RequestMapping("/fallback/products")
    public ResponseEntity<List<String>> productsFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonList(
                        "Product service is unavailable. Please try later."
                ));
    }
}
```

✅ Works for:

* GET
* POST
* PUT
* DELETE

👉 **This is the production-standard solution**

---

## ✅ FIX 2 (Method-Specific – Optional)

If you want different responses per method:

```java
@RestController
public class FallbackController {

    @GetMapping("/fallback/products")
    public ResponseEntity<String> getFallback() {
        return ResponseEntity.status(503)
                .body("GET: Product service unavailable");
    }

    @PostMapping("/fallback/products")
    public ResponseEntity<String> postFallback() {
        return ResponseEntity.status(503)
                .body("POST: Product service unavailable");
    }
}
```

⚠️ More code, rarely needed.

---

## 🔥 Your Gateway Config Is ALREADY CORRECT

This part is perfect 👇

```java
.filters(f -> f.circuitBreaker(config -> config
        .setName("ecomBreaker")
        .setFallbackUri("forward:/fallback/products")
))
```

No changes needed here ✅

---

## ✅ Quick Test Checklist

1. Stop **PRODUCT-SERVICE**
2. Call:

   ```
   GET  /api/products
   POST /api/products
   ```
3. Both should return:

   ```
   503 SERVICE UNAVAILABLE
   ```

---

## 🧠 Interview One-Liner (Very Important)

> **In Spring Cloud Gateway, fallback endpoints must support the same HTTP methods as the original request because the gateway forwards the method unchanged. The safest approach is using `@RequestMapping` to handle all methods.**

---

If you want next:

* Gateway **CircuitBreaker + Retry**
* Method-specific fallback logic
* Gateway **RateLimiter**
* Gateway fallback with JSON error structure

Just tell me 🚀
1) ![img_560.png](img_560.png)
2) ![img_561.png](img_561.png)
3) ![img_562.png](img_562.png)

now impletement same for other as well
1) ![img_563.png](img_563.png)
2) ![img_564.png](img_564.png)
3) ![img_565.png](img_565.png)
4) ![img_566.png](img_566.png)
5) ![img_567.png](img_567.png)
6) ![img_568.png](img_568.png)
7) ![img_569.png](img_569.png)
8) ![img_570.png](img_570.png)
9) ![img_571.png](img_571.png)
10) now see all the services are running
11) ![img_572.png](img_572.png)
12) ![img_573.png](img_573.png)
13) ![img_574.png](img_574.png)
14) ![img_575.png](img_575.png)
15) ![img_576.png](img_576.png)
16) now close the order service then make request:
17) ![img_577.png](img_577.png)
18) ![img_578.png](img_578.png)

