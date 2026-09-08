package com.example.retail.controller;

import com.example.retail.dto.ProdusRequest;
import com.example.retail.dto.ProdusResponse;
import com.example.retail.model.Produs;
import com.example.retail.service.ProdusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/** JSON API for products. HTML pages are served by {@link ProdusController}. */
@RestController
@RequestMapping("/api/produse")
public class ProdusRestController {

    private final ProdusService produsService;

    public ProdusRestController(ProdusService produsService) {
        this.produsService = produsService;
    }

    @GetMapping
    public List<ProdusResponse> listaProduse() {
        return produsService.listaProduse().stream()
                .map(ProdusResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ProdusResponse obtineProdus(@PathVariable Long id) {
        return new ProdusResponse(produsService.obtineProdus(id));
    }

    @PostMapping
    public ResponseEntity<ProdusResponse> creeazaProdus(@Valid @RequestBody ProdusRequest request) {
        Produs produs = toEntity(request);
        Produs salvat = produsService.salveazaProdus(produs);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProdusResponse(salvat));
    }

    @PutMapping("/{id}")
    public ProdusResponse actualizeazaProdus(@PathVariable Long id,
                                             @Valid @RequestBody ProdusRequest request) {
        Produs actualizat = produsService.actualizeazaProdus(id, toEntity(request));
        return new ProdusResponse(actualizat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> stergeProdus(@PathVariable Long id) {
        produsService.stergeProdus(id);
        return ResponseEntity.noContent().build();
    }

    private static Produs toEntity(ProdusRequest request) {
        Produs produs = new Produs(
                request.getNume(),
                request.getCategorie(),
                request.getPret(),
                request.getCantitateStoc(),
                request.getCodEan()
        );
        produs.setVersion(request.getVersion());
        return produs;
    }
}
