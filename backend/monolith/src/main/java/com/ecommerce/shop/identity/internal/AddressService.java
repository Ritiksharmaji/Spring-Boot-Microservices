package com.ecommerce.shop.identity.internal;

import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ecommerce.shop.identity.internal.IdentityDtos.*;

/** Address book: the first address becomes the default; exactly one default at a time. */
@Service
@RequiredArgsConstructor
class AddressService {

    static final int MAX_ADDRESSES = 10;

    private final AddressRepository addresses;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(long userId) {
        return addresses.findByUserIdOrderByDefaultAddressDescIdAsc(userId).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse create(long userId, AddressRequest request) {
        if (addresses.countByUserId(userId) >= MAX_ADDRESSES) {
            throw BusinessException.invalid("Too Many Addresses", "You can save up to " + MAX_ADDRESSES + " addresses");
        }
        Address a = new Address();
        a.setUserId(userId);
        apply(a, request);
        boolean first = addresses.countByUserId(userId) == 0;
        Address saved = addresses.saveAndFlush(a);
        if (first || Boolean.TRUE.equals(request.isDefault())) {
            makeDefault(userId, saved);
        }
        return AddressResponse.from(saved);
    }

    @Transactional
    public AddressResponse update(long userId, long id, AddressRequest request) {
        Address a = own(userId, id);
        apply(a, request);
        if (Boolean.TRUE.equals(request.isDefault())) {
            makeDefault(userId, a);
        }
        return AddressResponse.from(a);
    }

    @Transactional
    public void delete(long userId, long id) {
        Address a = own(userId, id);
        addresses.delete(a);
        addresses.flush();
        if (a.isDefaultAddress()) {
            addresses.findByUserIdOrderByDefaultAddressDescIdAsc(userId).stream().findFirst()
                    .ifPresent(next -> next.setDefaultAddress(true));
        }
    }

    @Transactional
    public void deleteAll(long userId) {
        addresses.deleteAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Address> defaultAddress(long userId) {
        return addresses.findFirstByUserIdAndDefaultAddressTrue(userId);
    }

    /** Used by PUT /api/users/me: creates or updates the default address. */
    @Transactional
    public void upsertDefault(long userId, AddressRequest request) {
        Optional<Address> current = defaultAddress(userId);
        if (current.isPresent()) {
            apply(current.get(), request);
        } else {
            create(userId, request);
        }
    }

    private void makeDefault(long userId, Address chosen) {
        addresses.findByUserIdOrderByDefaultAddressDescIdAsc(userId)
                .forEach(a -> a.setDefaultAddress(a.getId().equals(chosen.getId())));
    }

    private void apply(Address a, AddressRequest r) {
        a.setLabel(r.label());
        a.setFirstName(r.firstName().trim());
        a.setLastName(r.lastName());
        a.setPhone(r.phone());
        a.setStreet(r.street().trim());
        a.setCity(r.city().trim());
        a.setState(r.state());
        a.setZipcode(r.zipcode().trim());
        a.setCountry(r.country().trim());
    }

    private Address own(long userId, long id) {
        return addresses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> BusinessException.notFound("Address " + id + " not found"));
    }
}
