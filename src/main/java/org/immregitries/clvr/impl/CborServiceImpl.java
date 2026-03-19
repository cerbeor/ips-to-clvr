package org.immregitries.clvr.impl;

import com.authlete.cbor.CBORPairList;
import com.authlete.cbor.CBORParser;
import com.authlete.cbor.CBORizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.immregitries.clvr.CborService;
import org.immregitries.clvr.model.CLVRToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.zip.DataFormatException;

import static org.junit.Assert.assertTrue;

public class CborServiceImpl implements CborService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public CborServiceImpl() {
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
        CBORizer cboRizer = new CBORizer();
        Map<Integer, Object> cborMap = clvrToken.toCBORMap();
        CBORPairList pairList = (CBORPairList) cboRizer.cborizeMap(cborMap);
        return  pairList.encode();
    }

    @Override
    public CLVRToken undoCbor(byte[] cbor) throws IOException {
        CBORParser cborParser = new CBORParser(cbor);
        Object object = cborParser.next();
        assertTrue(object instanceof Map);
        Map<Object, Object> map = (Map<Object, Object>) object;
        CLVRToken clvrToken = CLVRToken.fromCBORMap(map);
        return clvrToken;
    }
}
