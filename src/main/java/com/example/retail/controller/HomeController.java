package com.example.retail.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


 /// Pagina de start
@Controller
public class HomeController {

    // Citim URL-ul de conexiune folosit de aplicatie (H2 sau MySQL),
    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @GetMapping("/")
    public String acasa(Model model) {
        boolean h2Activ = datasourceUrl != null && datasourceUrl.contains("h2:mem");
        model.addAttribute("h2Activ", h2Activ);
        return "home"; // -> WEB-INF/jsp/home.jsp
    }
}
