package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Nested class representing the vaccination record within the "v" array.
 */
public class CLVRVaccinationRecord extends AbstractCLVRComponent implements Serializable {
    @JsonProperty("reg")
    private String registryCode;

    @JsonProperty("rep")
    private int repositoryIndex;

    @JsonProperty("i")
    private int reference;

    @JsonProperty("a")
    private int ageInDays;

    @JsonProperty("mp")
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
