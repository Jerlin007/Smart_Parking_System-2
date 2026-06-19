package com.parking.service;

import com.parking.entity.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    void deleteUser(Long id);

    User activateUser(Long id);

    User deactivateUser(Long id);

    User updateUser(Long id, User updated);
}
