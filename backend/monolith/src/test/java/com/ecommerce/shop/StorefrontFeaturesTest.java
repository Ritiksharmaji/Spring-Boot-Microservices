package com.ecommerce.shop;

import com.ecommerce.shop.notification.NotificationSender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Features used by the React storefront: sizes, images, address book, wishlist, cash on delivery. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StorefrontFeaturesTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean NotificationSender sender;

    TestClient api;
    String admin;
    String customer;
    long shirt;

    @BeforeEach
    void setUp() throws Exception {
        api = new TestClient(mvc, json);
        admin = api.login("admin@test.local", "Admin12345");
        customer = api.register("s-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local");
        shirt = api.read(api.call("POST", "/api/products", admin, """
                {"name":"Men Slim Shirt","price":1299.00,"stockQuantity":5,"category":"Men","subCategory":"Topwear",
                 "sizes":["S","M","L"],"bestseller":true,"images":["/products/a.png","/products/b.png"]}"""))
                .get("id").asLong();
    }

    private String orderField(long orderId, String field) throws Exception {
        return api.read(api.call("GET", "/api/orders/" + orderId, customer, null)).get(field).asText();
    }

    @Test
    void productsCarryImagesSizesSubCategoryAndBestseller() throws Exception {
        api.call("GET", "/api/products/" + shirt, null, null)
                .andExpect(jsonPath("$.sizes", contains("S", "M", "L")))
                .andExpect(jsonPath("$.images", contains("/products/a.png", "/products/b.png")))
                .andExpect(jsonPath("$.imageUrl", is("/products/a.png")))
                .andExpect(jsonPath("$.subCategory", is("Topwear")))
                .andExpect(jsonPath("$.bestseller", is(true)));
    }

    @Test
    void cartLinesArePerSizeAndSizeIsRequired() throws Exception {
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":1}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title", is("Invalid Size")));
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":1,\"size\":\"XXL\"}")
                .andExpect(status().isBadRequest());

        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":2,\"size\":\"m\"}")
                .andExpect(status().isCreated());
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":1,\"size\":\"L\"}")
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items[0].size", is("M")))
                .andExpect(jsonPath("$.items[0].imageUrl", is("/products/a.png")))
                .andExpect(jsonPath("$.totalItems", is(3)));
        // stock (5) is shared by all sizes of the product
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":3,\"size\":\"S\"}")
                .andExpect(status().isConflict());
        api.call("PUT", "/api/cart/items/" + shirt + "?size=L", customer, "{\"quantity\":3}")
                .andExpect(jsonPath("$.totalItems", is(5)));
        api.call("DELETE", "/api/cart/items/" + shirt + "?size=M", customer, null).andExpect(status().isNoContent());
        api.call("GET", "/api/cart", customer, null).andExpect(jsonPath("$.items.length()", is(1)));
    }

    @Test
    void addressBookKeepsExactlyOneDefault() throws Exception {
        String home = """
                {"label":"Home","firstName":"Riya","phone":"+919876543210","street":"MG Road 1","city":"Pune",
                 "state":"MH","zipcode":"411001","country":"India"}""";
        long first = api.read(api.call("POST", "/api/addresses", customer, home).andExpect(status().isCreated()))
                .get("id").asLong();
        long second = api.read(api.call("POST", "/api/addresses", customer, home.replace("Home", "Work")
                .replace("MG Road 1", "IT Park").replace("}", ",\"isDefault\":true}"))).get("id").asLong();

        JsonNode list = api.read(api.call("GET", "/api/addresses", customer, null));
        assertThat(list).hasSize(2);
        assertThat(list.get(0).get("id").asLong()).isEqualTo(second);           // default first
        assertThat(list.get(0).get("isDefault").asBoolean()).isTrue();
        assertThat(list.get(1).get("isDefault").asBoolean()).isFalse();
        api.call("GET", "/api/users/me", customer, null).andExpect(jsonPath("$.address.street", is("IT Park")));

        api.call("DELETE", "/api/addresses/" + second, customer, null).andExpect(status().isNoContent());
        JsonNode after = api.read(api.call("GET", "/api/addresses", customer, null));
        assertThat(after).hasSize(1);
        assertThat(after.get(0).get("id").asLong()).isEqualTo(first);
        assertThat(after.get(0).get("isDefault").asBoolean()).isTrue();          // promoted

        String other = api.register("o-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local");
        api.call("DELETE", "/api/addresses/" + first, other, null).andExpect(status().isNotFound());
        api.call("POST", "/api/addresses", customer, "{\"firstName\":\"\"}").andExpect(status().isBadRequest());
    }

    @Test
    void wishlistIsIdempotentAndValidatesProducts() throws Exception {
        api.call("PUT", "/api/wishlist/" + shirt, customer, null).andExpect(jsonPath("$.productIds", contains((int) shirt)));
        api.call("PUT", "/api/wishlist/" + shirt, customer, null).andExpect(jsonPath("$.productIds.length()", is(1)));
        api.call("PUT", "/api/wishlist/999999", customer, null).andExpect(status().isNotFound());
        api.call("DELETE", "/api/wishlist/" + shirt, customer, null).andExpect(jsonPath("$.productIds.length()", is(0)));
        api.call("GET", "/api/wishlist", null, null).andExpect(status().isUnauthorized());
    }

    @Test
    void checkoutWithEnteredAddressCashOnDeliveryNotesAndSizes() throws Exception {
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":2,\"size\":\"M\"}");
        JsonNode order = api.read(api.call("POST", "/api/orders", customer, """
                        {"paymentMethod":"CASH","notes":"Leave at the door",
                         "shippingAddress":{"firstName":"Riya","lastName":"Sharma","phone":"+919876543210",
                          "street":"MG Road 1","city":"Pune","state":"MH","zipcode":"411001","country":"India"}}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod", is("CASH")))
                .andExpect(jsonPath("$.items[0].size", is("M")))
                .andExpect(jsonPath("$.items[0].imageUrl", is("/products/a.png")))
                .andExpect(jsonPath("$.recipientName", is("Riya Sharma")))
                .andExpect(jsonPath("$.notes", is("Leave at the door"))));
        long id = order.get("id").asLong();

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(orderField(id, "status")).isEqualTo("CONFIRMED"));
        assertThat(orderField(id, "paymentStatus")).isEqualTo("CASH_ON_DELIVERY");
        api.call("GET", "/api/payments/orders/" + id, customer, null).andExpect(jsonPath("$.status", is("CASH_ON_DELIVERY")));

        api.call("PATCH", "/api/orders/" + id + "/status", admin, "{\"status\":\"SHIPPED\"}");
        api.call("PATCH", "/api/orders/" + id + "/status", admin, "{\"status\":\"DELIVERED\"}");
        assertThat(orderField(id, "paymentStatus")).isEqualTo("PAID");
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                api.call("GET", "/api/payments/orders/" + id, customer, null).andExpect(jsonPath("$.status", is("COMPLETED"))));

        api.call("POST", "/api/orders", customer, "{\"paymentMethod\":\"CASH\",\"shippingAddress\":{\"firstName\":\"\"}}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void cardOrderCancelledAfterPaymentShowsRefund_cashOrderCancelledIsNotCharged() throws Exception {
        api.saveAddress(customer);
        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":1,\"size\":\"S\"}");
        long card = api.read(api.call("POST", "/api/orders", customer, "{\"paymentMethod\":\"CARD\"}")).get("id").asLong();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(orderField(card, "paymentStatus")).isEqualTo("PAID"));
        api.call("POST", "/api/orders/" + card + "/cancel", customer, null).andExpect(status().isOk());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(orderField(card, "paymentStatus")).isEqualTo("REFUNDED"));

        api.call("POST", "/api/cart/items", customer, "{\"productId\":" + shirt + ",\"quantity\":1,\"size\":\"S\"}");
        long cash = api.read(api.call("POST", "/api/orders", customer, "{\"paymentMethod\":\"CASH\"}")).get("id").asLong();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(orderField(cash, "status")).isEqualTo("CONFIRMED"));
        api.call("POST", "/api/orders/" + cash + "/cancel", customer, null)
                .andExpect(jsonPath("$.paymentStatus", is("NOT_CHARGED")));
        assertThat(api.read(api.call("GET", "/api/products/" + shirt, null, null)).get("stockQuantity").asInt()).isEqualTo(5);
    }

    @Test
    void customerCanDeleteTheirAccount_personalDataGoes_ordersStayAnonymised() throws Exception {
        String email = "del-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
        String token = api.register(email);
        api.saveAddress(token);
        api.call("PUT", "/api/wishlist/" + shirt, token, null);
        api.call("POST", "/api/cart/items", token, "{\"productId\":" + shirt + ",\"quantity\":1,\"size\":\"S\"}");
        long orderId = api.read(api.call("POST", "/api/orders", token, "{\"paymentMethod\":\"CASH\"}")).get("id").asLong();
        api.call("POST", "/api/cart/items", token, "{\"productId\":" + shirt + ",\"quantity\":1,\"size\":\"M\"}");

        api.call("DELETE", "/api/users/me", token, "{\"password\":\"wrong-pass1\"}")
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title", is("Wrong Password")));
        api.call("DELETE", "/api/users/me", admin, "{\"password\":\"Admin12345\"}")
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title", is("Admin Account")));

        api.call("DELETE", "/api/users/me", token, "{\"password\":\"Passw0rd!\"}").andExpect(status().isNoContent());

        api.call("POST", "/api/auth/login", null, "{\"email\":\"" + email + "\",\"password\":\"Passw0rd!\"}")
                .andExpect(status().isUnauthorized());
        api.call("GET", "/api/users/me", token, null)
                .andExpect(jsonPath("$.email", startsWith("deleted-")))
                .andExpect(jsonPath("$.phone").doesNotExist());
        api.call("GET", "/api/addresses", token, null).andExpect(jsonPath("$.length()", is(0)));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            api.call("GET", "/api/cart", token, null).andExpect(jsonPath("$.items.length()", is(0)));
            api.call("GET", "/api/wishlist", token, null).andExpect(jsonPath("$.productIds.length()", is(0)));
        });
        api.call("GET", "/api/orders/" + orderId, token, null).andExpect(status().isOk());
        // the email is free again
        api.register(email);
    }

    @Test
    void adminUploadsProductImages_onlyRealImages_servedPublicly() throws Exception {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 1, 2, 3};
        var upload = multipart("/api/products/images")
                .file(new MockMultipartFile("file", "shirt.jpg", "image/jpeg", png));

        String url = api.read(mvc.perform(upload.header("Authorization", "Bearer " + admin))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.url", matchesPattern("http://localhost/uploads/products/[0-9a-f]{32}\\.png"))))
                .get("url").asText();
        String path = url.substring("http://localhost".length());
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().bytes(png))
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")));

        // the uploaded URL is a valid product image
        api.call("PUT", "/api/products/" + shirt, admin, """
                {"name":"Men Slim Shirt","price":1299.00,"stockQuantity":5,"category":"Men","images":["%s"]}""".formatted(url))
                .andExpect(jsonPath("$.imageUrl", is(url)));

        mvc.perform(multipart("/api/products/images")
                        .file(new MockMultipartFile("file", "evil.png", "image/png", "<script>".getBytes()))
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title", is("Unsupported Image")));
        mvc.perform(multipart("/api/products/images")
                        .file(new MockMultipartFile("file", "a.png", "image/png", png))
                        .header("Authorization", "Bearer " + customer))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/products/images").file(new MockMultipartFile("file", "a.png", "image/png", png)))
                .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/products/images").header("Authorization", "Bearer " + admin))
                .andExpect(status().isBadRequest());
    }
}
