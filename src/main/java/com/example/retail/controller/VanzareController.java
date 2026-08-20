package com.example.retail.controller;

import com.example.retail.model.Bon;
import com.example.retail.service.PdfBonService;
import com.example.retail.service.ProdusService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        return "casa-marcat"; // -> WEB-INF/jsp/casa-marcat.jsp
    }


    @PostMapping("/vinde")
    public String vinde(@RequestParam("produsId") List<Long> produsIds,
                         @RequestParam("cantitate") List<Integer> cantitati,
                         Model model) {
        try {
            model.addAttribute("bon", produsService.inregistreazaBon(produsIds, cantitati));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // ex: stoc insuficient la una din linii, produs inexistent, cantitate invalida
            model.addAttribute("eroare", ex.getMessage());
        }
        model.addAttribute("produse", produsService.listaProduse());
        return "casa-marcat";
    }

    @GetMapping("/{id}/bon-pdf")
    @ResponseBody
    public ResponseEntity<byte[]> descarcaBonPdf(@PathVariable Long id) {
        Bon bon = produsService.obtineBon(id);
        byte[] pdf = pdfBonService.genereazaBonVanzare(bon);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bon-" + id + ".pdf")
                .body(pdf);
    }
}
