package org.immregitries.clvr.impl;

import com.authlete.cbor.CBORDecoder;
import com.authlete.cbor.CBORItem;
import com.authlete.cbor.CBORTaggedItem;
import com.authlete.cose.*;
import com.authlete.cose.constants.COSEAlgorithms;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.immregitries.clvr.SigningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class SigningServiceImpl implements SigningService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private CBORMapper cborMapper;

    public SigningServiceImpl() {
        this.cborMapper = new CBORMapper();
        Security.addProvider(new BouncyCastleProvider());
    }


    @Override
    public byte[] createCoseSign1(byte[] cborPayload, KeyPair keyPair, String kid) throws IOException, COSEException {
        ECPrivateKey priKey = (ECPrivateKey) keyPair.getPrivate();

        // Create a signer with the private key.
        COSESigner signer = new COSESigner(priKey);

        // Signature algorithm
        int algorithm = COSEAlgorithms.ES256;

        // Protected header
        COSEProtectedHeader protectedHeader =
                new COSEProtectedHeaderBuilder().alg(algorithm).build();

        // Unprotected header
        COSEUnprotectedHeader unprotectedHeader =
                new COSEUnprotectedHeaderBuilder().kid(kid).build();

        // Sig_structure
        SigStructure structure = new SigStructureBuilder()
                .signature1()
                .bodyAttributes(protectedHeader)
                .payload(cborPayload).build();
        // Sign the Sig_structure (= generate a signature).
        byte[] signature = signer.sign(structure, COSEAlgorithms.ES256);
        COSESign1 sign1 = new COSESign1Builder()
                .protectedHeader(protectedHeader)
                .unprotectedHeader(unprotectedHeader)
                .payload(cborPayload)
                .signature(signature)
                .build();
        CBORTaggedItem taggedItem = new CBORTaggedItem(18, sign1);

        byte[] encode = taggedItem.encode();
        return encode;
    }

    public byte[] cborFromCoseSign1Unverified(byte[] encode) throws IOException, COSEException {
        return cborFromCoseSign1(encode,null, null, true);
    }

    public byte[] cborFromCoseSign1(byte[] encode, Map<String, PublicKey> publicKeyMap, boolean noVerify) throws IOException, COSEException {
        return cborFromCoseSign1(encode,null, publicKeyMap, noVerify);
    }

    public byte[] cborFromCoseSign1(byte[] encode, PublicKey publicKey, boolean noVerify) throws IOException, COSEException {
        return cborFromCoseSign1(encode, publicKey, null, noVerify);
    }

    @Override
    public byte[] cborFromCoseSign1(byte[] encode, PublicKey publicKey, Map<String, PublicKey> publicKeyMap, boolean noVerify) throws IOException, COSEException {
        /*
         * Decode
         */
        CBORDecoder cborDecoder = new CBORDecoder(encode);
        CBORItem item = cborDecoder.next();
        COSESign1 coseSign1;
        try {
            CBORTaggedItem tagged = (CBORTaggedItem) item;
            coseSign1 = (COSESign1) tagged.getTagContent();
        } catch (ClassCastException classCastException) {
            coseSign1 = COSESign1.build(item);
        }

        /*
         * Verify signature
         */
        if (!noVerify) {
            if (publicKeyMap != null && !publicKeyMap.isEmpty()) {
                // Trying to
                COSEVerifier coseVerifier = new COSEVerifier((i, i1, bytes) -> {
                    String kid = new String(bytes);
                    PublicKey publicKey2 = publicKeyMap.get(kid);
                    if (publicKey2 == null) {
                        throw new COSEException("Public key not found for kid: " + kid);
                    }
                    return publicKey2;
                });
                coseVerifier.verify(coseSign1, null);
            } else if (publicKey != null) {
                COSEVerifier coseVerifier = new COSEVerifier(publicKey);
                coseVerifier.verify(coseSign1, null);
            } else {
                throw new COSEException("Public key is empty");
            }
        }


        byte[] bytes = coseSign1.getPayload().encode();
        /*
         * Removing bytes added through Cose to only get the payload
         */
//        return bytes;
        return Arrays.copyOfRange(bytes, 2, bytes.length);
    }


    @Deprecated
    public byte[] createCoseSign1Old(KeyPair keyPair, byte[] cborPayload) throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException {
        PrivateKey privateKey = keyPair.getPrivate();

        // 1. Define the protected header as a CBOR Map
        // We'll use a simple CBOR map with the algorithm identifier (-7 for ES256)

        // This is a minimal protected header. In a real-world scenario, you might add more claims.
        byte[] protectedHeader = cborMapper.writeValueAsBytes(Collections.singletonMap(1, -7)); // alg: ES256

        // 2. Define the unprotected header (an empty CBOR map for this example)
        byte[] unprotectedHeader = cborMapper.writeValueAsBytes(Collections.emptyMap());

        // 3. Construct the 'Sig_structure' for signing, as defined in RFC 9052 Section 4.4
        ByteArrayOutputStream sigStructureStream = new ByteArrayOutputStream();

        // This is a simplified representation of the CBOR array for 'Sig_structure'
        // [
        //   "Signature1",
        //   protected_header_bstr,
        //   aad_bstr,
        //   payload_bstr
        // ]
        sigStructureStream.write(0x84); // CBOR array of 4 items
        sigStructureStream.write(0x6a); // CBOR text string of length 10
        sigStructureStream.write("Signature1".getBytes());
        sigStructureStream.write(0x40 + protectedHeader.length); // CBOR byte string
        sigStructureStream.write(protectedHeader);
        sigStructureStream.write(0x40); // CBOR empty byte string for AAD
        sigStructureStream.write(0x40 + cborPayload.length); // CBOR byte string
        sigStructureStream.write(cborPayload);

        byte[] toBeSigned = sigStructureStream.toByteArray();

        // 4. Sign the 'ToBeSigned' data
        Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
        signature.initSign(privateKey);
        signature.update(toBeSigned);
        byte[] coseSignature = signature.sign();

        // 5. Assemble the final COSE_Sign1 message
        ByteArrayOutputStream coseStream = new ByteArrayOutputStream();

        // The final structure is a CBOR array of 4 elements
        // [
        //   protected_header_bstr,
        //   unprotected_header_map,
        //   payload_bstr,
        //   signature_bstr
        // ]

        // The headers and payload are all CBOR-tagged as byte strings.
        // We need to construct the final array manually for this simple example.
        coseStream.write(0x84); // CBOR array of 4 items

        coseStream.write(0x40 + protectedHeader.length); // CBOR byte string
        coseStream.write(protectedHeader);

        coseStream.write(0xa0); // CBOR empty map (unprotected header)

        coseStream.write(0x40 + cborPayload.length); // CBOR byte string
        coseStream.write(cborPayload);

        coseStream.write(0x40 + coseSignature.length); // CBOR byte string
        coseStream.write(coseSignature);

        return coseStream.toByteArray();
    }
}
