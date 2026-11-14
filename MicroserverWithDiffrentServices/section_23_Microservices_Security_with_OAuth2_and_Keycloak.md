
## ---- Section 23: Microservices Security with OAuth2 and Keycloak | Keycloak Masterclass--
## ----- 294. Introduction to Security, Authentication and Authorization ---
1) ![img_887.png](img_887.png)
2) ![img_888.png](img_888.png)
3) ![img_889.png](img_889.png)
4) ![img_890.png](img_890.png)
5) ![img_891.png](img_891.png)
6) 
## ----- 295. Introduction and Problem Statement --
1) ![img_892.png](img_892.png)
2) ![img_893.png](img_893.png)
3) ![img_895.png](img_895.png)
4) ![img_896.png](img_896.png)
5) ![img_897.png](img_897.png)
6) ![img_898.png](img_898.png)
7) ![img_899.png](img_899.png)
8) ![img_900.png](img_900.png)
9) ![img_901.png](img_901.png)

## --- 296. Overview of Keycloak - What and Why--
1) ![img_902.png](img_902.png)
2) keycloak work with spring security it is not replace the spring security

## ------------ 297. Installing and Setting Up Keycloak --
1) ![img_903.png](img_903.png)
2) ![img_904.png](img_904.png)
3) ![img_905.png](img_905.png)
4) ![img_906.png](img_906.png)
5) ![img_907.png](img_907.png)
6) docker run -d -p 127.0.0.1:8483:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.5.0 start-dev
7) ![img_908.png](img_908.png)
8) ![img_909.png](img_909.png)
9) ![img_910.png](img_910.png)
10) ![img_911.png](img_911.png)
11) ![img_912.png](img_912.png) so create a new user 
12) ![img_913.png](img_913.png)
13) ![img_914.png](img_914.png)
14) ![img_915.png](img_915.png)
15) ![img_916.png](img_916.png)
16) ![img_917.png](img_917.png)
17) ![img_918.png](img_918.png)
18) ![img_919.png](img_919.png)
19) now set the password
20) ![img_920.png](img_920.png)
21) ![img_921.png](img_921.png)
22) ![img_922.png](img_922.png)
23) now logout then login with new create user
24) ![img_923.png](img_923.png)
25) ![img_924.png](img_924.png)

## ---------- 298. Keycloak Architecture and Core Concepts --
1) ![img_925.png](img_925.png)
2) ![img_926.png](img_926.png)
3) ![img_927.png](img_927.png)
4) ![img_928.png](img_928.png)
5) 
## ---- 299. Different Protocols for Authentication with Keycloak ---
1) ![img_929.png](img_929.png)
2) ![img_930.png](img_930.png)
3) ![img_931.png](img_931.png)
4) ![img_932.png](img_932.png)
5) ![img_933.png](img_933.png)
6) https://openid.net/

## ---------- 300. Introduction to OAuth 2.0 and Different Flows --
1) ![img_934.png](img_934.png)
2) before 0AUth-2 
3) ![img_935.png](img_935.png)
4) ![img_936.png](img_936.png)
5) in 0Auth
6) ![img_937.png](img_937.png)
7) ![img_938.png](img_938.png)
8) ![img_939.png](img_939.png)
9) ![img_940.png](img_940.png)
10) ![img_941.png](img_941.png)
11) ![img_942.png](img_942.png)
12) ![img_943.png](img_943.png)

## ------ 301. What is Authorization Code Flow - How Does it Work? --
1) ![img_944.png](img_944.png)
2) ![img_945.png](img_945.png)
3) ![img_946.png](img_946.png)

