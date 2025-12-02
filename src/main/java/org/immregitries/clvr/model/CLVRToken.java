package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CLVRToken extends AbstractCLVRComponent {
    // more than 30 days validity
    public static final long DEFAULT_VALIDITY_TIME = 3000000L;

    /**
     * Issuer
     */
    @JsonProperty("1")
    private String issuer;

    /**
     * UNIX Expiration time
     */
    @JsonProperty("4")
    private long expirationTime;

    /**
     * Unix Issued time
     */
    @JsonProperty("6")
    private long issuedTime;

    @JsonProperty("-260")
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
}
