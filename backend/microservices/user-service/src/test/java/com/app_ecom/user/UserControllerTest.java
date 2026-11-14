package com.app_ecom.user;

import com.app_ecom.user.controllers.UserController;
import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.exception.ResourceNotFoundException;
import com.app_ecom.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean UserService userService;

    @Test
    void meRequiresTheGatewayIdentityHeader() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void missingProfileIs404() throws Exception {
        when(userService.getByKeycloakId("kc-1")).thenThrow(new ResourceNotFoundException("Profile not found"));
        mvc.perform(get("/api/users/me").header("X-User-ID", "kc-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Not Found")));
    }

    @Test
    void invalidProfileIsRejectedWithFieldErrors() throws Exception {
        mvc.perform(put("/api/users/me").header("X-User-ID", "kc-1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"email\":\"not-an-email\",\"phone\":\"12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void validProfileIsSavedForTheCallerFromTheHeader() throws Exception {
        UserResponse response = new UserResponse();
        response.setId("u1");
        response.setEmail("alice@example.com");
        when(userService.upsertMyProfile(eq("kc-1"), any())).thenReturn(response);

        mvc.perform(put("/api/users/me").header("X-User-ID", "kc-1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Alice","email":"alice@example.com","phone":"+919876543210",
                                 "address":{"street":"MG Road","city":"Pune","country":"India","zipcode":"411001"}}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("alice@example.com")));
        verify(userService).upsertMyProfile(eq("kc-1"), any());
    }
}
