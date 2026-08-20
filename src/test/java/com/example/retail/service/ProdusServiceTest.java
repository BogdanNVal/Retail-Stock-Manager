package com.example.retail.service;

import com.example.retail.model.Bon;
import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.model.Vanzare;
import com.example.retail.repository.BonRepository;
import com.example.retail.repository.ProdusRepository;
import com.example.retail.repository.VanzareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdusServiceTest {

    @Mock
    private ProdusRepository produsRepository;

    @Mock
    private VanzareRepository vanzareRepository;

    @Mock
    private BonRepository bonRepository;

    private ProdusService produsService;

    @BeforeEach
    void setUp() {
        produsService = new ProdusService(
                produsRepository,
                vanzareRepository,
                bonRepository,
                new EanValidator(),
                new AlimentarDiscountStrategy(),
                new NealimentarDiscountStrategy()
        );
    }

    @Test
    void salveazaProdus_cuCodEanInvalid_aruncaExceptie() {
        Produs produs = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.valueOf(5), 10, "12345678");

        IllegalArgumentException exceptie = assertThrows(IllegalArgumentException.class,
                () -> produsService.salveazaProdus(produs));

        assertTrue(exceptie.getMessage().contains("Cod EAN invalid"));
        verify(produsRepository, never()).save(any());
    }

    @Test
    void salveazaProdus_cuCodEanValid_esteSalvat() {
        Produs produs = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.valueOf(5), 10, "12345670");
        when(produsRepository.save(produs)).thenReturn(produs);

        Produs rezultat = produsService.salveazaProdus(produs);

        assertEquals(produs, rezultat);
        verify(produsRepository).save(produs);
    }

    @Test
    void inregistreazaVanzare_cuStocInsuficient_aruncaExceptie() {
        Produs produs = new Produs("Lapte", Categorie.ALIMENTAR, BigDecimal.valueOf(10), 2, "12345670");
        produs.setId(1L);
        when(produsRepository.findById(1L)).thenReturn(Optional.of(produs));

        assertThrows(IllegalStateException.class,
                () -> produsService.inregistreazaVanzare(1L, 5));

        verify(vanzareRepository, never()).save(any());
    }

    @Test
    void inregistreazaVanzare_produsAlimentar_cuCantitatePesteLimita_aplicaDiscount() {
        Produs produs = new Produs("Ulei", Categorie.ALIMENTAR, BigDecimal.valueOf(10), 20, "12345670");
        produs.setId(1L);
        when(produsRepository.findById(1L)).thenReturn(Optional.of(produs));
        when(vanzareRepository.save(any(Vanzare.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vanzare vanzare = produsService.inregistreazaVanzare(1L, 5);

        assertEquals(0, BigDecimal.valueOf(47.5).compareTo(vanzare.getTotalCuDiscount()));
        assertEquals(15, produs.getCantitateStoc());
        verify(produsRepository).save(produs);
    }

    @Test
    void inregistreazaVanzare_produsInexistent_aruncaExceptie() {
        when(produsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> produsService.inregistreazaVanzare(99L, 1));
    }

    @Test
    void inregistreazaBon_cuProduseDiferite_calculeazaTotalurileSiDiscountul() {
        Produs paine = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.valueOf(10), 20, "12345670");
        paine.setId(1L);
        Produs pix = new Produs("Pix", Categorie.NEALIMENTAR, BigDecimal.valueOf(5), 20, "12345671");
        pix.setId(2L);

        when(produsRepository.findById(1L)).thenReturn(Optional.of(paine));
        when(produsRepository.findById(2L)).thenReturn(Optional.of(pix));
        when(bonRepository.save(any(Bon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bon bon = produsService.inregistreazaBon(List.of(1L, 2L), List.of(5, 3));

        assertEquals(2, bon.getLinii().size());
        assertEquals(0, BigDecimal.valueOf(65).compareTo(bon.getTotalFaraDiscount()));
        assertEquals(0, BigDecimal.valueOf(61).compareTo(bon.getTotalCuDiscount()));
        assertEquals(0, BigDecimal.valueOf(4).compareTo(bon.getTotalDiscount()));
        assertEquals(15, paine.getCantitateStoc());
        assertEquals(17, pix.getCantitateStoc());
    }

    @Test
    void inregistreazaBon_cuListePeGoale_aruncaExceptie() {
        assertThrows(IllegalArgumentException.class,
                () -> produsService.inregistreazaBon(List.of(), List.of()));
    }

    @Test
    void inregistreazaBon_cuAcelasiProdusPeMaiMulteLinii_leCombinaSiAplicaDiscountulCorect() {
        Produs paine = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.valueOf(10), 20, "12345670");
        paine.setId(1L);

        when(produsRepository.findById(1L)).thenReturn(Optional.of(paine));
        when(bonRepository.save(any(Bon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bon bon = produsService.inregistreazaBon(List.of(1L, 1L), List.of(2, 4));

        assertEquals(1, bon.getLinii().size());
        assertEquals(6, bon.getLinii().get(0).getCantitate());

        assertEquals(0, BigDecimal.valueOf(57).compareTo(bon.getTotalCuDiscount()));
        assertEquals(14, paine.getCantitateStoc());
    }

    @Test
    void stergeProdus_cuVanzariAsociate_aruncaExceptieSiNuSterge() {
        when(vanzareRepository.existsByProdusId(1L)).thenReturn(true);

        IllegalStateException exceptie = assertThrows(IllegalStateException.class,
                () -> produsService.stergeProdus(1L));

        assertTrue(exceptie.getMessage().contains("vanzari"));
        verify(produsRepository, never()).deleteById(any());
    }

    @Test
    void stergeProdus_faraVanzariAsociate_esteSters() {
        when(vanzareRepository.existsByProdusId(1L)).thenReturn(false);

        produsService.stergeProdus(1L);

        verify(produsRepository).deleteById(1L);
    }
}
