package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.entity.Users;
import com.mnnitproject.location_service.entity.Users;
import com.mnnitproject.location_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    Users saveUser(Users user);



    List<Users> getUsersByName(String name);
}