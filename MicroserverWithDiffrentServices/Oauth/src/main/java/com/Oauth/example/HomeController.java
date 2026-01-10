package com.Oauth.example;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

//    @GetMapping("/")
//    public String home(){
//        return "Welcome , Hello";
//    }
        @GetMapping("/")
        public String home(OAuth2AuthenticationToken token){
            String email = token.getPrincipal().getAttribute("email");
            String name = token.getPrincipal().getAttribute("name");
            String role = token.getPrincipal().getAttribute("role");
            return "Welcome, "+ email + " , "+ name + " ," + role;
        }

}
