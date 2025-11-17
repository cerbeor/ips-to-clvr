package org.immregitries.clvr;

import com.authlete.cose.COSEException;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;

import java.io.IOException;
import java.security.*;
import java.util.zip.DataFormatException;

public interface CLVRService {
    boolean NOWRAP = false;

    String encodeCLVRtoQrCode(CLVRToken clvrToken, KeyPair keyPair) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException;

    CLVRToken decodeFullQrCode(byte[] qrcode, KeyPair keyPair) throws COSEException, IOException, DataFormatException;
}
