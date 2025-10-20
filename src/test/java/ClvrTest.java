import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immregitries.clvr.EvcService;
import org.immregitries.clvr.FhirToEvcUtil;
import org.immregitries.clvr.NuvaService;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ClvrTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NuvaService nuvaService;
    private EvcService evcService;
    private FhirToEvcUtil fhirToEvcUtil;
    private ObjectMapper objectMapper = new ObjectMapper();


    public ClvrTest() throws IOException {
        this.nuvaService = new NuvaService();
        this.evcService = new EvcService();
        this.fhirToEvcUtil = new FhirToEvcUtil(nuvaService);
    }


    @Test
    public void nuvaLoading() {
        nuvaService.findByCvx("12");
        assertEquals(1,1);

    }

    @SetUp
    public void keyPairAvailable() {
        String keyFileBaseName = "testKey"; // The base name "testKey"
        try {
            // First run: The key files won't exist, so it will generate and save them.
            KeyPair kp1 = TestKeyPairManager.getOrCreateKeyPair(keyFileBaseName);
            System.out.println("Private Key Algorithm: " + kp1.getPrivate().getAlgorithm());

            System.out.println("\n--- Running again to demonstrate loading from file ---\n");

            // Second run: The key files now exist, so it will load them.
            KeyPair kp2 = TestKeyPairManager.getOrCreateKeyPair(keyFileBaseName);
            System.out.println("Public Key Format: " + kp2.getPublic().getFormat());

            // You can optionally delete the files after testing
            // Files.delete(Paths.get(keyFileBaseName + PRIVATE_KEY_EXT));
            // Files.delete(Paths.get(keyFileBaseName + PUBLIC_KEY_EXT));
            // System.out.println("\nClean up: Key files deleted.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        //...
    }

    @BeforeEach
    void beforeEach() {

    }


}
