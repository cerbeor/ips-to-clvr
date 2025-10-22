package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CLVRToken extends AbstractCLVRComponent {

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
     * @param clvrPayload
     */
    public CLVRToken(CLVRPayload clvrPayload) {
        issuer = "SYA";
        issuedTime = System.currentTimeMillis() / 1000L;
        expirationTime = issuedTime + 1000000L;
        this.clvrPayload = clvrPayload;
    }

    /**
     * Contructor with only issued time set
     */
    public CLVRToken() {
        issuedTime = System.currentTimeMillis() / 1000L;
    }

    ;
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
