package org.immregitries.clvr.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.immregitries.clvr.CborService;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.zip.DataFormatException;

public class CborServiceImpl implements CborService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CBORMapper cborMapper = new CBORMapper();


    /**
     * Uses Jackson specification to cborize clvrPayload
     *
     * @param clvrToken
     * @return
     * @throws DataFormatException
     * @throws JsonProcessingException
     */
    @Override
    public byte[] toCbor(CLVRToken clvrToken) throws IOException {
        byte[] cbor = cborMapper.writeValueAsBytes(clvrToken);
        return cbor;
    }

    @Override
    public CLVRToken undoCbor(byte[] cbor) throws IOException {
        CLVRToken  clvrToken = cborMapper.readValue(cbor, CLVRToken.class);
        return clvrToken;
    }
}
