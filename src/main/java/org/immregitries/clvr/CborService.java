package org.immregitries.clvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.immregitries.clvr.model.CLVRPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.zip.DataFormatException;

public class CborService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CBORMapper cborMapper = new CBORMapper();


    /**
     * Uses Jackson specification to cborize evCPayload
     *
     * @param CLVRPayload
     * @return
     * @throws DataFormatException
     * @throws JsonProcessingException
     */
    public byte[] toCbor(CLVRPayload CLVRPayload) throws IOException {
        byte[] cbor = cborMapper.writeValueAsBytes(CLVRPayload);
//        logger.info("CBOR byte array created successfully.\ninputObject: {}\ncbor: {}\nparsed: {}", new ObjectMapper().writeValueAsString(evCPayload), new String(cbor), cborMapper.createParser(cbor).readValueAsTree());
        return cbor;
    }

    public CLVRPayload undoCbor(byte[] cbor) throws IOException {
//		logger.info("parse CBOR cbor: {}\nparsed: {}", new String(cbor), cborMapper.createParser(cbor).readValueAsTree());
        CLVRPayload CLVRPayload = cborMapper.readValue(cbor, CLVRPayload.class);
        return CLVRPayload;
    }
}
