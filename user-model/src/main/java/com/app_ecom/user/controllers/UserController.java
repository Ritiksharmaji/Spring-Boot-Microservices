package com.app_ecom.user.controllers;

import com.app_ecom.user.dto.UserRequest;
import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users")
    public List<UserResponse> getUsers(){
        return new ResponseEntity<> (userService.fetchAllUsers(), HttpStatus.OK).getBody();

    }

    @PostMapping("/api/users")
    public ResponseEntity<String>  createUser( @RequestBody UserRequest userRequest){
        userService.addUser(userRequest);
        return ResponseEntity.ok("User Added");
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id){
        return userService.fetchUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());

    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<String> UpdateUser(@PathVariable Long id, @RequestBody UserRequest updateUserRequest){
        boolean updated = userService.updateUser(id, updateUserRequest);
        if (updated) {
            return ResponseEntity.ok("user updated"); // return updated user
        } else {
            return ResponseEntity.notFound().build(); // 404 if user not found
        }

    }

}
