package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Admin;
import com.project.back_end.services.AuthService;

@RestController
@RequestMapping("${api.path}" + "admin")
public class AdminController {

    private final AuthService authService;

    @Autowired
    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return authService.validateAdmin(admin);
    }
}