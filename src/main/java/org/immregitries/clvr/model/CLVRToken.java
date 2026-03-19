package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CLVRToken {
    // more than 30 days validity
    public static final long DEFAULT_VALIDITY_TIME = 3000000L;
    public static final String ISSUER_KEY = "1";
    public static final String EXPIRATION_TIME_KEY = "4";
    public static final String ISSUED_TIME_KEY = "6";
    public static final String PAYLOAD_KEY = "-260";

    /**
     * Issuer
     */
    @JsonProperty(ISSUER_KEY)
    private String issuer;

    /**
     * UNIX Expiration time
     */
    @JsonProperty(EXPIRATION_TIME_KEY)
    private long expirationTime;

    /**
     * Unix Issued time
     */
    @JsonProperty(ISSUED_TIME_KEY)
    private long issuedTime;

    @JsonProperty(PAYLOAD_KEY)
    private CLVRPayload clvrPayload;

    /**
     * Constructor with default values
     *
     * @param clvrPayload
     * @param issuer
     */
    public CLVRToken(CLVRPayload clvrPayload, String issuer) {
        this.issuer = issuer;
        this.issuedTime = Instant.now().getEpochSecond();
        this.expirationTime = this.issuedTime + DEFAULT_VALIDITY_TIME;
        this.clvrPayload = clvrPayload;
    }

    /**
     * Constructor with only issued time set
     */
    public CLVRToken() {
        this.issuedTime = Instant.now().getEpochSecond();
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public long getIssuedTime() {
        return issuedTime;
    }

    public void setIssuedTime(long issuedTime) {
        this.issuedTime = issuedTime;
    }

    public CLVRPayload getClvrPayload() {
        return clvrPayload;
    }

    public void setClvrPayload(CLVRPayload clvrPayload) {
        this.clvrPayload = clvrPayload;
    }

    /**
     * Required for using ints as key
     * @return
     */
    public Map<Integer, Object> toCBORMap() {
        Map<Integer, Object> map = new HashMap<>();

        // Using Integer keys as CBOR maps often use labels (ints) to save space
        map.put(NumberUtils.toInt(ISSUER_KEY), issuer);
        map.put(NumberUtils.toInt(EXPIRATION_TIME_KEY), expirationTime);
        map.put(NumberUtils.toInt(ISSUED_TIME_KEY), issuedTime);
        map.put(NumberUtils.toInt(PAYLOAD_KEY), clvrPayload);

        return map;
    }

    /**
     * For display in Test UI
     * @return
     */
    public String toPrettyString() {
        String clvrPayloadString = "";
        if (clvrPayload != null) {
            clvrPayloadString = clvrPayload.toString();
        }
        return String.format("""
        {
          %s: "%s",
          %s: "%s",
          %s: "%s",
          %s: %s
        }
        """,
                ISSUER_KEY, issuer,
                EXPIRATION_TIME_KEY, expirationTime,
                ISSUED_TIME_KEY, issuedTime,
                PAYLOAD_KEY,  clvrPayloadString
        );
    }

    @Override
    public String toString() {
        return String.format("""
        {%s:"%s",%s:"%s",%s:"%s",%s:%s}
        """,
                ISSUER_KEY, issuer,
                EXPIRATION_TIME_KEY, expirationTime,
                ISSUED_TIME_KEY, issuedTime,
                PAYLOAD_KEY, clvrPayload
        );
    }

    public static CLVRToken fromCBORMap(Map<Object, Object> cborData) throws JsonProcessingException {
        CLVRToken clvrToken = new CLVRToken();

        // 1. Extract Issuer (Key as Integer, Value as String)
        Object issuerVal = cborData.get(NumberUtils.toInt(ISSUER_KEY));
        if (issuerVal != null) {
            clvrToken.setIssuer(issuerVal.toString());
        }

        // 2. Extract Timestamps (Handle as Long/Number)
        Object expVal = cborData.get(NumberUtils.toInt(EXPIRATION_TIME_KEY));
        if (expVal instanceof Number) {
            clvrToken.setExpirationTime(((Number) expVal).longValue());
        }

        Object iatVal = cborData.get(NumberUtils.toInt(ISSUED_TIME_KEY));
        if (iatVal instanceof Number) {
            clvrToken.setIssuedTime(((Number) iatVal).longValue());
        }

        // 3. Extract Payload (Usually a byte array or nested Map in CBOR)
        Object payloadVal = cborData.get(NumberUtils.toInt(PAYLOAD_KEY));
        if (payloadVal instanceof String) {
            clvrToken.setClvrPayload(AbstractCLVRComponent.JACKSON_MAPPER.readValue(payloadVal.toString(),CLVRPayload.class));
        } else if (payloadVal instanceof Map) {
            clvrToken.setClvrPayload(AbstractCLVRComponent.JACKSON_MAPPER.convertValue(payloadVal,CLVRPayload.class));
        }

        return clvrToken;
    }

    public static CLVRToken fromString(String input) throws JsonProcessingException {
        // 1. Clean the input (remove braces and extra whitespace)
        String clean = input.trim().replaceFirst("^\\{", "").replaceFirst("\\}$", "");

        // 2. Split by comma, but only commas followed by a "key:" pattern
        // This protects commas inside the payload string
        String[] pairs = clean.split("\\s*,\\s*(?=-?\\d+\\s*:)");

        CLVRToken token = new CLVRToken();
        for (String pair : pairs) {
            String[] parts = pair.split(":", 2);
            if (parts.length < 2) continue;

            String key = parts[0].trim();
            // Remove the wrapping double quotes from the value
            String value = parts[1].trim().replaceAll("^\"|\"$", "");

            switch (key) {
                case ISSUER_KEY -> token.setIssuer(value);
                case EXPIRATION_TIME_KEY -> token.setExpirationTime(Long.parseLong(value));
                case ISSUED_TIME_KEY -> token.setIssuedTime(Long.parseLong(value));
                case PAYLOAD_KEY -> token.setClvrPayload(CLVRPayload.fromString(value));
            }
        }
        return token;
    }

}
