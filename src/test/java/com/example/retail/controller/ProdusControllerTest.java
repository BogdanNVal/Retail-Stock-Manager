package com.example.retail.controller;

import com.example.retail.model.Produs;
import com.example.retail.security.SecurityConfig;
import com.example.retail.service.ProdusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdusController.class)
@Import(SecurityConfig.class)
class ProdusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdusService produsService;

    @Test
    @WithMockUser
    void salveazaProdus_cuNumeGol_returneazaFormularulCuErori() throws Exception {
        mockMvc.perform(post("/produse/salveaza")
                        .with(csrf())
                        .param("nume", "")
                        .param("categorie", "ALIMENTAR")
                        .param("pret", "10")
                        .param("cantitateStoc", "5")
                        .param("codEan", "12345670"))
                .andExpect(status().isOk())
                .andExpect(view().name("produs-form"))
                .andExpect(model().attributeExists("erori"));

        verify(produsService, never()).salveazaProdus(any());
    }

    @Test
    @WithMockUser
    void salveazaProdus_cuCodEanInvalid_afiseazaEroareaDeLaService() throws Exception {
        when(produsService.salveazaProdus(any()))
                .thenThrow(new IllegalArgumentException("Cod EAN invalid: 123"));

        mockMvc.perform(post("/produse/salveaza")
                        .with(csrf())
                        .param("nume", "Paine")
                        .param("categorie", "ALIMENTAR")
                        .param("pret", "10")
                        .param("cantitateStoc", "5")
                        .param("codEan", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("produs-form"))
                .andExpect(model().attributeExists("erori"));
    }

    @Test
    @WithMockUser
    void salveazaProdus_cuDateValide_redirectioneazaLaLista() throws Exception {
        when(produsService.salveazaProdus(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/produse/salveaza")
                        .with(csrf())
                        .param("nume", "Paine")
                        .param("categorie", "ALIMENTAR")
                        .param("pret", "10")
                        .param("cantitateStoc", "5")
                        .param("codEan", "12345670"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produse"));

        verify(produsService).salveazaProdus(any(Produs.class));
    }
}
