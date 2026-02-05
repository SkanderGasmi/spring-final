package com.project.back_end.mvc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.back_end.services.AuthService;

@Controller
public class DashboardController {

    @Autowired
    private AuthService authService;

    // Root route
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    // Admin dashboard - render Thymeleaf template WITH token validation
    @GetMapping("/adminDashboard")
    public ModelAndView adminDashboard(@RequestParam(value = "token", required = false) String token) {
        System.out.println("DEBUG: adminDashboard() called with token: " + token);
        
        ModelAndView mav = new ModelAndView();
        
        if (token == null || token.isEmpty()) {
            mav.setViewName("redirect:/index.html");
            return mav;
        }
        
        Map<String, String> response = authService.validateToken(token, "ADMIN");
        if (response.containsKey("error")) {
            mav.setViewName("redirect:/index.html");
            return mav;
        }
        
        // Valid token - render Thymeleaf template
        mav.setViewName("admin/adminDashboard");
        
        // Pass user info to template
        mav.addObject("username", response.get("username"));
        mav.addObject("role", "ADMIN");
        // IMPORTANT: Do NOT pass token to template - it should be in localStorage
        return mav;
    }

    // Doctor dashboard - render Thymeleaf template
    @GetMapping("/doctorDashboard")
    public ModelAndView doctorDashboard(@RequestParam(value = "token", required = false) String token) {
        System.out.println("DEBUG: doctorDashboard() called with token: " + token);
        
        ModelAndView mav = new ModelAndView();
        
        if (token == null || token.isEmpty()) {
            mav.setViewName("redirect:/index.html");
            return mav;
        }
        
        Map<String, String> response = authService.validateToken(token, "DOCTOR");
        if (response.containsKey("error")) {
            mav.setViewName("redirect:/index.html");
            return mav;
        }
        
        mav.setViewName("doctor/doctorDashboard");
        mav.addObject("username", response.get("username"));
        mav.addObject("role", "DOCTOR");
        return mav;
    }
    
    // Patient dashboard - render Thymeleaf template
    @GetMapping("/patientDashboard")
    public ModelAndView patientDashboard(@RequestParam(value = "token", required = false) String token) {
        System.out.println("DEBUG: patientDashboard() called with token: " + token);
        
        ModelAndView mav = new ModelAndView();
        
        if (token == null || token.isEmpty()) {
            mav.setViewName("redirect:/index.html");
            return mav;
        }
        
        Map<String, String> response = authService.validateToken(token, "PATIENT");
        if (response.containsKey("error")) {
            mav.setViewName("redirect:/index.html");
            return mav;
        }
        
        mav.setViewName("patient/patientDashboard");
        mav.addObject("username", response.get("username"));
        mav.addObject("role", "PATIENT");
        return mav;
    }
}