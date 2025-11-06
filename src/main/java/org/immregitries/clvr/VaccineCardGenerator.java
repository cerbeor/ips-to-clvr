package org.immregitries.clvr;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts; // Import the enumeration

import java.io.IOException;

public class VaccineCardGenerator {

    // Define coordinates and dimensions... (rest of the class variables remain the same)
    private static final PDRectangle PAGE_SIZE = PDRectangle.A6;
    private static final float MARGIN = 30;
    private static final float PAGE_WIDTH = PAGE_SIZE.getWidth();
    private static final float PAGE_HEIGHT = PAGE_SIZE.getHeight();
    private static float yPosition = PAGE_HEIGHT - MARGIN;
    private static final float LINE_HEIGHT = 15;


    public static void main(String[] args) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            // *** CORRECT: Use the proper PDPageContentStream constructor ***
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // --- 1. Draw Header ---
                drawHeader(contentStream);

                // ... (rest of the main method logic)

            } // contentStream is closed here

            CLVRPdfUtil.printPdf(document,"test-clvr-123456789");

            // ... (save document)

        } catch (IOException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
        }
    }

    // --- CORRECTED FONT METHOD IMPLEMENTATIONS ---

    private static void drawHeader(PDPageContentStream contentStream) throws IOException {
        contentStream.beginText();
        // *** CORRECT: Instantiating the font using the Standard14Fonts enum ***
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Official COVID-19 Vaccination Card");
        contentStream.endText();

        yPosition -= 20;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Issued by Public Health Authority");
        contentStream.endText();
    }

    private static float drawPatientInfo(PDPageContentStream contentStream, float currentY) throws IOException {
        String[] fields = {"Name:", "DOB:", "Patient ID:"};
        float newY = currentY;

        // *** CORRECT: Instantiating the font using the Standard14Fonts enum ***
        PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        contentStream.setFont(regularFont, 10);

        for (String field : fields) {
            newY -= LINE_HEIGHT;

            // Draw field label
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, newY);
            contentStream.showText(field);
            contentStream.endText();

            // Draw placeholder line
            // Use the correct font object for width calculation
            float labelWidth = regularFont.getStringWidth(field) / 1000 * 10;
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(MARGIN + labelWidth + 5, newY - 2);
            contentStream.lineTo(PAGE_WIDTH - MARGIN, newY - 2);
            contentStream.stroke();
        }
        return newY;
    }

    // ... (All other methods like drawSectionTitle and drawHistoryTable need to use the
    // new PDType1Font(Standard14Fonts.FontName.FONT_NAME) pattern for their font setting.)
}