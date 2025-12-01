package org.immregitries.clvr.impl;

import com.authlete.cose.COSEException;
import org.immregitries.clvr.*;
import org.immregitries.clvr.model.CLVRToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.*;
import java.util.zip.DataFormatException;

public class CLVRServiceImpl implements CLVRService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SigningService signingService;
    private final CborService cborService;
    private final QrCodeService qrCodeService;

    public CLVRServiceImpl(SigningService signingService, CborService cborService, QrCodeService qrCodeServiceImpl) {
        this.signingService = signingService;
        this.cborService = cborService;
        this.qrCodeService = qrCodeServiceImpl;
    }

    @Override
    public String encodeCLVRtoQrCode(CLVRToken clvrToken, KeyPair keyPair) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        byte[] cborPayload = cborService.toCbor(clvrToken);
        byte[] cosePayload = signingService.createCoseSign1(cborPayload, keyPair);
        byte[] deflated = CompressionUtil.deflate(cosePayload, NOWRAP);
        return qrCodeService.encodeQrCode(deflated);
    }


    @Override
    public CLVRToken decodeFullQrCode(byte[] qrcode, KeyPair keyPair) throws COSEException, IOException, DataFormatException {
        byte[] compressed = qrCodeService.decodeQrCode(qrcode);
        byte[] cose = CompressionUtil.inflate(compressed, NOWRAP);
        PublicKey aPublic = null;
        if (keyPair != null) {
            aPublic = keyPair.getPublic();
        }
        byte[] cbor = signingService.cborFromCoseSign1(cose, aPublic);
        CLVRToken clvrToken = cborService.undoCbor(cbor);
        return clvrToken;
    }


}
