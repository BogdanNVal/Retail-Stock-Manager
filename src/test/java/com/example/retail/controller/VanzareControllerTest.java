package com.example.retail.controller;

import com.example.retail.model.Bon;
import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.model.Vanzare;
import com.example.retail.security.SecurityConfig;
import com.example.retail.service.PdfBonService;
import com.example.retail.service.ProdusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VanzareController.class)
@Import(SecurityConfig.class)
class VanzareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdusService produsService;

    @MockBean
    private PdfBonService pdfBonService;

    @Test
    void pagina_faraAutentificare_redirectionareLaLogin() throws Exception {
        mockMvc.perform(get("/casa-de-marcat"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    void vinde_cuDateValide_redirectioneazaLaBon() throws Exception {
        Produs paine = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.TEN, 20, "12345670");
        paine.setId(1L);

        Bon bon = new Bon();
        bon.setId(42L);
        bon.adaugaLinie(new Vanzare(paine, 2, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(20)));

        when(produsService.inregistreazaBon(anyList(), anyList())).thenReturn(bon);

        mockMvc.perform(post("/casa-de-marcat/vinde")
                        .with(csrf())
                        .param("produsId", "1")
                        .param("cantitate", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/casa-de-marcat/42"));
    }

    @Test
    @WithMockUser
    void vinde_cuStocInsuficient_redirectioneazaCuEroare() throws Exception {
        when(produsService.inregistreazaBon(anyList(), anyList()))
                .thenThrow(new IllegalStateException("Stoc insuficient pentru Paine"));

        mockMvc.perform(post("/casa-de-marcat/vinde")
                        .with(csrf())
                        .param("produsId", "1")
                        .param("cantitate", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/casa-de-marcat"))
                .andExpect(flash().attributeExists("eroare"));
    }

    @Test
    @WithMockUser
    void veziBon_afiseazaBonul() throws Exception {
        Produs paine = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.TEN, 20, "12345670");
        paine.setId(1L);
        Bon bon = new Bon();
        bon.setId(7L);
        bon.adaugaLinie(new Vanzare(paine, 1, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN));

        when(produsService.obtineBon(7L)).thenReturn(bon);
        when(produsService.listaProduse()).thenReturn(List.of(paine));

        mockMvc.perform(get("/casa-de-marcat/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("casa-marcat"))
                .andExpect(model().attributeExists("bon"));
    }
}