## --------- 302. Implementing Authorization Code Flow ---
1) ![img_947.png](img_947.png)
2) ![img_948.png](img_948.png)
3) ![img_949.png](img_949.png)
4) after import that Oauth project we need to setup some key into the keyCloak 
5) login into keycloak and create a realm 
6) ![img_950.png](img_950.png)
7) ![img_951.png](img_951.png)
8) ![img_952.png](img_952.png)
9) ![img_953.png](img_953.png)
10) ![img_954.png](img_954.png)
11) ![img_955.png](img_955.png)
12) ![img_956.png](img_956.png)
13) ![img_957.png](img_957.png)
14) after making details in keycloak now make configuration into the yml file of Oauth2 service
15) ![img_958.png](img_958.png)
16) ![img_959.png](img_959.png)
17) go give the issue url into .yml:
18) ![img_960.png](img_960.png)
19) ![img_961.png](img_961.png)
20) ![img_962.png](img_962.png)
21) ![img_963.png](img_963.png)
22) now we will add a contoller to have a enpoint which work with our OAuth flow
23) ![img_964.png](img_964.png)
24) ![img_965.png](img_965.png)
25) ![img_966.png](img_966.png)
26) ![img_967.png](img_967.png)
    You’re **very close**, but the issue is a **wrong OAuth2 provider configuration**.
    Right now Spring Security is redirecting you to a **fake authorization server URL** (`https://auth-server/...`) instead of your **Keycloak server** (`localhost:8483`), so nothing loads.

Let’s fix this step by step 👇

---

## ❌ What’s going wrong

Your redirect URL is:

```
https://auth-server/oauth2/authorize
```

But your **actual auth server is Keycloak** running at:

```
http://localhost:8483/realms/auth2
```

Because of this mismatch:

* Browser redirects correctly
* But the authorization page **never loads**

---

## ✅ Correct Keycloak-based Configuration (Authorization Code Flow)

### ✔️ OPTION 1 (Recommended): Use ONLY `issuer-uri`

👉 **Do NOT manually define `authorization-uri`, `token-uri`, etc.**

Spring Boot will auto-discover everything from Keycloak.

### ✅ Correct `application.yml`

```yaml
spring:
  application:
    name: auth-code-flow

  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: oauth2-authrization-flow
            client-secret: GkvraQEvRllMqJnTtClSthPihVR1fkqf
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope:
              - openid
              - profile
              - email

        provider:
          keycloak:
            issuer-uri: http://localhost:8483/realms/auth2
```

✅ This is **enough**
✅ No need to define `authorization-uri`, `token-uri`, `userinfo-uri`

---

## ❌ What to REMOVE (Very Important)

Remove this completely 👇

```yaml
authorization-uri: https://auth-server/oauth2/authorize
token-uri: https://auth-server/oauth2/token
user-info-uri: https://auth-server/userinfo
```

These are **wrong URLs** for Keycloak.

---

## ✅ Security Configuration (MANDATORY)

You also need a **SecurityConfig**, otherwise Spring may behave unexpectedly.

```java
package com.Oauth.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login();

        return http.build();
    }
}
```

---

## ✅ Controller is FINE

Your controller is correct 👍

```java
@RestController
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "Welcome , Hello";
    }
}
```

---

## ✅ Expected Flow (After Fix)

1. Hit
   👉 `http://localhost:8080/`
2. Redirects to
   👉 `http://localhost:8483/realms/auth2/protocol/openid-connect/auth`
3. Keycloak **Login Page appears**
4. Login successfully
5. Redirects back to
   👉 `http://localhost:8080/`
6. Response:

   ```
   Welcome , Hello
   ```

---

## 🔍 Quick Checklist (Very Important)

✔ Keycloak running on **8483**
✔ Realm name = `auth2`
✔ Client type = **Confidential**
✔ Redirect URI in Keycloak:

```
http://localhost:8080/login/oauth2/code/keycloak
```

✔ Client Secret matches
✔ `openid` scope enabled

---

## 🚀 Next Steps (Recommended)

Since you already know **microservices & security**, next you should add:

* JWT extraction
* Role-based authorization
* Resource server config
* API Gateway with OAuth2
* Keycloak + Docker

If you want, I can:

* Debug your **Keycloak client settings**
* Add **JWT + role-based auth**
* Convert this to **microservice + gateway**
* Explain OAuth2 flow **visually (step-by-step)**

Just tell me 👍
1) ![img_968.png](img_968.png)
2) ![img_969.png](img_969.png)
3) ![img_970.png](img_970.png)
4) ![img_971.png](img_971.png)
5) now if we want to get the user details as well can 
6) ![img_972.png](img_972.png)
7) ![img_973.png](img_973.png)

## -----------  303. Introduction to Client Credentials Flow ---
1) ![img_974.png](img_974.png)
2) ![img_975.png](img_975.png)
3) ![img_976.png](img_976.png)
4) ![img_977.png](img_977.png)
5) ![img_978.png](img_978.png)
6) ![img_979.png](img_979.png)

