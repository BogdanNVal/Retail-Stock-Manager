package com.example.retail.service;

import com.example.retail.config.AppConfigSingleton;
import com.example.retail.model.Bon;
import com.example.retail.model.Vanzare;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;


 ///Genereaza un bon de vanzare in format PDF, folosind libraria iText.

@Service
public class PdfBonService {

    private static final DateTimeFormatter FORMAT_DATA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public byte[] genereazaBonVanzare(Bon bon) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            String numeMagazin = AppConfigSingleton.getInstance().getNumeMagazin();

            document.add(new Paragraph(numeMagazin).setBold().setFontSize(16));
            document.add(new Paragraph("Bon de vanzare #" + bon.getId()));
            document.add(new Paragraph("Data: " + bon.getDataBon().format(FORMAT_DATA)));
            document.add(new Paragraph(" "));

            Table tabel = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1.2f, 1, 1.2f}))
                    .useAllAvailableWidth();
            tabel.addHeaderCell("Produs");
            tabel.addHeaderCell("Cantitate");
            tabel.addHeaderCell("Total fara discount");
            tabel.addHeaderCell("Discount");
            tabel.addHeaderCell("Total cu discount");

            for (Vanzare linie : bon.getLinii()) {
                tabel.addCell(linie.getProdus().getNume());
                tabel.addCell(String.valueOf(linie.getCantitate()));
                tabel.addCell(linie.getTotalFaraDiscount() + " lei");
                tabel.addCell(linie.getDiscountValoare() + " lei");
                tabel.addCell(linie.getTotalCuDiscount() + " lei");
            }

            document.add(tabel);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Subtotal: " + bon.getTotalFaraDiscount() + " lei"));
            document.add(new Paragraph("Discount total: " + bon.getTotalDiscount() + " lei"));
            document.add(new Paragraph("Total de plata: " + bon.getTotalCuDiscount() + " lei").setBold());
        } catch (IOException e) {
            throw new PdfGenerationException("Nu s-a putut genera bonul PDF pentru bonul #" + bon.getId(), e);
        }

        return outputStream.toByteArray();
    }
}
