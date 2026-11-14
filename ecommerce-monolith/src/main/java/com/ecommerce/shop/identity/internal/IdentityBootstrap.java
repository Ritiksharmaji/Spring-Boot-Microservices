package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.identity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Start-up accounts: the first ADMIN from ADMIN_EMAIL / ADMIN_PASSWORD (never hard-coded),
 * and — only in demo mode — a public demo customer with an address so visitors can try checkout.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class IdentityBootstrap implements ApplicationRunner {

    static final String DEMO_EMAIL = "customer@shop.demo";
    static final String DEMO_PASSWORD = "Customer@123";

    private final UserRepository users;
    private final AddressService addresses;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-email:}")
    private String adminEmail;
    @Value("${app.bootstrap.admin-password:}")
    private String adminPassword;
    @Value("${app.demo.enabled:false}")
    private boolean demo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (StringUtils.hasText(adminEmail) && StringUtils.hasText(adminPassword)
                && !users.existsByEmailIgnoreCase(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail.trim().toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setFirstName("Shop");
            admin.setLastName("Admin");
            admin.setRole(Role.ADMIN);
            users.save(admin);
            log.info("Created admin account {}", admin.getEmail());
        } else if (!users.existsByRole(Role.ADMIN)) {
            log.warn("No ADMIN account exists; set ADMIN_EMAIL and ADMIN_PASSWORD to create one");
        }

        if (demo && !users.existsByEmailIgnoreCase(DEMO_EMAIL)) {
            User u = new User();
            u.setEmail(DEMO_EMAIL);
            u.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            u.setFirstName("Demo");
            u.setLastName("Customer");
            u.setPhone("+919876543210");
            users.saveAndFlush(u);
            addresses.create(u.getId(), new IdentityDtos.AddressRequest("Home", "Demo", "Customer", "+919876543210",
                    "MG Road 1", "Pune", "MH", "411001", "India", true));
            log.info("Demo mode: created demo customer {}", DEMO_EMAIL);
        }
    }
}
