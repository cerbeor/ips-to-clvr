package org.immregitries.clvr;

import com.authlete.cose.COSEException;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;

public interface SigningService {
    byte[] createCoseSign1(byte[] cborPayload, KeyPair keyPair) throws IOException, COSEException;

    byte[] cborFromCoseSign1(byte[] encode, PublicKey publicKey) throws IOException, COSEException;
}
