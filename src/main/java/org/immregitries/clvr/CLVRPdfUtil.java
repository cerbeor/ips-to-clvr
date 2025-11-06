package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang3.StringUtils;
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

public class CLVRPdfUtil {

    public static PDDocument createPdf(CLVRPayload payload, byte[] qrCode, String creator) throws IOException, WriterException {
        ObjectMapper objectMapper = new ObjectMapper();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDDocumentInformation pdDocumentInformation = new PDDocumentInformation();
        document.setDocumentInformation(pdDocumentInformation);
        pdDocumentInformation.setCreator(creator);
        pdDocumentInformation.setCustomMetadataValue("evc", objectMapper.writeValueAsString(payload));
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
        contentStream.showText(creator + "test IPS to EVC");
        contentStream.newLine();
        contentStream.showText("Patient Information for " +
                payload.getName().getFamilyName() +
                ", " +
                payload.getName().getGivenName());
        contentStream.newLine();
        contentStream.showText("Identifier: " + payload.getPersonIdentifier().getObjectIdentifier() + " " + payload.getPersonIdentifier().getId());
        contentStream.newLine();
        contentStream.showText(new String(qrCode));
//		contentStream.showText(clvrPayload.getName().getGivenName());
        contentStream.endText();
        contentStream.close();

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
