package com.mnnitproject.location_service.controller;

import com.mnnitproject.location_service.entity.Users;
import com.mnnitproject.location_service.entity.Users;
import com.mnnitproject.location_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/api/users")
    public Users addUser(@RequestBody Users user) {
        return userService.saveUser(user);
    }

    @GetMapping("/api/users/{name}")
    public List<Users> getUserByName(@PathVariable String name) {
        return userService.getUsersByName(name);
    }

    // Serve HTML page from /static
    @GetMapping("/")
    public ResponseEntity<Resource> getPage() {
        Resource html = new ClassPathResource("static/user-form.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
