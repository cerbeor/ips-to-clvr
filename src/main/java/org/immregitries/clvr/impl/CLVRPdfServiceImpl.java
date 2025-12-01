package org.immregitries.clvr.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.immregitries.clvr.CLVRPdfService;
import org.immregitries.clvr.CompressionUtil;
import org.immregitries.clvr.NUVAService;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;
import org.immregitries.clvr.model.CLVRVaccinationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CLVRPdfServiceImpl implements CLVRPdfService {
    public static final float TRANSPARENCY_HIGH = 1f;
    public static final float TRANSPARENCY_LOW = 0.6f;
    public static final String PDF_TEMPLATE_PDF = "src/main/resources/pdf_template.pdf";
    public static final String CLVR_PNG = "src/main/resources/clvr.png";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final NUVAService nuvaService;


    public CLVRPdfServiceImpl(NUVAService nuvaService) {
        this.nuvaService = nuvaService;
    }

    private static void printLabels(PDPageContentStream content, String english, String french, String spanish, int fontSize, PDFont font1, PDFont font2) throws IOException {
        content.setFont(font1, fontSize);
        content.showText(english + " ");
        content.setFont(font2, fontSize);
        content.setStrokingColor(CLVRPdfServiceImpl.TRANSPARENCY_LOW);
        content.showText("(" + french + "/");
        content.showText(spanish + ")");
        content.setStrokingColor(CLVRPdfServiceImpl.TRANSPARENCY_HIGH);
        content.setFont(font1, fontSize);
    }

    protected static void printPdf(
            PDDocument pdDocument,
            String name
    ) throws IOException {
        String fileName = StringUtils.substringBefore(name, ".");
        pdDocument.save(fileName + ".pdf");
        pdDocument.close();
    }

    @Override
    public PDDocument createPdf(CLVRToken token, byte[] qrCode, String pdfCreator) throws IOException {
        CLVRPayload payload = token.getClvrPayload();

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
        pdDocumentInformation.setCreator(pdfCreator);
        pdDocumentInformation.setCustomMetadataValue("evc", objectMapper.writeValueAsString(payload));


        float margin = 30;
        float langageMargin = -15;
        float yStart = page.getMediaBox().getHeight() - margin * 2;

        PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font oblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        PDType1Font boldOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);

        // Sub Header
        int subheaderSize = 10;
        content.beginText();
        content.newLineAtOffset(page.getMediaBox().getWidth() / 2 + margin * 3, page.getMediaBox().getHeight() - margin);
        printLabels(content, "Issued on", "Publié le", "Emitido el", subheaderSize, normalFont, oblique);
        content.setFont(bold, subheaderSize);
        content.showText(": " + simpleDateFormat.format(new Date(token.getIssuedTime() * 1000L)));

        content.newLineAtOffset(0, -15);
        printLabels(content, "Issuer", "Source", "Emisor", subheaderSize, normalFont, oblique);
        content.setFont(bold, subheaderSize);
        content.showText(": "  + token.getIssuer());

        content.newLineAtOffset(0, -15);
        printLabels(content, "Expiring", "Expiration", "Expira el", subheaderSize, normalFont, oblique);
        content.setFont(bold, subheaderSize);
        content.showText(": " + simpleDateFormat.format(new Date(token.getExpirationTime() * 1000L)));
        content.endText();

        // Header
        content.beginText();
        content.setFont(bold, 20);
        content.newLineAtOffset(margin, yStart);
        content.showText("Vaccination Summary");
        content.newLineAtOffset(10, langageMargin);
        content.setFont(oblique, 16);
        content.setStrokingColor(TRANSPARENCY_LOW);

        content.showText("Historique Vaccinal");
        content.newLineAtOffset(0, langageMargin);
        content.showText("Historial de vacunación");
        content.endText();
        content.setStrokingColor(TRANSPARENCY_HIGH);


        yStart -= 60;


        // Patient Info
        content.beginText();
        content.setFont(normalFont, 12);
        content.newLineAtOffset(margin, yStart);
        printLabels(content, "First Name", "Prénom", "Nombre", 12, normalFont, oblique);
        content.setFont(bold, 12);
        content.setStrokingColor(TRANSPARENCY_HIGH);
        content.showText(": " + payload.getName().getGivenName());
        content.setFont(normalFont, 12);

        content.newLineAtOffset(0, -15);
        printLabels(content, "Last Name", "Nom de Famille", "Apellido", 12, normalFont, oblique);
        content.setFont(bold, 12);
        content.showText(": " + payload.getName().getFamilyName());
        content.setFont(normalFont, 12);


        content.newLineAtOffset(0, -15);
        printLabels(content, "Date of Birth", "Date de Naissance", "Cumpleaños", 12, normalFont, oblique);
        content.setFont(bold, 12);
        content.showText(": " + simpleDateFormat.format(payload.getDateOfBirth()));
        content.setFont(normalFont, 12);

//        content.newLineAtOffset(0, -15);
//        if (payload.getPersonIdentifier().)
//        content.showText("Patient ID: " + payload.getPersonIdentifier());
        content.endText();

        yStart -= 70;

        // Table Header
        content.beginText();
        content.setFont(bold, 12);
        content.newLineAtOffset(margin, yStart);
        printLabels(content, "Vaccine", "Vaccin", "Vacuna", 12, normalFont, oblique);
        content.newLineAtOffset(150, 0);
        content.setFont(bold, 12);
        printLabels(content, "Date", "Date", "Fecha", 12, normalFont, oblique);
        content.newLineAtOffset(150, 0);
        content.setFont(bold, 12);
        printLabels(content, "Country", "Pays", "País", 12, normalFont, oblique);

        content.endText();

        yStart -= 20;

        // Draw table rows
        content.setFont(normalFont, 12);
        for (CLVRVaccinationRecord record : payload.getVaccinationRecords()) {
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
        int qr_width = 300; // Desired QR code width
        // Desired QR code height
        int qr_x = 150;
        int qr_y = 50;

        /*
        Printing black square before QR code to generate a border around the readable area
         */
        {
            int border_length = 2;
            BitMatrix black = new BitMatrix(qr_width + border_length * 2, qr_width + border_length * 2);
            black.flip();
            BufferedImage bufferedBlackImage = MatrixToImageWriter.toBufferedImage(black);
            PDImageXObject pdImageXObject = LosslessFactory.createFromImage(document, bufferedBlackImage);
            content.drawImage(pdImageXObject, qr_x - border_length, qr_y - border_length);
        }
        {
            BitMatrix bitMatrix = CompressionUtil.qrCodeBitMatrix(new String(qrCode), qr_width, qr_width);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            qrCodeImageObject = LosslessFactory.createFromImage(document, bufferedImage);
            content.drawImage(qrCodeImageObject, qr_x, qr_y);

        }

        // CLVR SVG
        PDImageXObject clvrLogoImage = PDImageXObject.createFromFile(CLVR_PNG, document);
        content.drawImage(clvrLogoImage, qr_x, qr_y + qr_width);

        // Footer text
        content.beginText();
        content.setFont(oblique, 10);
        content.newLineAtOffset(margin, 30);
        content.showText("PDF generated on: " + LocalDate.now() + " by " + pdfCreator);
        content.endText();

        // Close everything
        content.close();
        return document;
    }

}
