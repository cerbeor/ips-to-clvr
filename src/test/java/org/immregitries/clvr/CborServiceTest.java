package org.immregitries.clvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immregitries.clvr.model.CLVRPayload;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
class CborServiceTest extends ClvrTest {

    Logger logger = LoggerFactory.getLogger(CborServiceTest.class);

    public CborServiceTest() throws IOException {
        super();
    }

    @Test
    void toCborConsistence() throws JsonProcessingException {
        String test_sample = TEST_SAMPLE;
        testCborConsistence(test_sample);
    }

    void testCborConsistence(String string) throws JsonProcessingException {
        CLVRPayload payload =  objectMapper.readValue(string, CLVRPayload.class);
        testCborConsistence(payload);
    }

    void testCborConsistence(CLVRPayload payload) {
        try {
            byte[] cbor = cborService.toCbor(payload);
            logger.info("Testing CBOR for {} -> {}", payload, new String(cbor));
            CLVRPayload undone =  cborService.undoCbor(cbor);
            assertEquals(payload.toString(), undone.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}