package com.example.retail.controller;

import com.example.retail.dto.ProdusRequest;
import com.example.retail.dto.ProdusResponse;
import com.example.retail.model.Produs;
import com.example.retail.service.ProdusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API pentru gestiunea produselor (raspunde in JSON).
 * Separat de ProdusController, care serveste pagini JSP pentru interfata web.
 */
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
        Produs produs = new Produs(
                request.getNume(),
                request.getCategorie(),
                request.getPret(),
                request.getCantitateStoc(),
                request.getCodEan()
        );
        Produs salvat = produsService.salveazaProdus(produs);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProdusResponse(salvat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> stergeProdus(@PathVariable Long id) {
        produsService.stergeProdus(id);
        return ResponseEntity.noContent().build();
    }

    /// Erorile de validare (cod EAN invalid, produs inexistent) transformate intr-un raspuns JSON cu status HTTP
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> trateazaEroareValidare(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

     /// Erorile de validare @Valid (nume gol, pret negativ etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> trateazaEroareValidareCamp(MethodArgumentNotValidException ex) {
        String mesaj = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(mesaj);
    }
}
