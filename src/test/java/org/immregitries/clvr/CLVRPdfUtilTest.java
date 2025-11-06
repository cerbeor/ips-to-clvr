package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.syadem.nuva.NUVA;
import com.syadem.nuva.SupportedLocale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.immregitries.clvr.model.CLVRPayload;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.immregitries.clvr.BaseCLVRTest.TEST_SAMPLE;
import static org.immregitries.clvr.BaseCLVRTest.TEST_SAMPLE_QR;
import static org.junit.jupiter.api.Assertions.*;

class CLVRPdfUtilTest {
    private NUVAService nuvaService;
    private VaccinationSummaryCard vaccinationSummaryCard;
    private ObjectMapper objectMapper = new ObjectMapper();

    public CLVRPdfUtilTest() throws IOException {
        this.nuvaService = new NUVAService(NUVA.load(SupportedLocale.English));
        this.vaccinationSummaryCard = new VaccinationSummaryCard(nuvaService);
    }

    @Test
    void createPdf() throws IOException, WriterException {
         CLVRPayload payload =  objectMapper.readValue(TEST_SAMPLE, CLVRPayload.class);
        PDDocument pdDocument = vaccinationSummaryCard.generatePDF(payload, TEST_SAMPLE_QR.getBytes(), "Unit test");
        CLVRPdfUtil.printPdf(pdDocument,"test-zbeul-9999");
    }

    @Test
    void printPdf() {
    }
}