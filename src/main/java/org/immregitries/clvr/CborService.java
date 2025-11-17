package org.immregitries.clvr;

import org.immregitries.clvr.model.CLVRPayload;

import java.io.IOException;

public interface CborService {
    byte[] toCbor(CLVRPayload clvrPayload) throws IOException;

    CLVRPayload undoCbor(byte[] cbor) throws IOException;
}
