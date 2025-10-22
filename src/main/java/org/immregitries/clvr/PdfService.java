package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.immregitries.clvr.model.CLVRPayload;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class PdfService {

    public static PDDocument createPdf(CLVRPayload CLVRPayload, byte[] qrCode) throws IOException, WriterException {
        ObjectMapper objectMapper = new ObjectMapper();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDDocumentInformation pdDocumentInformation = new PDDocumentInformation();
        document.setDocumentInformation(pdDocumentInformation);
        pdDocumentInformation.setCreator("IIS SANDBOX");
        pdDocumentInformation.setCustomMetadataValue("evc", objectMapper.writeValueAsString(CLVRPayload));
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        PDImageXObject qrCodeImageObject;
        {
            int width = 300; // Desired QR code width
            int height = 300; // Desired QR code height
            BitMatrix bitMatrix = CompressionUtil.qrCodeBitMatrix(new String(qrCode), width, height);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            qrCodeImageObject = LosslessFactory.createFromImage(document, bufferedImage);
        }
        contentStream.drawImage(qrCodeImageObject, 150, 150);


        // Creating Paragraph object
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 12);
        contentStream.beginText();
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(25, 700);
        contentStream.showText("IIS Sandbox Test IPS to EVC");
        contentStream.newLine();
        contentStream.showText("Patient Information for " +
                CLVRPayload.getName().getFamilyName() +
                ", " +
                CLVRPayload.getName().getGivenName());
        contentStream.newLine();
        contentStream.showText("Identifier: " + CLVRPayload.getPersonIdentifier().getObjectIdentifier() + " " + CLVRPayload.getPersonIdentifier().getId());
        contentStream.newLine();
        contentStream.showText(new String(qrCode));
//		contentStream.showText(clvrPayload.getName().getGivenName());
        contentStream.endText();
        contentStream.close();

        return document;
    }

    protected void printPdf(
            PDDocument pdDocument,
            String name
    ) throws IOException {
		pdDocument.save(name + ".pdf");
        pdDocument.close();
    }

}