## ------ 304. Implementing Client Credentials Flow --
1) know we are going to create two new services one services will have protected data which is exposed by protected api and second one is tying to consume that data which is protected by first service and both services are not client those are machine
2) ![img_980.png](img_980.png)
3) ![img_981.png](img_981.png)
4) now create one one which is client which trying to access that resource from the resource-service
5) ![img_982.png](img_982.png)
6) ![img_983.png](img_983.png)
7) now start the keyclock 
8) write the below command in 
```declarative
  docker run -d -p 8483:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.5.0 start-dev --hostname=localhos
```
9) ![img_994.png](img_994.png)
10) ![img_995.png](img_995.png)
11) ![img_996.png](img_996.png)
12) 
7) now create one more client 
8) ![img_984.png](img_984.png)
9) ![img_985.png](img_985.png)
10) ![img_986.png](img_986.png)
11) ![img_987.png](img_987.png)
12) ![img_988.png](img_988.png)
13) ![img_989.png](img_989.png)
14) now start working on resource service
15) ![img_990.png](img_990.png)
16) to get the above resource-service issurs url follow the below
16) ![img_997.png](img_997.png)
17) ![img_998.png](img_998.png)
18) 
16) after this we need to add the security-configuration
17) ![img_991.png](img_991.png)
18) now we need a protected API which client want to access
19) ![img_992.png](img_992.png)
20) now resource-server is configure propally now we will set the client
21) first configure the .yml file of client
22) ![img_999.png](img_999.png)
23) ![img_1000.png](img_1000.png)
24) ![img_1001.png](img_1001.png)
25) now ones configuration for client done we start to do the code for client 
26) ![img_1002.png](img_1002.png)
27) ![img_1003.png](img_1003.png)
28) ![img_1004.png](img_1004.png)
29) ![img_1005.png](img_1005.png)
30) now we will start the resource application 
31) ![img_1006.png](img_1006.png)
32) then start the client 
33) ![img_1007.png](img_1007.png)

soluation :

Very good 👏 your setup is almost correct.
The problem is **not in your client code logic** — it is most likely in **Keycloak configuration or token validation**.

Let’s debug properly step-by-step.

---

# ✅ First — Your Architecture Is Correct

You have:

### 🔐 Resource Server (8082)

* Validates JWT using:

```yaml
issuer-uri: http://localhost:8483/realms/auth2
```

### 🤖 Client (8081)

* Uses client_credentials
* Gets token from:

```
http://127.0.0.1:8483/realms/auth2/protocol/openid-connect/token
```

Code logic ✔ correct
OAuth2AuthorizedClientManager ✔ correct
Bearer token attached ✔ correct

So why 401?

---

# 🔴 The REAL Problem (Very Common)

In **Client Credentials Flow**, the token does NOT contain:

```
scope
roles
audience (correct resource)
```

And Spring Resource Server by default validates:

* issuer
* signature
* expiration
* audience (sometimes)
* scopes/authorities

Most likely your token does not have proper audience or roles for Service-2.

---

# ✅ Step 1 — Print The Token

Add this temporarily:

```java
System.out.println("Access Token: " + accessToken);
```

Copy the token.

Go to:

