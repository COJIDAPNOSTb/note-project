package com.example.authservice.app.controller;

import org.springframework.context.annotation.Role;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Secured("ROLE_USER")
    @GetMapping("/user")
    public String testUser()
    {
        return "You are User!";
    }

    @GetMapping("/admin")
    @Secured("ROLE_ADMIN")
    public String testAdmin()
    {
        return "You are Admin!";
    }
}
