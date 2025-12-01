package org.immregitries.clvr;

import org.immregitries.clvr.model.CLVRToken;

import java.io.IOException;

/**
 * Cbor Conversion
 */
public interface CborService {
    /**
     * Converts to COSE
     *
     * @param clvrToken
     * @return
     * @throws IOException
     */
    byte[] toCbor(CLVRToken clvrToken) throws IOException;

    CLVRToken undoCbor(byte[] cbor) throws IOException;
}
