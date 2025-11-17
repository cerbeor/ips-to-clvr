package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Nested class representing the "nam" structure.
 */
public class CLVRName extends AbstractCLVRComponent implements Serializable {
    @JsonProperty("fnt")
    private String familyName;

    @JsonProperty("gnt")
    private String givenName;

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
}
