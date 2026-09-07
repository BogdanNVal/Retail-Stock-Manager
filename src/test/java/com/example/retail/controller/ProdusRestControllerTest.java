package com.example.retail.controller;

import com.example.retail.dto.ProdusRequest;
import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.security.SecurityConfig;
import com.example.retail.service.ProdusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdusRestController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProdusRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdusService produsService;

    @Test
    void listaProduse_faraAuth_estePermisa() throws Exception {
        Produs produs = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.TEN, 5, "12345670");
        produs.setId(1L);
        when(produsService.listaProduse()).thenReturn(List.of(produs));

        mockMvc.perform(get("/api/produse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nume").value("Paine"));
    }

    @Test
    void creeazaProdus_faraAuth_esteInterzis() throws Exception {
        ProdusRequest request = requestValid();

        mockMvc.perform(post("/api/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void creeazaProdus_cuAuth_returneazaCreated() throws Exception {
        ProdusRequest request = requestValid();
        Produs salvat = new Produs(request.getNume(), request.getCategorie(),
                request.getPret(), request.getCantitateStoc(), request.getCodEan());
        salvat.setId(10L);
        when(produsService.salveazaProdus(any(Produs.class))).thenReturn(salvat);

        mockMvc.perform(post("/api/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizeazaProdus_returneazaProdusul() throws Exception {
        ProdusRequest request = requestValid();
        Produs actualizat = new Produs("Paine integrala", Categorie.ALIMENTAR,
                BigDecimal.valueOf(6), 12, "12345670");
        actualizat.setId(1L);
        when(produsService.actualizeazaProdus(eq(1L), any(Produs.class))).thenReturn(actualizat);

        mockMvc.perform(put("/api/produse/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nume").value("Paine integrala"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void stergeProdus_cuVanzari_returneazaConflict() throws Exception {
        doThrow(new IllegalStateException("Produsul nu poate fi sters")).when(produsService).stergeProdus(1L);

        mockMvc.perform(delete("/api/produse/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void creeazaProdus_cuHttpBasic_estePermis() throws Exception {
        ProdusRequest request = requestValid();
        Produs salvat = new Produs(request.getNume(), request.getCategorie(),
                request.getPret(), request.getCantitateStoc(), request.getCodEan());
        salvat.setId(3L);
        when(produsService.salveazaProdus(any(Produs.class))).thenReturn(salvat);

        mockMvc.perform(post("/api/produse")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(produsService).salveazaProdus(any(Produs.class));
    }

    private static ProdusRequest requestValid() {
        ProdusRequest request = new ProdusRequest();
        request.setNume("Paine");
        request.setCategorie(Categorie.ALIMENTAR);
        request.setPret(BigDecimal.valueOf(5));
        request.setCantitateStoc(20);
        request.setCodEan("12345670");
        return request;
    }
}