👉 [https://jwt.io](https://jwt.io)

Decode it.

Check:

* iss
* aud
* scope
* realm_access
* resource_access

---

# 🔴 Most Likely Issue #1 — Missing Audience

Your resource server expects token issued for it.

In Keycloak:

Go to:

```
Client → oauth2-client-credintials
```

Then:

### Client Settings → Access Type

Must be:

```
Confidential
```

---

### Then VERY IMPORTANT:

Go to:

```
Client → oauth2-client-credintials → Client Scopes
```

Make sure:

* "openid" default scope is added
* or create a client scope that adds audience

---

# 🔴 Most Common Fix (Audience Mapper)

In Keycloak:

1. Go to your client
2. Click **Client Scopes**
3. Add a new mapper:

```
Mapper Type: Audience
Included Client Audience: resourceservice
```

OR

Go to:

```
Client → Mappers → Create
```

Add:

```
Name: audience
Mapper Type: Audience
Included Client Audience: resourceservice
```

This ensures token has:

```json
"aud": "resourceservice"
```

---

# 🔴 Most Common Issue #2 — Different Host

You are using:

Resource Server:

```
issuer-uri: http://localhost:8483
```

Client:

```
token-uri: http://127.0.0.1:8483
```

⚠️ localhost ≠ 127.0.0.1 in strict validation sometimes.

👉 Change token-uri to:

```
http://localhost:8483/realms/auth2/protocol/openid-connect/token
```

Keep everything consistent.

---

# 🔴 Most Common Issue #3 — Realm Mismatch

Make sure:

```
iss in JWT =
http://localhost:8483/realms/auth2
```

If it says 127.0.0.1 and your resource server expects localhost → 401.

They must match EXACTLY.

---

# 🧪 Quick Debug Trick

Temporarily allow all in resource server:

```java
.authorizeHttpRequests(auth -> auth
        .anyRequest().permitAll()
)
```

If it works → token validation problem
If still fails → client not sending token

---

# 🎯 What I Think Is Happening

Most likely:

✔ Token is generated
✔ Token is attached
❌ Resource server rejects due to issuer mismatch or audience

---

# 🚀 Clean Checklist

### In Client YAML

Change:

```
token-uri: http://127.0.0.1:8483
```

to:

```
token-uri: http://localhost:8483
```

---

### In Keycloak

Ensure:

* Client type = Confidential
* Service Accounts Enabled = ON
* Audience mapper exists OR disable audience validation

---

# 💥 If You Want Immediate Working Solution

Add this in resource server to ignore audience:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8483/realms/auth2
          audiences: []
```

Or customize decoder bean.

---

# 🎓 Final Question For You

When you decode JWT:

What is the value of:

```
iss
aud
```

Send me those two values only.
Then I will give you exact final fix.

```

Here is a **correct, complete `application.yml`** for a **pure OAuth2 client using Client Credentials Flow**
👉 **Service-to-Service (no user, no browser)**
👉 **Calling a secured Resource Server (8082)**
👉 **Keycloak as Auth Server**

---

## ✅ `application.yml` — PURE CLIENT (8081)

```yaml
spring:
  application:
    name: pure-client

  security:
    oauth2:
      client:
        registration:
          keycloak-client:
            client-id: pure-client
            client-secret: YOUR_CLIENT_SECRET
            authorization-grant-type: client_credentials
            scope:
              - payment.read

        provider:
          keycloak:
            token-uri: http://localhost:8483/realms/auth2/protocol/openid-connect/token

server:
  port: 8081
```

---
![img_1008.png](img_1008.png)

=== output ===
![img_1009.png](img_1009.png)


## 🔑 Important Notes

### ✔ `client-id`

Must be the **Keycloak client ID**

### ✔ `client-secret`

From:

```
Keycloak → Clients → pure-client → Credentials
```

### ✔ `scope`

Must match **Keycloak client scopes / roles**

---

## ✅ Required Dependency (Client App)

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

---
================ Belows are using WebClient as we have did using RestTemp as above ===
## ✅ WebClient Config (TOKEN AUTO-INJECTION)

```java
@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient(ClientRegistrationRepository repo,
                        OAuth2AuthorizedClientService service) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(repo, service);

        oauth.setDefaultClientRegistrationId("keycloak-client");

        return WebClient.builder()
                .apply(oauth.oauth2Configuration())
                .build();
    }
}
```

---

## ✅ Calling Resource Server (8082)

```java
@RestController
@RequestMapping("/client")
public class ClientController {

    private final WebClient webClient;

    public ClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/call-resource")
    public String callResource() {
        return webClient.get()
                .uri("http://localhost:8082/api/hello")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
```

---

## 🧪 Full Flow Test

1️⃣ Start Keycloak (8483)
2️⃣ Start Resource Server (8082)
3️⃣ Start Client (8081)
4️⃣ Hit:

```
GET http://localhost:8081/client/call-resource
```

---

## ❌ If You See Errors

| Error           | Reason          |
| --------------- | --------------- |
| 401             | Scope missing   |
| 403             | Role not mapped |
| invalid_client  | Wrong secret    |
| issuer mismatch | Wrong realm     |

---

## 🧠 Architecture You Now Have

```
pure-client (8081)
   ↓ (client_credentials)
Keycloak (8483)
   ↓ (JWT)
resource-service (8082)
```

🔥 **This is real production microservice security**

---

## 🚀 Next (Choose One)

* Scope → Role mapping in Keycloak
* API Gateway with OAuth2
* Token relay between services
* Securing Kafka with OAuth2
* Full interview-ready explanation

Just say **next** 💪

Good catch — there are **2 problems** here:
1️⃣ Java code has a **syntax + design issue**
2️⃣ OAuth2 **will NOT work automatically with plain `RestTemplate`**

I’ll fix **both properly** ✅

---

# ✅ FIX 1: Java Code (Syntax + Correct OAuth2 Usage)

### ❌ Problem in your code

```java
return new RestTemplate()
```

➡ missing `;`

But **bigger issue** ❌
👉 `RestTemplate` **does NOT automatically attach OAuth2 tokens**

For **Client Credentials Flow**, you must use:
✔ `OAuth2AuthorizedClientManager`
✔ or `WebClient` (recommended)

---

# ✅ RECOMMENDED FIX (Production-Ready)

## ✔ Use `WebClient` (Best Practice)

### ✅ `PureClientApplication.java`

```java
package com.client.example.pure_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PureClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(PureClientApplication.class, args);
    }
}
```

---

## ✅ OAuth2 WebClient Configuration

### `WebClientConfig.java`

```java
package com.client.example.pure_client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        // IMPORTANT: registration id from application.yml
        oauth2.setDefaultClientRegistrationId("keycloak-client");

        return WebClient.builder()
                .apply(oauth2.oauth2Configuration())
                .build();
    }
}
```

---

## ✅ Client Controller (Calling Resource Service)

```java
package com.client.example.pure_client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class ClientController {

    private final WebClient webClient;

    @Value("${service2.url}")
    private String service2Url;

    public ClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/call-service2")
    public String callService2() {
        return webClient.get()
                .uri(service2Url + "/api/hello")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
```

---

# ✅ FIX 2: application.yml (Mostly Correct, Small Improvements)

Your YAML is **almost perfect** 👍
Just **clean & finalize it**

```yaml
spring:
  application:
    name: pure-client

  security:
    oauth2:
      client:
        registration:
          keycloak-client:
            client-id: oauth2-client-credintials
            client-secret: j59g9M8g48dVB1u26z50QaFjxypgGvWf
            authorization-grant-type: client_credentials
            scope:
              - payment.read

        provider:
          keycloak:
            token-uri: http://localhost:8483/realms/auth2/protocol/openid-connect/token

service2:
  url: http://localhost:8082

server:
  port: 8081
```

### ❌ Removed `openid`

`openid` is **NOT needed** for client credentials
(no user, no ID token)

---

# ✅ Required Dependency (Don’t Miss This)

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

# 🧪 End-to-End Test

1️⃣ Start **Keycloak** (8483)
2️⃣ Start **Resource Service** (8082)
3️⃣ Start **Pure Client** (8081)

Hit:

```
GET http://localhost:8081/call-service2
```

### ✅ Expected Output

```
Hello from Resource Server
```

---

# ❌ If Something Fails

| Error              | Cause                               |
| ------------------ | ----------------------------------- |
| 401                | Scope missing in token              |
| 403                | Scope not mapped in resource server |
| invalid_client     | Wrong secret                        |
| Connection refused | Service 8082 not running            |

---

# 🔥 You Are Now Using REAL Client Credentials Flow

✔ No user
✔ Secure JWT
✔ Token auto-managed
✔ Microservice ready

---

## 🚀 Next (Tell Me What You Want)

* Role vs Scope mapping in Keycloak
* Securing API Gateway
* Token relay pattern
* Client Credentials interview Q&A
* RestTemplate version (if interviewer asks)

Just say **next** 💪

![img_993.png](img_993.png)
Great screenshot 👍 — this **actually proves your configuration is working**, but there’s a **conceptual misunderstanding** (very common, even for experienced devs).

Let’s clear it **once and for all**.

---

# ❌ Why you are seeing a **blank “Login with OAuth 2.0” page**

You opened:

```
http://localhost:8081/login
```

And you see:

> **Login with OAuth 2.0**
> (no buttons, no Keycloak redirect)

### 🚨 This is NOT an error

### 🚨 This is EXPECTED behavior for **Client Credentials Flow**

---

# 🧠 CORE CONCEPT (MOST IMPORTANT)

## 🔐 Client Credentials Flow ≠ User Login

| Flow                   | Who logs in?         | Browser used? |
| ---------------------- | -------------------- | ------------- |
| Authorization Code     | 👤 User              | ✅ Yes         |
| Password Grant         | 👤 User              | ⚠️ Deprecated |
| **Client Credentials** | 🤖 Machine (Service) | ❌ NO          |

👉 **Client Credentials Flow has NO LOGIN PAGE**

---

# 🔥 Why the page is blank

Spring shows the OAuth login page **only when there are OAuth clients that support user login**.

Your client:

```yaml
authorization-grant-type: client_credentials
```

🚫 Client Credentials **does not involve a user**
🚫 No redirect to Keycloak
🚫 No login form
🚫 No button shown

So Spring renders an **empty login page**.

✔️ This means Spring is behaving correctly.

---

# ✅ HOW CLIENT CREDENTIALS FLOW REALLY WORKS

### ❌ WRONG WAY (what you tried)

```
Browser → /login → expect Keycloak page
```

### ✅ CORRECT WAY

```
Service → Token Endpoint → Access Token → Call Resource Server
```

---

# 🧪 CORRECT TEST (DO THIS)

### Call your client endpoint directly:

```
GET http://localhost:8081/call-service2
```

### What happens internally:

1. Spring requests token from Keycloak
2. Keycloak issues **access token**
3. Client calls resource server (8082)
4. Resource server validates JWT
5. Response returned

✔️ **NO browser login involved**

---

# 🧩 WHY `/login` EXISTS AT ALL THEN?

Spring Security auto-configures `/login` **if OAuth Client is on classpath**.

But:

* No `authorization_code` clients → no buttons
* Only `client_credentials` → empty page

---

# 🔍 IF YOU WANT A LOGIN PAGE (OPTIONAL)

You must use:

```yaml
authorization-grant-type: authorization_code
```

and add:

```yaml
redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```

👉 That’s a **different flow** (user-based login)

---

# 🏆 INTERVIEW-READY EXPLANATION (VERY IMPORTANT)

> “Client Credentials Flow is used for service-to-service communication.
> It does not involve a browser or user authentication.
> Therefore, there is no login page or redirect.”

If you say this in interviews → **instant strong impression** 💯

---

# ✅ SUMMARY (PIN THIS)

✔ Your config is **correct**
✔ Blank login page is **expected**
✔ Client Credentials ≠ user login
✔ Test via API, not browser
✔ You are implementing **real microservice security**

---

## 🔥 Next Level (Tell me what you want next)

* Decode & inspect JWT token
* Map Keycloak roles → Spring authorities
* Secure endpoints with scopes
* API Gateway + Client Credentials
* Common interview traps in OAuth2

Just say 👉 **next** 🚀

## === 306. Client as OAuth2 Client and Resource Server ===
1) till now we have create a client which is console based now we are going to create a client which expose the api throw the postman
2) now creating new service 
3) ![img_1010.png](img_1010.png)
4) then load it as moven project
5) ![img_1011.png](img_1011.png)
6) now this service will act like both client as well resource service so we need to config it as both type for that first get all the config of client and paste to it 
7) ![img_1012.png](img_1012.png)
8) now  we need to security config to validate the token which will come from postman so we can get that from resourceservice or we can create as well 
9) ![img_1013.png](img_1013.png)
10) after this we need to prepare our client to communicate with resource server
11) we can take that client code accept to commond line and paste into security config below in same file 
12) ![img_1014.png](img_1014.png)
13) now after this we need to create two files one is a controller which will expose the endpoint and second one we need to create a class which as a method called fetchData which will do the job to communicating to resource server and get the data .
14) ![img_1015.png](img_1015.png)
15) ![img_1016.png](img_1016.png)
16) then start the application:
17) ![img_1017.png](img_1017.png)
18) ![img_1018.png](img_1018.png)
19) means we need to pass the token of keyclock using postman
20) ![img_1019.png](img_1019.png)
21) ![img_1020.png](img_1020.png)
22) ![img_1021.png](img_1021.png)
23) ![img_1022.png](img_1022.png)
24) other way is doing is 
25) ![img_1023.png](img_1023.png)
26) ![img_1024.png](img_1024.png)
27) ![img_1025.png](img_1025.png)
28) ![img_1026.png](img_1026.png)
29) ![img_1027.png](img_1027.png)
30) so till now we are getting token from postman and validating it then generation new token and by tat new token we are calling requst to resource service 
31) ![img_1028.png](img_1028.png)
32) now we are going to use the same token which we are sending by postman
33) ![img_1029.png](img_1029.png)
34) ![img_1030.png](img_1030.png)

