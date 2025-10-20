import com.fasterxml.jackson.databind.ObjectMapper;
import org.immregitries.clvr.*;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ClvrTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NUVAService nuvaService;
    private CLVRService cLVRService;
    private SigningService signingService;
    private CborService cborService;
    private QrCodeService qrCodeService;

    private TestKeyPairManager testKeyPairManager;


    private FhirConversionUtil fhirToCLVRPayloadUtil;
    private ObjectMapper objectMapper = new ObjectMapper();


    public ClvrTest() throws IOException {
        this.nuvaService = new NUVAService();
        this.cborService = new CborService();
        this.signingService = new SigningService();
        this.qrCodeService = new QrCodeService();
        this.cLVRService = new CLVRService(signingService,cborService,qrCodeService);
        this.fhirToCLVRPayloadUtil = new FhirConversionUtil(nuvaService);
        this.testKeyPairManager = new TestKeyPairManager(folder);
    }


    @Test
    public void nuvaLoading() {
        nuvaService.findByCvx("12");
        assertEquals(1,1);

    }

    @Test
    public void keyPairAvailable() {
        String keyFileBaseName = "testKey"; // The base name "testKey"
        KeyPair kp1 = null;
        try {
            // First run: The key files won't exist, so it will generate and save them.
            kp1 = testKeyPairManager.getOrCreateKeyPair(keyFileBaseName);
            logger.info("Private Key Algorithm: " + kp1.getPrivate().getAlgorithm());

            logger.info("\n--- Running again to demonstrate loading from file ---\n");

            // Second run: The key files now exist, so it will load them.
            KeyPair kp2 = testKeyPairManager.getOrCreateKeyPair(keyFileBaseName);
            logger.info("Public Key Format: " + kp2.getPublic().getFormat());

            // You can optionally delete the files after testing
            // Files.delete(Paths.get(keyFileBaseName + PRIVATE_KEY_EXT));
            // Files.delete(Paths.get(keyFileBaseName + PUBLIC_KEY_EXT));
            // logger.info("\nClean up: Key files deleted.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(kp1);
        folder.delete();
        //...
    }

    @BeforeEach
    void beforeEach() {

    }

    @AfterAll
    void beforeEach() {
        folder.delete();
    }


}
