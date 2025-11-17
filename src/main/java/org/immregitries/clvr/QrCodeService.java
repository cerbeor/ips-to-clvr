package org.immregitries.clvr;

import com.authlete.cose.COSEException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

public interface QrCodeService {
    String VC_1 = "VC1:";

    byte[] decodeQrCode(byte[] qrcode);

    String encodeQrCode(byte[] deflated) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException;
}