## ----- 307. Introduction to PKCE Flow -----
1) ![img_1031.png](img_1031.png)
2) ![img_1032.png](img_1032.png)
3) ![img_1033.png](img_1033.png)
4) ![img_1034.png](img_1034.png)
5) ![img_1035.png](img_1035.png)

## ======== 308. Implementing PKCE Flow ====
1) now we are going to implement the pkce flow in a service for that we doing in separete service so create a spring boot project
2) ![img_1036.png](img_1036.png)
3) ![img_1037.png](img_1037.png)
4) now we need to configure the KeyClock client to this servcies 
5) now run the keycloak and login into it.
6) and create a client for it 
7) ![img_1038.png](img_1038.png)
8) ![img_1039.png](img_1039.png)
9) then add the frontend app url 
10) ![img_1040.png](img_1040.png)
11) ![img_1041.png](img_1041.png)
12) ![img_1042.png](img_1042.png)
13) ![img_1043.png](img_1043.png)
14) now we need to add rhe security config file 
15) ![img_1044.png](img_1044.png)
16) then create a controller
17) ![img_1045.png](img_1045.png)
18) now run and test the application
19) ![img_1046.png](img_1046.png)
20) ![img_1047.png](img_1047.png)
21) ![img_1048.png](img_1048.png)
22) ![img_1049.png](img_1049.png)
23) ![img_1050.png](img_1050.png)
24) ![img_1051.png](img_1051.png)
25) ![img_1052.png](img_1052.png)
26) ![img_1053.png](img_1053.png)
27) ![img_1054.png](img_1054.png)
28) so if we want to get the user name that can also possibe 
29) go to jwt.io website and paste the generated token there
30) ![img_1055.png](img_1055.png)
31) ![img_1056.png](img_1056.png)

