package org.immregitries.clvr.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.immregitries.clvr.CborService;
import org.immregitries.clvr.model.CLVRToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.zip.DataFormatException;

public class CborServiceImpl implements CborService {
    private final CBORMapper cborMapper;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public CborServiceImpl() {
        cborMapper = new CBORMapper();
    }

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
        CLVRToken clvrToken = cborMapper.readValue(cbor, CLVRToken.class);
//        ObjectMapper mapper = new ObjectMapper(new CBORFactory());
//
//        // Use TypeReference to specify the map with Integer keys
//        Map<Integer, Integer> intKeyMap = mapper.readValue(cbor, new TypeReference<Map<Integer, Integer>>() {});
//
//        System.out.println("Deserialized Map: " + intKeyMap);
//        System.out.println("Value for key 1: " + intKeyMap.get(1)); // Access using int key
        return clvrToken;
    }
}
