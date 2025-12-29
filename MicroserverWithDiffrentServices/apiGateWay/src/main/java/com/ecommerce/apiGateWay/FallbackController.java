package com.ecommerce.apiGateWay;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

//@RestController
//public class FallbackController {
//
//    //@GetMapping("/fallback/products")
//    @RequestMapping("/fallback/products")
//    public ResponseEntity<List<String>> productsFallback(){
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//                .body(Collections.singletonList("Product service is unavialble now try latter"));
//    }
//}

// ========= other ==========
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/products")
    public ResponseEntity<List<String>> productFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonList(
                        "Product service is unavailable. Please try later."
                ));
    }

    @RequestMapping("/users")
    public ResponseEntity<List<String>> userFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonList(
                        "User service is unavailable. Please try later."
                ));
    }

    @RequestMapping("/orders")
    public ResponseEntity<List<String>> orderFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonList(
                        "Order service is unavailable. Please try later."
                ));
    }

    @RequestMapping("/cart")
    public ResponseEntity<List<String>> cartFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonList(
                        "Cart service is unavailable. Please try later."
                ));
    }
}

