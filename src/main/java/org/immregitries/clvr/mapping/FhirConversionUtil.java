package org.immregitries.clvr.mapping;

import com.syadem.nuva.Vaccine;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseCoding;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.immregitries.clvr.NUVAService;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRVaccinationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converts FHIR Bundle to
 *
 * @param <Bundle>       HAPIFHIR Bundle class
 * @param <Immunization> HAPIFHIR Immunization Class
 * @param <Patient>      HAPIFHIR Patient Class
 */
public abstract class FhirConversionUtil<Bundle extends IBaseBundle, Immunization extends IDomainResource, Patient extends IDomainResource> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    // TODO support more codes like snomed
    /**
     * Map for scanning of NUVA and searching of equivalent code, with <FHIR System, equivalent NUVA Nomenclature Code> an example is <"http://hl7.org/fhir/sid/cvx", "CVX">
     */
    public static final Map<String, String> SYSTEM_NUVA_MAP = Map.of(
            NUVASystems.NUVA_SYSTEM, NUVASystems.NUVA_NOMENCLATURE,
            MappingHelper.CVX_SYSTEM, NUVASystems.NUVA_CVX_NOMENCLATURE,
            MappingHelper.ATC_SYSTEM, NUVASystems.NUVA_ATC_NOMENCLATURE,
            MappingHelper.IPS_ATC_SYSTEM, NUVASystems.NUVA_ATC_NOMENCLATURE,
            MappingHelper.IPS_UV_SYSTEM, NUVASystems.NUVA_SCT_NOMENCLATURE,
            MappingHelper.SCT_SYSTEM, NUVASystems.NUVA_SCT_NOMENCLATURE
    );

    public NUVAService getNuvaService() {
        return nuvaService;
    }

    private final NUVAService nuvaService;

    public FhirConversionUtil(NUVAService nuvaService) {
        this.nuvaService = nuvaService;
    }

    public Optional<Vaccine> getNuvaSearchResult(List<? extends IBaseCoding> codingList) {
        return SYSTEM_NUVA_MAP.entrySet().stream()
                .map(entry -> getNuvaVaccine(codingList, entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    public Optional<Vaccine> getNuvaVaccine(List<? extends IBaseCoding> codings, String system, String nuvaNomenclature) {
//        codings.stream().forEach(coding -> logger.info("coding system  {} {} {}", coding.getSystem(), system,system.equals( coding.getSystem()) ));
        Optional<? extends IBaseCoding> coding = MappingHelper.filterCodingList(codings, system);
        if (coding.isPresent()) {
            return getNuvaService().findByCode(coding.get().getCode(), nuvaNomenclature);
        }
        return Optional.empty();
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
