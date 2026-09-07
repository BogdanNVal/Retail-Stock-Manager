package com.example.retail.controller;

import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.service.ProdusService;
import com.example.retail.service.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
        return "produse-list";
    }

    @GetMapping("/nou")
    public String formularProdusNou(Model model) {
        model.addAttribute("produs", new Produs());
        model.addAttribute("categorii", Categorie.values());
        model.addAttribute("titluFormular", "Adauga produs");
        return "produs-form";
    }

    @GetMapping("/{id}/editeaza")
    public String formularEditare(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("produs", produsService.obtineProdus(id));
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("eroareStergere", ex.getMessage());
            return "redirect:/produse";
        }
        model.addAttribute("categorii", Categorie.values());
        model.addAttribute("titluFormular", "Editeaza produs");
        return "produs-form";
    }

    @PostMapping("/salveaza")
    public String salveazaProdus(@Valid @ModelAttribute Produs produs, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return reafiseazaFormular(produs, model, extrageMesajeEroare(bindingResult));
        }

        try {
            if (produs.getId() == null) {
                produsService.salveazaProdus(produs);
            } else {
                produsService.actualizeazaProdus(produs.getId(), produs);
            }
        } catch (IllegalArgumentException | ObjectOptimisticLockingFailureException
                 | DataIntegrityViolationException | ResourceNotFoundException ex) {
            String mesaj;
            if (ex instanceof ObjectOptimisticLockingFailureException) {
                mesaj = "Datele au fost modificate de alt utilizator. Reincarca formularul si incearca din nou.";
            } else if (ex instanceof DataIntegrityViolationException) {
                mesaj = "Conflict de date (de exemplu cod EAN duplicat).";
            } else {
                mesaj = ex.getMessage();
            }
            return reafiseazaFormular(produs, model, List.of(mesaj));
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

    private String reafiseazaFormular(Produs produs, Model model, List<String> erori) {
        model.addAttribute("categorii", Categorie.values());
        model.addAttribute("erori", erori);
        model.addAttribute("titluFormular", produs.getId() == null ? "Adauga produs" : "Editeaza produs");
        return "produs-form";
    }

    private List<String> extrageMesajeEroare(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
    }
}
