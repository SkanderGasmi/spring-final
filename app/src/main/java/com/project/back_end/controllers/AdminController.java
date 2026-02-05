package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Admin;
import com.project.back_end.services.AuthService;

@RestController
@RequestMapping("${api.path}" + "admin")
@CrossOrigin(origins = "http://localhost:3000") // Adjust for your frontend
public class AdminController {

    private final AuthService authService;

    @Autowired
    public AdminController(AuthService authService) {
        this.authService = authService;
    }
   
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        System.out.println("ADMIN LOGIN HIT");
        return authService.validateAdmin(admin);
    }
}