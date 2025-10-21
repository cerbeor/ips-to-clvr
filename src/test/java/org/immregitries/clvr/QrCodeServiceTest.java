package org.immregitries.clvr;

import com.authlete.cose.COSEException;
import org.immregitries.clvr.model.CLVRPayload;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest extends ClvrTest {


    public QrCodeServiceTest() throws IOException {
        super();
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