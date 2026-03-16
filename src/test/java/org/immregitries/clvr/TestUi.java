package org.immregitries.clvr;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hl7.fhir.r4.model.Bundle;
import org.immregitries.clvr.impl.CLVRPdfServiceImpl;
import org.immregitries.clvr.model.AbstractCLVRComponent;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRToken;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.*;
import java.text.ParseException;
import java.util.UUID;

public class TestUi extends JFrame {
    public static final Color BACKGROUND = new Color(245, 245, 245);
    public static final int ERROR_ROWS = 8;
    public static final int ERROR_COLUMNS = 20;
    public static final Insets INSETS = new Insets(10, 5, 5, 5);
    private BaseCLVRTest baseCLVRTest =  new BaseCLVRTest();
    private CLVRPdfService clvrPdfService = new CLVRPdfServiceImpl(baseCLVRTest.nuvaService);
    private FhirContext fhirContext = FhirContext.forR4();

    /**
     * Main components
     */
    private JPanel mainPanel;
    private JTextArea globalErrorArea;

    /**
     * Accessible Input Fields
     */
    private JTextArea keyTextArea = new JTextArea(50, 40);
    private JTextArea fhirBundleArea = new JTextArea(50, 40);
    private JTextArea clvrTokenArea = new JTextArea(50, 40);
    private JTextArea qrTextArea = new JTextArea(2, 40);
    private JTextField issuerField = new JTextField(5);

    private KeyPair keyPair;

    public TestUi() throws IOException{
        setTitle("FHIR to Health QR Code Generator (Java 17)");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. Top Navigation Status ---
        JLabel statusLabel = new JLabel("Test IPS to CLVR", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.NORTH);


        // --- 2. Main Content (Steps) ---
        GridLayout gridLayout = new GridLayout();
        mainPanel = new JPanel(gridLayout);

        mainPanel.add(keyPanel());

        mainPanel.add(inputsPanel());
//        mainPanel.add(fhirPanel());
        mainPanel.add(clvrPanel());
        mainPanel.add(qrPanel());
        add(mainPanel, BorderLayout.CENTER);

        // --- 3. Error/Exception UI ---
        globalErrorArea = new JTextArea(ERROR_ROWS, ERROR_COLUMNS);
        globalErrorArea.setEditable(false);
        globalErrorArea.setForeground(Color.RED);
        globalErrorArea.setBackground(BACKGROUND);
        add(new JScrollPane(globalErrorArea), BorderLayout.SOUTH);
    }


