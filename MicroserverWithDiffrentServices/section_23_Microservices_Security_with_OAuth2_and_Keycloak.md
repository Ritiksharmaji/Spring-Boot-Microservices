
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
8) 