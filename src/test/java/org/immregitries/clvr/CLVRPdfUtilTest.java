package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.syadem.nuva.NUVA;
import com.syadem.nuva.SupportedLocale;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.immregitries.clvr.impl.CLVRPdfServiceImpl;
import org.immregitries.clvr.model.CLVRPayload;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.immregitries.clvr.BaseCLVRTest.TEST_SAMPLE;
import static org.immregitries.clvr.BaseCLVRTest.TEST_SAMPLE_QR;

class CLVRPdfUtilTest {
    private NUVAService nuvaService;
    private CLVRPdfService clvrPdfService;
    private ObjectMapper objectMapper = new ObjectMapper();

    public CLVRPdfUtilTest() throws IOException {
        this.nuvaService = new NUVAService(NUVA.load(SupportedLocale.English));
        this.clvrPdfService = new CLVRPdfServiceImpl(nuvaService);
    }

    @Test
    void createPdf() throws IOException, WriterException {
         CLVRPayload payload =  objectMapper.readValue(TEST_SAMPLE, CLVRPayload.class);
        PDDocument pdDocument = clvrPdfService.createPdf(payload, TEST_SAMPLE_QR.getBytes(), "Unit test");
        printPdf(pdDocument,"unit-test-pdf");
    }

    private static void printPdf(
            PDDocument pdDocument,
            String name
    ) throws IOException {
        String fileName = StringUtils.substringBefore(name,".");
        pdDocument.save(fileName + ".pdf");
        pdDocument.close();
    }
}