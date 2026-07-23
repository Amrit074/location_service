package com.mnnitproject.location_service.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class RootController {

    @GetMapping("/")
    public void redirectRoot(HttpServletResponse response) throws IOException {
        response.sendRedirect("/userform.html");
    }
}
