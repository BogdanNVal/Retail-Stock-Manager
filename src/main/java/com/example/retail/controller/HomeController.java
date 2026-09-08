package com.example.retail.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


 /// Pagina de start
@Controller
public class HomeController {

    // Citim URL-ul de conexiune folosit de aplicatie (H2, MySQL, sau PostgreSQL),
    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @GetMapping("/")
    public String acasa(Model model) {
        String dbKind = "mysql";
        if (datasourceUrl != null) {
            if (datasourceUrl.contains("h2:")) {
                dbKind = "h2";
            } else if (datasourceUrl.contains("postgres")) {
                dbKind = "postgres";
            }
        }
        model.addAttribute("dbKind", dbKind);
        model.addAttribute("h2Activ", "h2".equals(dbKind));
        return "home";
    }
}
