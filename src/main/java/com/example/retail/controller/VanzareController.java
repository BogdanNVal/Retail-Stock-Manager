package com.example.retail.controller;

import com.example.retail.model.Bon;
import com.example.retail.service.PdfBonService;
import com.example.retail.service.PdfGenerationException;
import com.example.retail.service.ProdusService;
import com.example.retail.service.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/casa-de-marcat")
public class VanzareController {

    private final ProdusService produsService;
    private final PdfBonService pdfBonService;

    public VanzareController(ProdusService produsService, PdfBonService pdfBonService) {
        this.produsService = produsService;
        this.pdfBonService = pdfBonService;
    }

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("produse", produsService.listaProduse());
        return "casa-marcat";
    }

    @GetMapping("/{id}")
    public String veziBon(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("bon", produsService.obtineBon(id));
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("eroare", ex.getMessage());
            return "redirect:/casa-de-marcat";
        }
        model.addAttribute("produse", produsService.listaProduse());
        return "casa-marcat";
    }

    @PostMapping("/vinde")
    public String vinde(@RequestParam("produsId") List<Long> produsIds,
                        @RequestParam("cantitate") List<Integer> cantitati,
                        RedirectAttributes redirectAttributes) {
        try {
            Bon bon = produsService.inregistreazaBon(produsIds, cantitati);
            return "redirect:/casa-de-marcat/" + bon.getId();
        } catch (IllegalArgumentException | IllegalStateException | ResourceNotFoundException
                 | ObjectOptimisticLockingFailureException ex) {
            String mesaj = ex instanceof ObjectOptimisticLockingFailureException
                    ? "Stocul a fost modificat intre timp. Reincearca vanzarea."
                    : ex.getMessage();
            redirectAttributes.addFlashAttribute("eroare", mesaj);
            return "redirect:/casa-de-marcat";
        }
    }

    @GetMapping("/{id}/bon-pdf")
    @ResponseBody
    public ResponseEntity<?> descarcaBonPdf(@PathVariable Long id) {
        try {
            Bon bon = produsService.obtineBon(id);
            byte[] pdf = pdfBonService.genereazaBonVanzare(bon);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bon-" + id + ".pdf")
                    .body(pdf);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (PdfGenerationException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Nu s-a putut genera bonul PDF.");
        }
    }
}
