package com.ecommerce.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Small helper around MockMvc for JSON calls with a bearer token. */
public record TestClient(MockMvc mvc, ObjectMapper json) {

    public ResultActions call(String method, String path, String token, Object body) throws Exception {
        MockHttpServletRequestBuilder req = switch (method) {
            case "GET" -> get(path);
            case "POST" -> post(path);
            case "PUT" -> put(path);
            case "PATCH" -> patch(path);
            case "DELETE" -> delete(path);
            default -> throw new IllegalArgumentException(method);
        };
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON)
                    .content(body instanceof String s ? s : json.writeValueAsString(body));
        }
        return mvc.perform(req);
    }

    public JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    public String login(String email, String password) throws Exception {
        return read(call("POST", "/api/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")).get("accessToken").asText();
    }

    public String register(String email) throws Exception {
        return read(call("POST", "/api/auth/register", null,
                "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"" + email + "\",\"password\":\"Passw0rd!\"}"))
                .get("accessToken").asText();
    }

    public void saveAddress(String token) throws Exception {
        call("PUT", "/api/users/me", token, """
                {"firstName":"Riya","lastName":"Sharma","phone":"+919876543210",
                 "address":{"street":"MG Road 1","city":"Pune","state":"MH","country":"India","zipcode":"411001"}}""");
    }

    public long createProduct(String adminToken, String name, String price, int stock) throws Exception {
        return read(call("POST", "/api/products", adminToken,
                "{\"name\":\"" + name + "\",\"price\":" + price + ",\"stockQuantity\":" + stock + ",\"category\":\"test\"}"))
                .get("id").asLong();
    }
}
