package org.immregitries.clvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.immregitries.clvr.model.EvCPayload;
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
     * @param evCPayload
     * @return
     * @throws DataFormatException
     * @throws JsonProcessingException
     */
    public byte[] toCbor(EvCPayload evCPayload) throws IOException {
        byte[] cbor = cborMapper.writeValueAsBytes(evCPayload);
        logger.info("CBOR byte array created successfully.\ninputObject: {}\ncbor: {}\nparsed: {}", new ObjectMapper().writeValueAsString(evCPayload), new String(cbor), cborMapper.createParser(cbor).readValueAsTree());
        return cbor;
    }

    public EvCPayload undoCbor(byte[] cbor) throws IOException {
//		logger.info("parse CBOR cbor: {}\nparsed: {}", new String(cbor), cborMapper.createParser(cbor).readValueAsTree());
        EvCPayload evCPayload = cborMapper.readValue(cbor, EvCPayload.class);
        return evCPayload;
    }
}
