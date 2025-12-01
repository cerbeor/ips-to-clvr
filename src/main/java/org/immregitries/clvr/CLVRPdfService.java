package org.immregitries.clvr;

import com.google.zxing.WriterException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.immregitries.clvr.model.CLVRToken;

import java.io.IOException;

/**
 * Generates PDF CLVR prototype
 */
public interface CLVRPdfService {
    PDDocument createPdf(CLVRToken token, byte[] qrCode, String creator) throws IOException, WriterException;

//    PDDocument createPdf(CLVRPayload payload, byte[] qrCode, String creator) throws IOException, WriterException;
}
