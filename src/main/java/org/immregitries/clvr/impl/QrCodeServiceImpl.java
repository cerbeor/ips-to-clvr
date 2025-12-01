package org.immregitries.clvr.impl;

import com.authlete.cose.COSEException;
import nl.minvws.encoding.Base45;
import org.apache.commons.lang3.StringUtils;
import org.immregitries.clvr.QrCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

public class QrCodeServiceImpl implements QrCodeService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public byte[] decodeQrCode(byte[] qrcode) {
        String s = new String(qrcode);
        if (s.startsWith(VC_1)) {
            String s2 = StringUtils.substringAfter(s, VC_1);
            return Base45.getDecoder().decode(s2);
        }
        return Base45.getDecoder().decode(s);
    }

    @Override
    public String encodeQrCode(byte[] deflated) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        return VC_1 + Base45.getEncoder().encodeToString(deflated);
    }

}