## =================== 309. Refresh Token in OAuth2 =====
1) ![img_1057.png](img_1057.png)
2) ![img_1058.png](img_1058.png)
3) ![img_1059.png](img_1059.png)
4) ![img_1060.png](img_1060.png)


## =========== 310. Implementing PKCE Flow with React ====
1) we need to create a react app using vite
2) ![img_1061.png](img_1061.png)
3) ![img_1062.png](img_1062.png)
4) now we are going to config the react with react-oauth2-pkce beacuse it don't support by default
5) for that we need to use the external library which is : npm i react-oauth2-code-pkce
6) ![img_1063.png](img_1063.png)
7) ![img_1064.png](img_1064.png)
8) ![img_1065.png](img_1065.png)
9) ![img_1066.png](img_1066.png)
10) then automatically frontend app will protected 
11) ![img_1067.png](img_1067.png)
12) ![img_1068.png](img_1068.png)
13) ![img_1069.png](img_1069.png)
14) ![img_1070.png](img_1070.png)
15) ![img_1071.png](img_1071.png)
16) now we will access the backend api ones user will login 
17) ![img_1072.png](img_1072.png)
18) ![img_1073.png](img_1073.png)
19) ![img_1074.png](img_1074.png)
20) so we need to enable the cross-origin from backend
21) ![img_1075.png](img_1075.png)
22) ![img_1076.png](img_1076.png)
23) ![img_1077.png](img_1077.png)
24) ![img_1078.png](img_1078.png)
25) ![img_1079.png](img_1079.png)

## ======= 311. Resource to learn more =====
1) ![img_1080.png](img_1080.png)
2) https://oauth.net/2/
3) https://auth0.com/
4) ![img_1081.png](img_1081.png)
5) 