package org.immregitries.clvr;

import com.authlete.cose.COSEException;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;

public interface SigningService {
    byte[] createCoseSign1(byte[] cborPayload, KeyPair keyPair, String kid) throws IOException, COSEException;

    byte[] cborFromCoseSign1Unverified(byte[] encode) throws IOException, COSEException;
    byte[] cborFromCoseSign1(byte[] encode, PublicKey publicKey, boolean noVerify) throws IOException, COSEException;
    byte[] cborFromCoseSign1(byte[] encode, Map<String, PublicKey> publicKeyMap, boolean noVerify) throws IOException, COSEException;
    byte[] cborFromCoseSign1(byte[] encode, PublicKey publicKey, Map<String, PublicKey> publicKeyMap, boolean noVerify) throws IOException, COSEException;
}
