package com.mnnitproject.location_service.repository;


import com.mnnitproject.location_service.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<Users, Long> {
    List<Users> findByName(String name);
}