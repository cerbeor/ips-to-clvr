package org.immregitries.clvr;

import com.authlete.cbor.CBORDecoder;
import com.authlete.cbor.CBORItem;
import com.authlete.cbor.CBORTaggedItem;
import com.authlete.cose.*;
import com.authlete.cose.constants.COSEAlgorithms;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import nl.minvws.encoding.Base45;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.immregitries.clvr.model.EvCPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.*;
import java.util.zip.DataFormatException;

public class EvcService {

	public static final String VC_1 = "VC1:";
	public static final boolean NOWRAP = false;

	private SigningService signingService = new SigningService();
	private CborService cborService = new CborService();
	

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public EvcService() {
		Security.addProvider(new BouncyCastleProvider());
	}

	public byte[] decodeQrCode(byte[] qrcode) {
		String s = new String(qrcode);
		if (s.startsWith(VC_1)) {
			String s2 = StringUtils.substringAfter(s,VC_1);
			return Base45.getDecoder().decode(s2);
		}
		return Base45.getDecoder().decode(s);
	}

	public String encodeQrCode(EvCPayload evCPayload, KeyPair keyPair) throws IOException, COSEException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
		byte[] cborPayload = cborService.toCbor(evCPayload);
		byte[] cosePayload = signingService.createCoseSign1(cborPayload, keyPair);
//		byte[] coseOld = createCoseSign1Old(iisSigningKey, cborPayload);
//		logger.info("test cose {}\n, coseold {}\n", cosePayload, coseOld);
		byte[] deflated = CompressionUtil.deflate(cosePayload, NOWRAP);
		return VC_1 + Base45.getEncoder().encodeToString(deflated);
	}


	public EvCPayload decodeFullQrCode(byte[] qrcode, KeyPair keyPair) throws COSEException, IOException, DataFormatException {
		logger.info("A0 qrcode {}", new String(qrcode));
		byte[] compressed = decodeQrCode(qrcode);
		logger.info("A1 compressed {}", new String(compressed));
		byte[] cose = CompressionUtil.inflate(compressed, NOWRAP);
		logger.info("A2 cose {}", new String(cose));
		byte[] cbor = signingService.cborFromCoseSign1(cose, keyPair.getPublic());
		logger.info("A3 cbor {}", new String(cbor));
		EvCPayload evCPayload = cborService.undoCbor(cbor);
		logger.info("A4 evc {}", evCPayload);
		return evCPayload;
	}



}
