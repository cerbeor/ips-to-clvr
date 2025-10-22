package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

/**
 * Nested class representing the optional "pid" structure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonIdentifier extends AbstractCLVRComponent implements Serializable {
    @JsonProperty("oid")
    private String objectIdentifier;

    @JsonProperty("id")
    private String id;

    public String getObjectIdentifier() {
        return objectIdentifier;
    }

    public void setObjectIdentifier(String objectIdentifier) {
        this.objectIdentifier = objectIdentifier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
