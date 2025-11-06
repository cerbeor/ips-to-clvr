package org.immregitries.clvr;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import javax.imageio.ImageIO;

public class VaccinationSummaryCard {

    public static void main(String[] args) throws Exception {
        new VaccinationSummaryCard().generatePDF("Vaccination_Summary.pdf");
        System.out.println("✅ Vaccination summary card generated: Vaccination_Summary.pdf");
    }

    public void generatePDF(String outputFileName) throws IOException, WriterException {
        // Create a new document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);

        float margin = 50;
        float yStart = page.getMediaBox().getHeight() - margin;

        // Header
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
        content.newLineAtOffset(margin, yStart);
        content.showText("Vaccination History Summary");
        content.endText();

        yStart -= 40;

        // Patient Info
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        content.newLineAtOffset(margin, yStart);
        content.showText("Name: John Doe");
        content.newLineAtOffset(0, -15);
        content.showText("Date of Birth: 1990-04-23");
        content.newLineAtOffset(0, -15);
        content.showText("Patient ID: 12345678");
        content.endText();

        yStart -= 70;

        // Table Header
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        content.newLineAtOffset(margin, yStart);
        content.showText("Vaccine");
        content.newLineAtOffset(150, 0);
        content.showText("Date");
        content.newLineAtOffset(150, 0);
        content.showText("Dose");
        content.endText();

        yStart -= 20;

        // Example Vaccination Data
        List<String[]> vaccinations = Arrays.asList(
                new String[]{"COVID-19", "2021-03-12", "1st Dose"},
                new String[]{"COVID-19", "2021-04-09", "2nd Dose"},
                new String[]{"Influenza", "2024-10-10", "Annual"},
                new String[]{"Tetanus", "2023-07-15", "Booster"}
        );

        // Draw table rows
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        for (String[] record : vaccinations) {
            content.beginText();
            content.newLineAtOffset(margin, yStart);
            content.showText(record[0]);
            content.newLineAtOffset(150, 0);
            content.showText(record[1]);
            content.newLineAtOffset(150, 0);
            content.showText(record[2]);
            content.endText();
            yStart -= 20;
        }

        // Generate QR Code
        String qrData = "PatientID:12345678; Name:John Doe; Last Updated:" + LocalDate.now();
        BufferedImage qrImage = generateQRCodeImage(qrData, 150, 150);

        // Save QR to temporary file
        File qrTemp = new File("qr_temp.png");
        ImageIO.write(qrImage, "png", qrTemp);

        // Add QR code to PDF (bottom center)
        PDImageXObject pdImage = PDImageXObject.createFromFileByContent(qrTemp, document);
        float qrX = (page.getMediaBox().getWidth() - 150) / 2;
        float qrY = 50;
        content.drawImage(pdImage, qrX, qrY, 150, 150);

        // Footer text
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
        content.newLineAtOffset(margin, 30);
        content.showText("Generated on: " + LocalDate.now());
        content.endText();

        // Close everything
        content.close();
        document.save(outputFileName);
        document.close();

        // Cleanup
        qrTemp.delete();
    }

    // Helper: Generate QR code as BufferedImage
    private BufferedImage generateQRCodeImage(String data, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}