    private JPanel keyPanel() {
        JLabel statusLabel = new JLabel("Key Required");
        // Main Container
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 1. Title (North)
        JLabel titleLabel = new JLabel("<html><h2>Signing keys</h2></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 2. Form Section (Center)
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 5, 5, 5); // Add top margin to separate
        formPanel.add(new JLabel("JWK key (EC)"), gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Give the textarea the extra vertical space
        gbc.fill = GridBagConstraints.BOTH;
        keyTextArea.setLineWrap(true);
        formPanel.add(new JScrollPane(keyTextArea), gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // 3. Actions & Status (South)
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));

        // Buttons Row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backward = generateKeyButton(statusLabel);
        JButton loadKey = getLoadKeyPairButton(statusLabel);
        buttonPanel.add(backward);
        buttonPanel.add(loadKey);

        // Status Label
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(Color.BLUE); // Default color

        southPanel.add(buttonPanel);
        southPanel.add(statusLabel);
        southPanel.add(Box.createVerticalStrut(10)); // Bottom padding

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    private @NonNull JButton getLoadKeyPairButton(JLabel keyErrorArea) {
        JButton loadKeyPair = new JButton("Load key Pair");
        loadKeyPair.addActionListener(e -> {
            try {
                keyErrorArea.setText(""); // Clear old errors
                keyPair = parseKeyPair();
                handleSuccess("Successfully loaded key Pair", keyErrorArea);
            } catch (Exception ex) {
                handleError(ex, keyErrorArea, "Key pair could not be loaded");
            }
        });
        return loadKeyPair;
    }

    private @NonNull JButton generateKeyButton(JLabel keyErrorArea) {
        JButton backward = new JButton("Example");
        backward.addActionListener(e -> {
            try {
                keyErrorArea.setText(""); // Clear old errors
                JWK jwk = new ECKeyGenerator(Curve.P_256)
                        .keyID(UUID.randomUUID().toString())
                        .keyUse(KeyUse.SIGNATURE)
                        .generate();
                keyPair = jwk.toECKey().toKeyPair();
                keyTextArea.setText(prettyPrintJson(jwk.toJSONString()));
            } catch (Exception ex) {
                keyErrorArea.setForeground(Color.RED);
                handleError(ex, keyErrorArea, null);
            }
        });
        return backward;
    }

    public JPanel inputsPanel() {
        JLabel statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        // Main Container
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // 1. Title (North)
        JLabel titleLabel = new JLabel("<html><h2>Bundle Processor</h2></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 2. Form Section (Center)
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Issuer Label & Field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Issuer (3 letter code):"), gbc);

        gbc.gridy = 1;
        formPanel.add(issuerField, gbc);

        // FHIR Bundle Label & TextArea
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 5, 5, 5); // Add top margin to separate
        formPanel.add(new JLabel("FHIR Bundle:"), gbc);

        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Give the textarea the extra vertical space
        gbc.fill = GridBagConstraints.BOTH;
        fhirBundleArea.setLineWrap(true);
        formPanel.add(new JScrollPane(fhirBundleArea), gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // 3. Actions & Status (South)
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));

        // Buttons Row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnExample = genGetFhirExampleButton(statusLabel);
        JButton btnConvert =  fhirForwardButton(statusLabel);
        buttonPanel.add(btnExample);
        buttonPanel.add(btnConvert);

        // Status Label
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(Color.BLUE); // Default color

        southPanel.add(buttonPanel);
        southPanel.add(statusLabel);
        southPanel.add(Box.createVerticalStrut(10)); // Bottom padding

        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private @NonNull JButton genGetFhirExampleButton(JLabel statusLabel) {
        JButton getExampleFhirBundle = new JButton("Example");
        getExampleFhirBundle.addActionListener(e -> {
            try {
                issuerField.setText("SYA");
                fhirBundleArea.setText(FhirConversionUtilTest.IPS_SAMPLE_R4_BUNDLE);
            } catch (Exception ex) {
                handleError(ex, statusLabel, null);
            }
        });
        return getExampleFhirBundle;
    }

    private @NonNull JButton fhirForwardButton(JLabel fhirErrorArea) {
        JButton forward = new JButton("Convert FHIR Bundle >>");
        forward.addActionListener(e -> {
            try {
                fhirErrorArea.setText("");
                if (fhirBundleArea.getText().isEmpty()) throw new IllegalArgumentException("FHIR Bundle cannot be empty!");
                Bundle fhirBundle = fhirContext.newJsonParser().setPrettyPrint(true).parseResource(Bundle.class, fhirBundleArea.getText());
                CLVRPayload clvrPayloadFromBundle = baseCLVRTest.fhirConversionUtilR4.toCLVRPayloadFromBundle(fhirBundle);
                CLVRToken clvrToken = new CLVRToken(clvrPayloadFromBundle, issuerField.getText());
                clvrTokenArea.setText(prettyPrintJson(clvrToken.toString()));
                handleSuccess("Parsed and Converted FHIR Bundle", fhirErrorArea);
            } catch (Exception ex) {
                handleError(ex, fhirErrorArea, null);
            }
        });
        return forward;
    }

    private JPanel clvrPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = INSETS;

        panel.add(new JLabel("<html><h2>CLVR Token</h2></html>"), gbc);

        gbc.gridx = 0;
//        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        panel.add(new JScrollPane(clvrTokenArea), gbc);

        JLabel clvrErrorArea = new JLabel();
        clvrErrorArea.setBackground(BACKGROUND);
        add(new JScrollPane(clvrErrorArea), BorderLayout.SOUTH);


        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.fill = 0;
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        JButton genQrButton = getGenQrButton(clvrErrorArea);

        buttonsPanel.add(genQrButton, gbc);
        panel.add(buttonsPanel, gbc);
        return panel;
    }

    private @NonNull JButton getGenQrButton(JLabel clvrErrorArea) {
        JButton genQrButton = new JButton("Generate QR Code >>");
        genQrButton.addActionListener(e -> {
            try {
                CLVRToken clvrToken = parseClvrToken();
                qrTextArea.setText(baseCLVRTest.clvrService.encodeCLVRtoQrCode(clvrToken, keyPair));
                handleSuccess("Generated QR Code", clvrErrorArea);
            } catch (Exception ex) {
                handleError(ex, clvrErrorArea, null);
            }
        });
        return genQrButton;
    }



    private JPanel qrPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = INSETS;

        panel.add(new JLabel("<html><h2>Qr Code</h2></html>"), gbc);

        gbc.gridx = 0;
//        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        panel.add(new JScrollPane(qrTextArea), gbc);

        JLabel qrErrorArea = new JLabel();
        qrErrorArea.setForeground(Color.RED);
        qrErrorArea.setBackground(BACKGROUND);
        add(new JScrollPane(qrErrorArea), BorderLayout.SOUTH);

        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.fill = 0;
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        JButton backward = new JButton("<< Parse CLVR Token");
        backward.addActionListener(e -> {
            try {
                String clvrTokenString = baseCLVRTest.clvrService.decodeFullQrCode(qrTextArea.getText().getBytes(), keyPair).toString();
                clvrTokenArea.setText(prettyPrintJson(clvrTokenString));
            } catch (Exception ex) {
                handleError(ex, qrErrorArea, null);
            }
        });
        JButton printQrButton = printQrButton(qrErrorArea);
        JButton printPdfButton = getPrintPdfButton(qrErrorArea);
        JButton exportPdfButton = exportPdfButton(qrErrorArea);

