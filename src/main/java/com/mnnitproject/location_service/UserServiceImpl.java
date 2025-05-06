package com.mnnitproject.location_service;



import com.mnnitproject.location_service.entity.Users;
import com.mnnitproject.location_service.repository.UserRepository;
import com.mnnitproject.location_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public Users saveUser(Users user) {
        return userRepository.save(user);
    }

    @Override
    public List<Users> getUsersByName(String name) {
        return userRepository.findByName(name);
    }
}
