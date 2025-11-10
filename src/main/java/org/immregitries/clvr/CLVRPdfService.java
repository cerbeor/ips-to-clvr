package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.syadem.nuva.Vaccine;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.VaccinationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CLVRPdfService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PDF_TEMPLATE_PDF = "src/test/resources/pdf_template.pdf";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private ObjectMapper objectMapper = new ObjectMapper();

    private NUVAService nuvaService;


    public CLVRPdfService(NUVAService nuvaService) {
        this.nuvaService = nuvaService;
    }


    public PDDocument createPdf(CLVRPayload payload, byte[] qrCode, String creator) throws IOException, WriterException {
        // Create a new document
        PDDocument document;
        PDPage page;
        PDPageContentStream content;

        try {
            document = Loader.loadPDF(new RandomAccessReadBufferedFile(PDF_TEMPLATE_PDF));
            page = document.getPage(0);
            content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false, true);

        } catch (IOException ioException) {
            document = new PDDocument();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);

            logger.info("PDF template not found for CLVR, using blank new document");
        }

        PDDocumentInformation pdDocumentInformation = new PDDocumentInformation();
        document.setDocumentInformation(pdDocumentInformation);
        pdDocumentInformation.setCreator(creator);
        pdDocumentInformation.setCustomMetadataValue("evc", objectMapper.writeValueAsString(payload));



        float margin = 50;
        float yStart = page.getMediaBox().getHeight() - margin;

        // Header
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
        content.newLineAtOffset(margin, yStart);
        content.showText("Vaccination History Summary - CLVR TEST");
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

    protected static void printPdf(
            PDDocument pdDocument,
            String name
    ) throws IOException {
        String fileName = StringUtils.substringBefore(name,".");
		pdDocument.save(fileName + ".pdf");
        pdDocument.close();
    }

}
