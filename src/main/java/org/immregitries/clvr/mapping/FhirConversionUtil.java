package org.immregitries.clvr.mapping;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregitries.clvr.NUVAService;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRVaccinationRecord;

/**
 * Converts FHIR Bundle to
 * @param <Bundle>  HAPIFHIR Bundle class
 * @param <Immunization> HAPIFHIR Immunization Class
 * @param <Patient> HAPIFHIR Patient Class
 */
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


    /**
     * Converts a FHIR IPS Bundle resource to an CLVRPayload.
     * <p>
     * This method extracts patient and immunization data from the bundle
     * to create the CLVRPayload.
     * </p>
     *
     * @param ipsBundle The FHIR IPS Bundle resource to convert.
     * @return An CLVRPayload object containing the data from the bundle.
     */
    public abstract CLVRPayload toCLVRPayloadFromBundle(Bundle ipsBundle);

    /**
     * Converts a FHIR Immunization resource into an CLVR VaccinationRecord.
     * <p>
     * This method assumes the FHIR resource contains the necessary data points
     * to populate the VaccinationRecord object. It uses the `meta.profile` to determine
     * the registry code and extracts other information from the resource.
     * </p>
     *
     * @param immunization The FHIR Immunization resource to convert.
     * @param patient      The FHIR Patient resource to calculate age from.
     * @return A VaccinationRecord object populated with data from the FHIR resource.
     */
    public abstract CLVRVaccinationRecord toVaccinationRecord(Immunization immunization, Patient patient);

    /**
     * Converts a FHIR Patient resource into an CLVRPayload.
     * <p>
     * This method populates the demographic information of the CLVRPayload
     * from the FHIR Patient resource.
     * </p>
     *
     * @param patient The FHIR Patient resource to convert.
     * @return An CLVRPayload object populated with patient data.
     */
    public abstract CLVRPayload toCLVRPayload(Patient patient);

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
