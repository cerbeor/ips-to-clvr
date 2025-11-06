package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syadem.nuva.Vaccine;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.VaccinationRecord;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class VaccinationSummaryCard {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private NUVAService nuvaService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public VaccinationSummaryCard(NUVAService nuvaService) {
        this.nuvaService = nuvaService;
    }

    public PDDocument generatePDF(CLVRPayload payload, byte[] qrCode, String creator) throws IOException, WriterException {
        // Create a new document
        PDDocument document = new PDDocument();

        PDDocumentInformation pdDocumentInformation = new PDDocumentInformation();
        document.setDocumentInformation(pdDocumentInformation);
        pdDocumentInformation.setCreator(creator);
        pdDocumentInformation.setCustomMetadataValue("evc", objectMapper.writeValueAsString(payload));
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
        content.showText("Name: " + payload.getName().getGivenName() + " " + payload.getName().getFamilyName());
        content.newLineAtOffset(0, -15);
        content.showText("Date of Birth: " + simpleDateFormat.format(payload.getDateOfBirth()));
//        content.newLineAtOffset(0, -15);
//        if (payload.getPersonIdentifier().)
//        content.showText("Patient ID: " + payload.getPersonIdentifier());
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
        content.showText("Country");
        content.endText();

        yStart -= 20;

        // Draw table rows
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        for (VaccinationRecord record : payload.getVaccinationRecords()) {
            Vaccine vaccine = nuvaService.getNuva().getQueries().lookupVaccineByCode(record.getNuvaCode());
            content.beginText();
            content.newLineAtOffset(margin, yStart);
            content.showText(vaccine.getName());
            content.newLineAtOffset(150, 0);
            Date adminDate = new Date(TimeUnit.DAYS.toMillis(record.getAgeInDays()) + payload.getDateOfBirth().getTime());
            content.showText(simpleDateFormat.format(adminDate));
            content.newLineAtOffset(150, 0);
            content.showText(record.getRegistryCode());
            content.endText();
            yStart -= 20;
        }

        PDImageXObject qrCodeImageObject;
        {
            int width = 300; // Desired QR code width
            int height = 300; // Desired QR code height
            BitMatrix bitMatrix = CompressionUtil.qrCodeBitMatrix(new String(qrCode), width, height);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            qrCodeImageObject = LosslessFactory.createFromImage(document, bufferedImage);
        }
        content.drawImage(qrCodeImageObject, 150, 150);

        // Footer text
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
        content.newLineAtOffset(margin, 30);
        content.showText("Generated on: " + LocalDate.now());
        content.endText();

        // Close everything
        content.close();
        return document;
    }

    // Helper: Generate QR code as BufferedImage
    private BufferedImage generateQRCodeImage(String data, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}
