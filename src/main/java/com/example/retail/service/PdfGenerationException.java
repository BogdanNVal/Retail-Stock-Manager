package com.example.retail.service;


/// Exceptie unchecked aruncata cand generarea unui PDF esueaza.

public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
