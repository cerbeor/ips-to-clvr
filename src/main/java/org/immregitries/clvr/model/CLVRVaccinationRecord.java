package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Nested class representing the vaccination record within the "v" array.
 */
public class CLVRVaccinationRecord extends AbstractCLVRComponent implements Serializable {
    public static final String REG = "reg";
    public static final String REP = "rep";
    public static final String I = "i";
    public static final String MP = "mp";
    public static final String A = "a";
    @JsonProperty(REG)
    private String registryCode;

    @JsonProperty(REP)
    private int repositoryIndex;

    @JsonProperty(I)
    private int reference;

    @JsonProperty(A)
    private int ageInDays;

    @JsonProperty(MP)
    private int nuvaCode;

    public String getRegistryCode() {
        return registryCode;
    }

    public void setRegistryCode(String registryCode) {
        this.registryCode = registryCode;
    }

    public int getRepositoryIndex() {
        return repositoryIndex;
    }

    public void setRepositoryIndex(int repositoryIndex) {
        this.repositoryIndex = repositoryIndex;
    }

    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public int getAgeInDays() {
        return ageInDays;
    }

    public void setAgeInDays(int ageInDays) {
        this.ageInDays = ageInDays;
    }

    public int getNuvaCode() {
        return nuvaCode;
    }

    public void setNuvaCode(int nuvaCode) {
        this.nuvaCode = nuvaCode;
    }
}
