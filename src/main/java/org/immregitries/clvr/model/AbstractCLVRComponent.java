package org.immregitries.clvr.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;

/**
 * Unifies Serialization of subclasses into string using the Jackson mappings, allowed when keys are String only
 */
public abstract class AbstractCLVRComponent {
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

    @Override
    public String toString() {
        try {
            return JACKSON_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }
}
