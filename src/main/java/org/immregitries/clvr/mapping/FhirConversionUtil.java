package org.immregitries.clvr.mapping;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregitries.clvr.NUVAService;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.VaccinationRecord;

public abstract class FhirConversionUtil<Bundle extends IBaseBundle, Immunization extends IDomainResource, Patient extends IDomainResource> {
    public static final String IMMUNIZATION_ORIGIN_EXTENDED = "http://hl7.org/fhir/ValueSet/immunization-origin";
    public static final String COUNTRY_ORIGIN_SYSTEM = "http://hl7.org/fhir/ValueSet/country";
    public static final String REPOSITORY_INDEX_SYSTEM = "repositoryIndexCoding";
    public static final String REFERENCE_SYSTEM = "reference";

    public NUVAService getNuvaService() {
        return nuvaService;
    }

    private NUVAService nuvaService;

    public FhirConversionUtil(NUVAService nuvaService) {
        this.nuvaService = nuvaService;
    }


    public abstract CLVRPayload toCLVRPayloadFromBundle(Bundle ipsBundle);

    public abstract VaccinationRecord toVaccinationRecord(Immunization immunization, Patient patient);

    public abstract CLVRPayload toEvCPayload(Patient patient);

    /**
     * Helper method to extract registry code from a FHIR profile URL.
     *
     * @param url The profile URL string.
     * @return The extracted registry code.
     */
    public String extractRegistryCodeFromUrl(String url) {
        // This is a simple example. A more robust implementation might be needed.
        // E.g., urn:example:registry:FRA -> FRA
        String[] parts = url.split(":");
        if (parts.length > 2) {
            return parts[parts.length - 1];
        }
        return "N/A";
    }

}
