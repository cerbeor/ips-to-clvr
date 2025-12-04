package org.immregitries.clvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.syadem.nuva.NUVA;
import com.syadem.nuva.SupportedLocale;
import com.syadem.nuva.Vaccine;
import org.immregitries.clvr.impl.CLVRServiceImpl;
import org.immregitries.clvr.impl.CborServiceImpl;
import org.immregitries.clvr.impl.QrCodeServiceImpl;
import org.immregitries.clvr.impl.SigningServiceImpl;
import org.immregitries.clvr.mapping.FhirConversionUtilR4;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class BaseCLVRTest {
    public static final boolean NOWRAP = false;

    public static final String TEST_SAMPLE_QR = "6BFOXN*TS0BI$ZDZRH AENUKSIL3W8 G2RTC RIQJDA+Q910OJL46KBG3:ZH-O9UVPQRHIY1VS1NQ1 WUXOE9Y431T3$KOGVV5U+%9SI6%RU/TUPRAAUIWVH$R1+ZE6%P/T1RM2JOJV 4G.K115WT0PG0QB00.I:S9M2JJHHIOI.CBPHNGG2M53%H2W58.0NW58:D9N/IR6H.14A$O.4VF679WCBKNK%O:OR.1UUHRKZ4N*1J4N.3V$/K166ZRLYKN% NL8SI01$UI*5GY14LUI.RNUWC01JZMQP-1K13J+6CBB/S1BCCLVA";
    public static final String TEST_ISSUER = "SYA";
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
    protected CLVRService clvrService;
    protected SigningService signingService;
    protected CborService cborService;
    protected QrCodeService qrCodeService;

    protected TestKeyPairManager testKeyPairManager;


    protected FhirConversionUtilR4 fhirConversionUtilR4;
    protected ObjectMapper objectMapper = new ObjectMapper();


    public BaseCLVRTest() throws IOException {
        NUVA nuva = NUVA.load(SupportedLocale.English);
        this.nuvaService = new NUVAService(nuva);
        this.cborService = new CborServiceImpl();
        this.signingService = new SigningServiceImpl();
        this.qrCodeService = new QrCodeServiceImpl();
        this.clvrService = new CLVRServiceImpl(signingService, cborService, qrCodeService);
        this.fhirConversionUtilR4 = new FhirConversionUtilR4(nuvaService);
        this.testKeyPairManager = new TestKeyPairManager(folder);
//        BasicConfigurator.configure();
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
            ECKey jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) kp2.getPublic())
//                    .keyID(UUID.randomUUID().toString()) // Optional: Assign a Key ID
                    .build();

            // 3. Print the public EC JWK parameters
//            logger.info("Key : " + jwk.toPublicJWK().toJSONString());
//            logger.info("Private Key : " + jwk.toJSONString());
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
