import org.junit.jupiter.api.io.TempDir;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class TestKeyPairManager {

    public Path folder;


    private final String ALGORITHM = "EC";
    private final String CURVE_NAME = "secp256r1"; // A common EC curve
    private final String PRIVATE_KEY_EXT = ".priv";
    private final String PUBLIC_KEY_EXT = ".pub";

    public TestKeyPairManager(Path folder) {
        this.folder = folder;
    }

    /**
     * Retrieves an EC KeyPair from the specified file base name, or generates and saves it if it doesn't exist.
     *
     * @param fileBaseName The base name for the key files (e.g., "testKey").
     * @return The loaded or newly generated KeyPair.
     * @throws Exception If there are issues with file I/O, security, or key handling.
     */
    public KeyPair getOrCreateKeyPair(String fileBaseName) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        Path privateKeyPath = folder.resolve(fileBaseName + PRIVATE_KEY_EXT);
        Path publicKeyPath = folder.resolve(fileBaseName + PUBLIC_KEY_EXT);

        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath) && privateKeyPath.toFile().length() > 0) {
            System.out.println("✅ Key pair found. Loading from files: " + fileBaseName + "...");
            return loadKeyPair(privateKeyPath, publicKeyPath);
        } else {
            System.out.println("⚠️ Key pair not found. Generating new EC key pair: " + fileBaseName + "...");
            KeyPair keyPair = generateKeyPair();
            saveKeyPair(keyPair, privateKeyPath, publicKeyPath);
            System.out.println("✨ New key pair generated and saved successfully.");
            return keyPair;
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Private Key Management Methods
    // ------------------------------------------------------------------------------------------------

    /**
     * Generates a new EC KeyPair using the specified curve.
     *
     * @return The newly generated KeyPair.
     * @throws NoSuchAlgorithmException           If the EC algorithm is not supported.
     * @throws InvalidAlgorithmParameterException If the curve name is invalid.
     */
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_NAME);
        keyGen.initialize(ecSpec, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    /**
     * Saves the KeyPair to the specified file paths.
     *
     * @param keyPair        The KeyPair to save.
     * @param privateKeyPath Path to save the private key.
     * @param publicKeyPath  Path to save the public key.
     * @throws IOException If there's an error writing to the files.
     */
    private void saveKeyPair(KeyPair keyPair, Path privateKeyPath, Path publicKeyPath) throws IOException {
        // Save Private Key (PKCS#8 format)
        try (FileOutputStream fos = new FileOutputStream(privateKeyPath.toFile())) {
            fos.write(keyPair.getPrivate().getEncoded());
        }

        // Save Public Key (X.509 format)
        try (FileOutputStream fos = new FileOutputStream(publicKeyPath.toFile())) {
            fos.write(keyPair.getPublic().getEncoded());
        }
    }

    /**
     * Loads a KeyPair from the specified file paths.
     *
     * @param privateKeyPath Path to the private key file.
     * @param publicKeyPath  Path to the public key file.
     * @return The loaded KeyPair.
     * @throws Exception If there are issues with file I/O or key decoding.
     */
    private KeyPair loadKeyPair(Path privateKeyPath, Path publicKeyPath) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        // Read the key bytes
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);

        // Get KeyFactory for EC
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);

        // Load Private Key
        PKCS8EncodedKeySpec pkcs8Spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = kf.generatePrivate(pkcs8Spec);

        // Load Public Key
        X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = kf.generatePublic(x509Spec);

        return new KeyPair(publicKey, privateKey);
    }
}