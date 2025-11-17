package org.immregitries.clvr.impl;

import com.authlete.cose.*;
import org.immregitries.clvr.*;
import org.immregitries.clvr.model.CLVRPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.*;
import java.util.zip.DataFormatException;

public class CLVRServiceImpl implements CLVRService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private SigningService signingService;
	private CborService cborService;
	private QrCodeService qrCodeService;

	public CLVRServiceImpl(SigningService signingService, CborService cborService, QrCodeService qrCodeServiceImpl) {
		this.signingService = signingService;
		this.cborService = cborService;
		this.qrCodeService = qrCodeServiceImpl;
	}

	@Override
	public String encodeCLVRtoQrCode(CLVRPayload CLVRPayload, KeyPair keyPair) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
		byte[] cborPayload = cborService.toCbor(CLVRPayload);
		byte[] cosePayload = signingService.createCoseSign1(cborPayload, keyPair);
		byte[] deflated = CompressionUtil.deflate(cosePayload, NOWRAP);
		return qrCodeService.encodeQrCode(deflated);
	}


	@Override
	public CLVRPayload decodeFullQrCode(byte[] qrcode, KeyPair keyPair) throws COSEException, IOException, DataFormatException {
		byte[] compressed = qrCodeService.decodeQrCode(qrcode);
		byte[] cose = CompressionUtil.inflate(compressed, NOWRAP);
		PublicKey aPublic = null;
		if (keyPair != null) {
			aPublic = keyPair.getPublic();
		}
		byte[] cbor = signingService.cborFromCoseSign1(cose, aPublic);
		CLVRPayload CLVRPayload = cborService.undoCbor(cbor);
		return CLVRPayload;
	}



}