        buttonsPanel.add(backward, gbc);
        buttonsPanel.add(printQrButton, gbc);
        buttonsPanel.add(printPdfButton, gbc);
        buttonsPanel.add(exportPdfButton, gbc);

        panel.add(buttonsPanel, gbc);
        return panel;
    }

    private @NonNull JButton getPrintPdfButton(JLabel errorArea) {
        JButton printPdfButton = new JButton("Show PDF");
        printPdfButton.addActionListener(e -> {
            try {
                errorArea.setText("");
                PDDocument pdDocument = clvrPdfService.createPdf(parseClvrToken(), qrTextArea.getText().getBytes(), "Test-window");
                PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
                BufferedImage image = pdfRenderer.renderImageWithDPI(0, 75, ImageType.RGB);

                // 3. Display in a Popup (or add to your main panel)
                JLabel label = new JLabel(new ImageIcon(image));
                JOptionPane.showMessageDialog(this, label, "Generated and show PDF", JOptionPane.PLAIN_MESSAGE);
                handleSuccess("Generated and shown PDF", errorArea);
            } catch (Exception ex) {
                handleError(ex, errorArea, null);
            }
        });
        return printPdfButton;
    }

    private @NonNull JButton exportPdfButton(JLabel errorArea) {
        JButton printPdfButton = new JButton("Export PDF");
        printPdfButton.addActionListener(e -> {
            try {
                errorArea.setText("");

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter("Pdf Files", ".pdf"));
                // Optional: set the initial directory
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

                int result = fileChooser.showOpenDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    String fileName = "ips-to-clvr-export.pdf";

                    // Create the new file in the selected directory
                    File newFile = new File(selectedDirectory.getAbsolutePath(), fileName);


                    // Do something with the file path, e.g., display it in a dialog or a JTextField
                    JOptionPane.showMessageDialog(this, "Selected File Path: " + newFile.getAbsolutePath(), "Export PDF", JOptionPane.PLAIN_MESSAGE);
                    // Example of setting the path to a JTextField (assuming 'textField' exists)
                    PDDocument pdDocument = clvrPdfService.createPdf(parseClvrToken(), qrTextArea.getText().getBytes(), "Test-window");
                    pdDocument.save(newFile);
                    handleSuccess("Exported PDF", errorArea);
                } else {
                    JOptionPane.showMessageDialog(this, "File selection cancelled.");
                }

            } catch (Exception ex) {
                handleError(ex, errorArea, "Pdf Export failed.");
            }
        });
        return printPdfButton;
    }

    private @NonNull JButton printQrButton(JLabel qrErrorArea) {
        JButton printQrButton = new JButton("Show QR Code");
        printQrButton.addActionListener(e -> {
            try {
                qrErrorArea.setText("");
                // 1. Generate the BitMatrix (300x300 pixels)
                BitMatrix matrix = new MultiFormatWriter().encode(
                        qrTextArea.getText(),
                        BarcodeFormat.QR_CODE,
                        300, 300
                );

                // 2. Convert Matrix to BufferedImage
                BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

                // 3. Display in a Popup (or add to your main panel)
                JLabel label = new JLabel(new ImageIcon(image));
                JOptionPane.showMessageDialog(this, label, "Generated Health QR Code", JOptionPane.PLAIN_MESSAGE);
                handleSuccess("QR printed", qrErrorArea);
            } catch (Exception ex) {
                handleError(ex, qrErrorArea, null);
            }
        });
        return printQrButton;
    }

    private void handleException(Exception e) {
        globalErrorArea.setForeground(Color.RED);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        globalErrorArea.setText("⚠️ LATEST ERROR:\n" + sw.toString());
    }

    private void handleSuccess(String message, JLabel errorArea) {
        globalErrorArea.setText("");
        errorArea.setForeground(Color.GREEN);
        errorArea.setText("Success:\n" + message);
    }

    private void handleError(Exception ex, JLabel errorArea, String message) {
        errorArea.setForeground(Color.RED);
        errorArea.setText("Error:\n" + StringUtils.firstNonBlank(message, ex.getMessage()));
        handleException(ex);
    }

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String prettyPrintJson(String jsonString) {
        // Create a JSONObject from the string
        JsonElement el = JsonParser.parseString(jsonString);
        // Use toString(int indentFactor) to format with 4 spaces
        return gson.toJson(el);
    }

    private KeyPair parseKeyPair() throws ParseException, JOSEException {
        try {
            return JWK.parse(keyTextArea.getText()).toRSAKey().toKeyPair();
        } catch (ClassCastException classCastException) {
            return JWK.parse(keyTextArea.getText()).toECKey().toKeyPair();
        }
    }

    private CLVRToken parseClvrToken() throws JsonProcessingException {
        return AbstractCLVRComponent.OBJECT_MAPPER.readValue(clvrTokenArea.getText().strip(), CLVRToken.class);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new TestUi().setVisible(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}