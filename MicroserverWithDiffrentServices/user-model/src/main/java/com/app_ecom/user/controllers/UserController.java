package com.app_ecom.user.controllers;

import com.app_ecom.user.dto.UserRequest;
import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

  //  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
    public ResponseEntity<UserResponse> getUser(@PathVariable String id){
//        logger.info("Request received for user:{}",id );
//        logger.trace("this is TRACE level - very details logs");
//        logger.debug("this is DEBUG level - used for developer debugging");
//        logger.warn("this is WARM level - general System information");
//        logger.error("this is ERROR level - something failed");
        // ============= using lombok =======================
        log.info("Request received for user:{}",id );
        log.trace("this is TRACE level - very details logs");
        log.debug("this is DEBUG level - used for developer debugging");
        log.warn("this is WARM level - general System information");
        log.error("this is ERROR level - something failed");

        return userService.fetchUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());

    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<String> UpdateUser(@PathVariable String id, @RequestBody UserRequest updateUserRequest){
        boolean updated = userService.updateUser(id, updateUserRequest);
        if (updated) {
            return ResponseEntity.ok("user updated"); // return updated user
        } else {
            return ResponseEntity.notFound().build(); // 404 if user not found
        }

    }

}
