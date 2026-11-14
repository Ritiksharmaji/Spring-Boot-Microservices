package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.identity.Customer;
import com.ecommerce.shop.identity.CustomerDirectory;
import com.ecommerce.shop.identity.Role;
import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.ecommerce.shop.identity.internal.IdentityDtos.*;

@Service
@RequiredArgsConstructor
class IdentityService implements CustomerDirectory {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final LoginAttemptGuard loginGuard;

    /** Self-registration always creates a CUSTOMER. */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw BusinessException.conflict("Conflict", "Email " + email + " is already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName());
        user.setRole(Role.CUSTOMER);
        return token(users.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        loginGuard.checkAllowed(request.email());
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElse(null);
        if (user == null) {
            loginGuard.failed(request.email());
            throw BusinessException.unauthorized("Invalid email or password");
        }
        loginGuard.succeeded(request.email());
        return token(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(long userId) {
        return UserResponse.from(find(userId));
    }

    /** Updates name, phone and delivery address. Email and role are not changeable here. */
    @Transactional
    public UserResponse updateProfile(long userId, ProfileRequest request) {
        User user = find(userId);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        if (request.address() != null) {
            AddressDto a = request.address();
            user.setStreet(a.street());
            user.setCity(a.city());
            user.setState(a.state());
            user.setCountry(a.country());
            user.setZipcode(a.zipcode());
        }
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return users.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse get(long id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse changeRole(long id, Role role) {
        if (role == null) {
            throw BusinessException.invalid("Invalid Role", "role is required");
        }
        User user = find(id);
        user.setRole(role);
        return UserResponse.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomer(long userId) {
        return users.findById(userId).map(u -> new Customer(u.getId(), u.getEmail(), u.fullName(),
                u.hasAddress()
                        ? new Customer.DeliveryAddress(u.getStreet(), u.getCity(), u.getState(), u.getCountry(), u.getZipcode())
                        : null));
    }

    private TokenResponse token(User user) {
        return new TokenResponse(tokens.issue(user), "Bearer", tokens.ttlSeconds(), UserResponse.from(user));
    }

    private User find(long id) {
        return users.findById(id).orElseThrow(() -> BusinessException.notFound("User " + id + " not found"));
    }
}
