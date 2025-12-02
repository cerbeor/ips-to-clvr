package org.immregitries.clvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
class CborServiceTest extends BaseCLVRTest {

    Logger logger = LoggerFactory.getLogger(CborServiceTest.class);

    public CborServiceTest() throws IOException {
        super();
    }

//    @Test
//    void toCborConsistence() throws JsonProcessingException {
//        String test_sample = TEST_SAMPLE;
//        testCborConsistence(test_sample);
//    }

    @Test
    void toCborConsistence() throws JsonProcessingException {
        String test_sample = TEST_SAMPLE;
        testCborConsistence(test_sample);
    }

    void testCborConsistence(String string) throws JsonProcessingException {
        CLVRPayload payload = objectMapper.readValue(string, CLVRPayload.class);
        testCborConsistence(payload);
    }

    void testCborConsistence(CLVRPayload payload) {
        testCborConsistence(new CLVRToken(payload, TEST_ISSUER));
    }
    void testCborConsistence(CLVRToken clvrToken) {
        try {
            byte[] cbor = cborService.toCbor(clvrToken);
            logger.info("Testing CBOR for {} -> {}", clvrToken, new String(cbor));
            CLVRToken undone =  cborService.undoCbor(cbor);
            assertEquals(clvrToken.toString(), undone.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}