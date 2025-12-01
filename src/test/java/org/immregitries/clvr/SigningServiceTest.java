package org.immregitries.clvr;

import com.authlete.cose.COSEException;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class SigningServiceTest extends BaseCLVRTest {

    public SigningServiceTest() throws IOException {
        super();
    }

//    @Test
//    void createCoseSign1() {
//    }

    @Test
    void coseSign1Consistence() throws IOException {
        testSigningConsistence(TEST_SAMPLE);
    }

    void testSigningConsistence(String string) throws IOException {
        CLVRPayload payload =  objectMapper.readValue(string, CLVRPayload.class);
        testSigningConsistence(payload);
    }

    void testSigningConsistence(CLVRPayload payload) throws IOException {
        byte[] cbor = cborService.toCbor(new CLVRToken(payload, "SYA"));
        testSigningConsistence(cbor);
    }

    void testSigningConsistence(byte[] cbor) {
        try {
            KeyPair keyPair = testKeyPairManager.getOrCreateKeyPair(TEST_KEY_FILE_NAME);
            byte[] coseSign1 = signingService.createCoseSign1(cbor, keyPair);
            byte[] cborUndone = signingService.cborFromCoseSign1(coseSign1, keyPair.getPublic());
            String s1 = new String(cbor);
            String s2 = new String(cborUndone);
//            logger.info("Test CoseSign1 lengths: {} - {} \n{} \n{} \n{} \n{}",cbor.length,cborUndone.length, s1, s2, cbor, cborUndone);
            assertEquals(cbor.length,cborUndone.length);
            assertEquals(s1, s2);
            assertArrayEquals(cbor, cborUndone);
        } catch (IOException | COSEException | InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                 InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}