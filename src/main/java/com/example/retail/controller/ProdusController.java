package com.example.retail.controller;

import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.service.ProdusService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/produse")
public class ProdusController {

    private final ProdusService produsService;

    public ProdusController(ProdusService produsService) {
        this.produsService = produsService;
    }

    @GetMapping
    public String listaProduse(Model model) {
        model.addAttribute("produse", produsService.listaProduse());
        return "produse-list"; // -> WEB-INF/jsp/produse-list.jsp
    }

    @GetMapping("/nou")
    public String formularProdusNou(Model model) {
        model.addAttribute("produs", new Produs());
        model.addAttribute("categorii", Categorie.values());
        return "produs-form";
    }

    @PostMapping("/salveaza")
    public String salveazaProdus(@Valid @ModelAttribute Produs produs, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorii", Categorie.values());
            model.addAttribute("erori", extrageMesajeEroare(bindingResult));
            return "produs-form";
        }

        try {
            produsService.salveazaProdus(produs);
        } catch (IllegalArgumentException ex) {
            // cod EAN invalid (checksum) - validat in service
            model.addAttribute("categorii", Categorie.values());
            model.addAttribute("erori", List.of(ex.getMessage()));
            return "produs-form";
        }

        return "redirect:/produse";
    }

    @PostMapping("/{id}/sterge")
    public String stergeProdus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            produsService.stergeProdus(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("eroareStergere", ex.getMessage());
        }
        return "redirect:/produse";
    }

    private List<String> extrageMesajeEroare(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
    }
}
