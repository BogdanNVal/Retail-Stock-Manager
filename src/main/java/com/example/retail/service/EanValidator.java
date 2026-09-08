package com.example.retail.service;

import org.springframework.stereotype.Component;

@Component
public class EanValidator {

    public boolean esteValid(String cod) {
        if (cod == null || (cod.length() != 8 && cod.length() != 13)) {
            return false;
        }
        if (!cod.chars().allMatch(Character::isDigit)) {
            return false;
        }
        int cifraControlCalculata = calculeazaCifraControl(cod.substring(0, cod.length() - 1));
        int cifraControlDinCod = Character.getNumericValue(cod.charAt(cod.length() - 1));
        return cifraControlCalculata == cifraControlDinCod;
    }

    /**
     * Calculeaza cifra de control pentru un cod EAN fara ultima cifra.
     * Regula standard: suma ponderata (alternanta 1x/3x) a cifrelor, apoi
     * rotunjire in sus la urmatorul multiplu de 10.
     */
    public int calculeazaCifraControl(String codFaraControl) {
        int suma = 0;
        int lungime = codFaraControl.length();
        for (int i = 0; i < lungime; i++) {
            int cifra = Character.getNumericValue(codFaraControl.charAt(i));
            // Pozitiile numarate de la dreapta la stanga alterneaza ponderea 3 / 1
            boolean estePonderata = (lungime - i) % 2 != 0;
            suma += estePonderata ? cifra * 3 : cifra;
        }
        int rest = suma % 10;
        return rest == 0 ? 0 : 10 - rest;
    }
}
