package org.immregitries.clvr;

import com.authlete.cose.*;
import nl.minvws.encoding.Base45;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.immregitries.clvr.model.CLVRPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.*;
import java.util.zip.DataFormatException;

public class CLVRService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String VC_1 = "VC1:";
	public static final boolean NOWRAP = false;

	private SigningService signingService;
	private CborService cborService;
	private QrCodeService qrCodeService;

	public CLVRService(SigningService signingService, CborService cborService, QrCodeService qrCodeService) {
		this.signingService = signingService;
		this.cborService = cborService;
		this.qrCodeService = qrCodeService;
	}

	public String encodeCLVRtoQrCode(CLVRPayload CLVRPayload, KeyPair keyPair) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
		byte[] cborPayload = cborService.toCbor(CLVRPayload);
		byte[] cosePayload = signingService.createCoseSign1(cborPayload, keyPair);
//		byte[] coseOld = createCoseSign1Old(iisSigningKey, cborPayload);
//		logger.info("test cose {}\n, coseold {}\n", cosePayload, coseOld);
		byte[] deflated = CompressionUtil.deflate(cosePayload, NOWRAP);
		return qrCodeService.encodeQrCode(deflated);
	}


	public CLVRPayload decodeFullQrCode(byte[] qrcode, KeyPair keyPair) throws COSEException, IOException, DataFormatException {
//		logger.info("A0 qrcode {}", new String(qrcode));
		byte[] compressed = qrCodeService.decodeQrCode(qrcode);
//		logger.info("A1 compressed {}", new String(compressed));
		byte[] cose = CompressionUtil.inflate(compressed, NOWRAP);
//		logger.info("A2 cose {}", new String(cose));
		byte[] cbor = signingService.cborFromCoseSign1(cose, keyPair.getPublic());
//		logger.info("A3 cbor {}", new String(cbor));
		CLVRPayload CLVRPayload = cborService.undoCbor(cbor);
//		logger.info("A4 evc {}", CLVRPayload);
		return CLVRPayload;
	}



}
