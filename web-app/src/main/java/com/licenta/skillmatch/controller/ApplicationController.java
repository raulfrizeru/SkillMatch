package com.licenta.skillmatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {
    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard";
    }

    @GetMapping("/about")
    public String showAboutUsPage() {
        return "about";
    }

    @GetMapping("/login")
    public String ShowLoginForm(){
        return "login";
    }
}
