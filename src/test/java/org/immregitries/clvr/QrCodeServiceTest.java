package org.immregitries.clvr;

import com.authlete.cose.COSEException;
import org.immregitries.clvr.model.CLVRPayload;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.*;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest extends BaseCLVRTest {


    public QrCodeServiceTest() throws IOException {
        super();
    }

    @Test
    public void processSampleEnd() throws COSEException, DataFormatException, IOException {
        String qr = TEST_SAMPLE_QR;
        String sample = TEST_SAMPLE;
        extracted(qr, sample);
    }

    /**
     * Verifying qrcode parsing leads to the right payload
     * @param qr
     * @param originalPayload
     * @throws COSEException
     * @throws IOException
     * @throws DataFormatException
     */
    private void extracted(String qr, String originalPayload) throws COSEException, IOException, DataFormatException {
        //        byte[] cbor = cborService.toCbor(objectMapper.readValue(sample,CLVRPayload.class));
//        logger.info("Expected length {}, expected CBOR {}",cbor.length, new String(cbor));
        CLVRPayload payload = clvrService.decodeFullQrCode(qr.getBytes(), null);
        assertEquals(objectMapper.readValue(originalPayload, CLVRPayload.class).toString(),payload.toString());
    }


    @Test
    void decodeQrCode() throws Exception {
        testQrCodeConsistence(TEST_SAMPLE);
    }


    void testQrCodeConsistence(String string) throws Exception {
        CLVRPayload payload =  objectMapper.readValue(string, CLVRPayload.class);
        testQrCodeConsistence(payload);
    }

    void testQrCodeConsistence(CLVRPayload payload) throws Exception {
        byte[] cbor = cborService.toCbor(payload);
        KeyPair keyPair = testKeyPairManager.getOrCreateKeyPair(TEST_KEY_FILE_NAME);
        byte[] coseSign1 = signingService.createCoseSign1(cbor, keyPair);
        byte[] deflated = CompressionUtil.deflate(coseSign1, NOWRAP);
        testQrCodeConsistence(deflated);
    }

    void testQrCodeConsistence(byte[] deflated) throws Exception {
        String qrcode = qrCodeService.encodeQrCode(deflated);
        logger.info("QRCODE: \n{}", qrcode);
        byte[] undone = qrCodeService.decodeQrCode(qrcode.getBytes());
        assertArrayEquals(deflated, undone);
    }
}