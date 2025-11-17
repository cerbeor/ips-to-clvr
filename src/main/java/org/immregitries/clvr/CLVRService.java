package org.immregitries.clvr;

import com.authlete.cose.COSEException;
import org.immregitries.clvr.model.CLVRPayload;

import java.io.IOException;
import java.security.*;
import java.util.zip.DataFormatException;

public interface CLVRService {
    boolean NOWRAP = false;

    String encodeCLVRtoQrCode(CLVRPayload CLVRPayload, KeyPair keyPair) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException;

    CLVRPayload decodeFullQrCode(byte[] qrcode, KeyPair keyPair) throws COSEException, IOException, DataFormatException;
}
