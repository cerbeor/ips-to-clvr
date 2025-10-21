package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.syadem.nuva.Vaccine;
import org.apache.log4j.BasicConfigurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ClvrTest {
    public static final boolean NOWRAP = false;

    public static final String TEST_SAMPLE = "{\"ver\": \"1.0.0\", \"nam\": {\"fnt\": \"DOË\"," +
            " \"gnt\": \"John\"}, \"dob\": \"2017-07-19\", \"v\": " +
            "[{\"reg\": \"FRA\", \"rep\": 36, \"i\": 1245, \"a\": 1386," +
            " \"mp\": 29}, {\"reg\": \"FRA\", " +
            "\"rep\": 36, \"i\": 127, \"a\": 1688, \"mp\": 644}]}";
    public static final String TEST_KEY_FILE_NAME = "testKey";

    @TempDir
    static Path folder;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected NUVAService nuvaService;
    protected CLVRService cLVRService;
    protected SigningService signingService;
    protected CborService cborService;
    protected QrCodeService qrCodeService;

    protected TestKeyPairManager testKeyPairManager;


    protected FhirConversionUtil fhirToCLVRPayloadUtil;
    protected ObjectMapper objectMapper = new ObjectMapper();


    public ClvrTest() throws IOException {
        this.nuvaService = new NUVAService();
        this.cborService = new CborService();
        this.signingService = new SigningService();
        this.qrCodeService = new QrCodeService();
        this.cLVRService = new CLVRService(signingService,cborService,qrCodeService);
        this.fhirToCLVRPayloadUtil = new FhirConversionUtil(nuvaService);
        this.testKeyPairManager = new TestKeyPairManager(folder);
        BasicConfigurator.configure();
    }


    @Test
    public void nuvaLoading() {
        Optional<Vaccine> vaccine = nuvaService.findByCvx("20");
        assertEquals(true,vaccine.isPresent());
        assertEquals(602,vaccine.get().getCode());
//        logger.info("NUVA Vaccine for code found {}", vaccine.get());
    }

    @Test
    public void keyPairAvailable() {
        String keyFileBaseName = TEST_KEY_FILE_NAME; // The base name "testKey"
        KeyPair kp1 = null;
        KeyPair kp2 = null;
        try {
            // First run: The key files won't exist, so it will generate and save them.
            kp1 = testKeyPairManager.getOrCreateKeyPair(keyFileBaseName);
            logger.info("Private Key Algorithm: " + kp1.getPrivate().getAlgorithm());
            logger.info("\n--- Running again to demonstrate loading from file ---\n");
            // Second run: The key files now exist, so it will load them.
            kp2 = testKeyPairManager.getOrCreateKeyPair(keyFileBaseName);
            ECPublicKey ecPublicKey = (ECPublicKey) kp2.getPublic();
            ECKey jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) kp2.getPrivate())
//                    .keyID(UUID.randomUUID().toString()) // Optional: Assign a Key ID
                    .build();

            // 3. Print the public EC JWK parameters
            logger.info("Public Key : " + jwk.toPublicJWK().toJSONString());
            logger.info("Private Key : " + jwk.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(kp1);
        assertNotNull(kp2);
        //...
    }

    @BeforeEach
    void beforeEach() {

    }

    @AfterEach
    void afterEach() {
    }


}
