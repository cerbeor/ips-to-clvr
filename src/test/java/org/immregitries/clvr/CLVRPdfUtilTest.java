package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.syadem.nuva.NUVA;
import com.syadem.nuva.SupportedLocale;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hl7.fhir.r4.model.Bundle;
import org.immregitries.clvr.impl.CLVRPdfServiceImpl;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.immregitries.clvr.BaseCLVRTest.*;
import static org.immregitries.clvr.FhirConversionUtilTest.FOR_PDF_TEST;
import static org.immregitries.clvr.FhirConversionUtilTest.IPS_SAMPLE_R4_IIS;

class CLVRPdfUtilTest extends BaseCLVRTest {
    private CLVRPdfService clvrPdfService;
    private ObjectMapper objectMapper = new ObjectMapper();

    public CLVRPdfUtilTest() throws IOException {
        super();
        this.nuvaService = new NUVAService(NUVA.load(SupportedLocale.English));
        this.clvrPdfService = new CLVRPdfServiceImpl(nuvaService);
    }

    @Test
    void createPdf() throws IOException, WriterException {
        CLVRPayload payload =  objectMapper.readValue(TEST_SAMPLE, CLVRPayload.class);
        CLVRToken clvrToken = new CLVRToken(payload, TEST_ISSUER);
        PDDocument pdDocument = clvrPdfService.createPdf(clvrToken, TEST_SAMPLE_QR.getBytes(), "Unit test");
        printPdf(pdDocument,"unit-test-pdf");
    }

    @Test
    void createPdfIIS() throws IOException, WriterException {
        CLVRPayload payload =  objectMapper.readValue(FOR_PDF_TEST, CLVRPayload.class);
        CLVRToken clvrToken = new CLVRToken(payload, TEST_ISSUER);
        PDDocument pdDocument = clvrPdfService.createPdf(clvrToken, TEST_SAMPLE_QR.getBytes(), "Unit test");
        printPdf(pdDocument,"unit-test-iis-pdf");
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