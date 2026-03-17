package org.immregitries.clvr;

import ca.uhn.fhir.context.FhirContext;
import com.authlete.cose.COSEException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.fhir.r4.model.Bundle;
import org.immregitries.clvr.mapping.FhirConversionUtilR4;
import org.immregitries.clvr.mapping.FhirConversionUtilR5;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static org.immregitries.clvr.mapping.FhirSystems.*;


class FhirConversionUtilTest extends BaseCLVRTest {

    private FhirConversionUtilR4 fhirConversionUtilR4;
    private FhirConversionUtilR5 fhirConversionUtilR5;
    private FhirContext fhirContextR4;
    private FhirContext fhirContextR5;

    public FhirConversionUtilTest() throws IOException {
        super();
        fhirContextR4 = FhirContext.forR4();
        fhirConversionUtilR4 = new FhirConversionUtilR4(nuvaService);
//        fhirContextR5 = FhirContext.forR5();
//        fhirConversionUtilR5 = new FhirConversionUtilR5(nuvaService);
    }

    /**
     * Currently invalid as the Registry identifier is maybe bound to change in its FHIR representation
     *
     * @throws JsonProcessingException
     */
    @Test
    void toCLVRPayloadFromBundleR4() throws JsonProcessingException {
        String ipsSample = Samples.IPS_SAMPLE_R4_BUNDLE_SYADEM;
        String testSample = TEST_SAMPLE;
//        logger.info(IPS_SAMPLE_R4_BUNDLE);
        Bundle bundle = fhirContextR4.newJsonParser().parseResource(Bundle.class, ipsSample);
        CLVRPayload clvrPayloadFromBundle = fhirConversionUtilR4.toCLVRPayloadFromBundle(bundle);
        Assertions.assertNotNull(clvrPayloadFromBundle);
        Assertions.assertEquals(objectMapper.readValue(testSample, CLVRPayload.class).toString(), clvrPayloadFromBundle.toString());
    }

    @Test
    void testIISBundle() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, COSEException, SignatureException, InvalidKeyException, NoSuchProviderException {
        String ipsSample = Samples.IPS_SAMPLE_R4_IIS;
//        logger.info(IPS_SAMPLE_R4_BUNDLE);
        Bundle bundle = fhirContextR4.newJsonParser().parseResource(Bundle.class, ipsSample);
        CLVRPayload clvrPayloadFromBundle = fhirConversionUtilR4.toCLVRPayloadFromBundle(bundle);
        Assertions.assertNotNull(clvrPayloadFromBundle);
        String qr = clvrService.encodeCLVRtoQrCode(new CLVRToken(clvrPayloadFromBundle, "Test"), testKeyPairManager.getOrCreateKeyPair(TEST_KEY_FILE_NAME));
//        logger.info(clvrPayloadFromBundle.toString());
        Assertions.assertNotNull(qr);
    }
}