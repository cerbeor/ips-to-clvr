package org.immregitries.clvr;

import com.authlete.cose.COSEException;
import nl.minvws.encoding.Base45;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.*;

public class QrCodeService {
    public static final String VC_1 = "VC1:";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public byte[] decodeQrCode(byte[] qrcode) {
        String s = new String(qrcode);
        if (s.startsWith(VC_1)) {
            String s2 = StringUtils.substringAfter(s,VC_1);
            return Base45.getDecoder().decode(s2);
        }
        return Base45.getDecoder().decode(s);
    }

    public String encodeQrCode(byte[] deflated) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        return VC_1 + Base45.getEncoder().encodeToString(deflated);
    }

}
