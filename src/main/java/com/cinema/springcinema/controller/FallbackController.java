package com.cinema.springcinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FallbackController {

    @GetMapping(value = {"/movies/**", "/tickets", "/login", "/register", "/checkout", "/screenings/**", "/chatbot"})
    public String forward() {
        return "forward:/index.html";
    }
}