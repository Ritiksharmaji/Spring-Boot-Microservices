package com.app_ecom.user.service;

import com.app_ecom.user.dto.AddressDTO;
import com.app_ecom.user.dto.UserRequest;
import com.app_ecom.user.dto.UserResponse;
import com.app_ecom.user.model.Address;
import com.app_ecom.user.model.User;
import com.app_ecom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final List<User> users = new ArrayList<>();
    private final UserRepository userRepository;
    private Long nextId = 1L;

    public List<UserResponse> fetchAllUsers(){
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void addUser( UserRequest userRequest){
        User user = new User();
        updateUserFromRequest(user,userRequest);
        userRepository.save(user);
    }

    public Optional<UserResponse> fetchUser(Long id){
        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    // for updating user
    public boolean updateUser(Long id, UserRequest updateUser){
        return userRepository.findById(id)
                .map(existingUser->{
                    updateUserFromRequest(existingUser, updateUser);
                    userRepository.save(existingUser);
                    return true;
                }).orElse(false);
    }

    private void updateUserFromRequest(User user, UserRequest userRequest) {
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        if (userRequest.getAddress() != null){
            Address address = new Address();
            address.setStreet(userRequest.getAddress().getStreet());
            address.setState(userRequest.getAddress().getState());
            address.setZipcode(userRequest.getAddress().getZipcode());
            address.setCountry(userRequest.getAddress().getCountry());
            address.setCity(userRequest.getAddress().getCity());

            user.setAddress(address);

        }

    }

    private UserResponse mapToUserResponse(User user){
        UserResponse response = new UserResponse();
        response.setId(String.valueOf(user.getId()));
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setPhone(user.getPhone());
        if(user.getAddress() != null){
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setStreet(user.getAddress().getStreet());
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setState(user.getAddress().getState());
            addressDTO.setCountry(user.getAddress().getCountry());
            addressDTO.setZipcode(user.getAddress().getZipcode());
            response.setAddress(addressDTO);
        }
        return response;

    }

}
