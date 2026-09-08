package com.example.retail.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String acasa() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
