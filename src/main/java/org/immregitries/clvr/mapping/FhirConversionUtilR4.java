package org.immregitries.clvr.mapping;

import com.syadem.nuva.Vaccine;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.immregitries.clvr.NUVAService;
import org.immregitries.clvr.model.CLVRName;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.CLVRVaccinationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.immregitries.clvr.mapping.FhirSystems.*;

public class FhirConversionUtilR4 extends FhirConversionUtil<Bundle, Immunization, Patient> {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public FhirConversionUtilR4(NUVAService nuvaService) {
        super(nuvaService);
    }

    @Override
    public CLVRVaccinationRecord toVaccinationRecord(Immunization immunization, Patient patient) {
        CLVRVaccinationRecord vaccinationRecord = new CLVRVaccinationRecord();

        // Current way SYADEM deals with the report

        for (Identifier identifier : immunization.getIdentifier()) {
            // based on sample found in tool
            // "identifier": [
            // {
            //  "system": "http://EVC/MasterRecord",
            //  "value\": "FRA/36/2021-05-05/1245"
            //  }
            // ]
            if (EVC_MASTER_RECORD.equals(identifier.getSystem())) {
                String[] strings = identifier.getValue().split("/");
                vaccinationRecord.setRegistryCode(strings[0]);
                vaccinationRecord.setRepositoryIndex(Integer.parseInt(strings[1]));
                vaccinationRecord.setReference(Integer.parseInt(strings[3]));
            }
        }

        CodeableConcept immunizationReportOrigin = immunization.getReportOrigin();
        // TODO map for registry of registries
        // TODO define FHIR CodeableConcept profile
        if (immunizationReportOrigin != null && immunizationReportOrigin.hasCoding()) {

            Coding countryCoding = MappingHelper.filterCodeableConcept(immunizationReportOrigin, COUNTRY_ORIGIN_SYSTEM);
            if (countryCoding.hasCode()) {
                // TODO make a Registry of Registries to identify country with FHIR ReportOrigin
                vaccinationRecord.setRegistryCode(countryCoding.getCode());
            }
            Coding repositoryIndexCoding = MappingHelper.filterCodeableConcept(immunizationReportOrigin, REPOSITORY_INDEX_SYSTEM);
            if (repositoryIndexCoding.hasCode()) {
                vaccinationRecord.setRepositoryIndex(Integer.parseInt(repositoryIndexCoding.getCode()));
            }
            Coding reference = MappingHelper.filterCodeableConcept(immunizationReportOrigin, REFERENCE_SYSTEM);
            if (reference.hasCode()) {
                vaccinationRecord.setReference(Integer.parseInt(reference.getCode()));
            }
            //Arbitrary value, since IIs sandbox is in no registry info
            if (StringUtils.isBlank(vaccinationRecord.getRegistryCode())) {
                vaccinationRecord.setRegistryCode("USA");
            }
        }

        try {
            if (vaccinationRecord.getReference() == 0) {
                vaccinationRecord.setReference(immunization.getIdElement().getIdPartAsBigDecimal().intValue());
            }
        } catch (NumberFormatException ignored) {
        }


        /*
         * Using Stream to avoid useless queries on NUVA, using a constant Map for to look for mappings
         */
        Optional<Vaccine> nuvaSearchResult = getNuvaSearchResult(immunization.getVaccineCode().getCoding());
        nuvaSearchResult.ifPresent(vaccine -> vaccinationRecord.setNuvaCode(vaccine.getCode()));

        // Calculate age in days (a)
        if (immunization.hasOccurrenceDateTimeType() && patient.hasBirthDate()) {
            Date immunizationDate = immunization.getOccurrenceDateTimeType().getValue();
            Date birthDate = patient.getBirthDate();
            long diffInMillies = Math.abs(immunizationDate.getTime() - birthDate.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            vaccinationRecord.setAgeInDays((int) diff);
        }

        return vaccinationRecord;
    }

    @Override
    public CLVRPayload toCLVRPayload(Patient patient) {
        CLVRPayload payload = new CLVRPayload();

        // Set version
        payload.setVersion("1.0.0");

        // Populate name
        if (patient.hasName()) {
            HumanName fhirName = patient.getNameFirstRep();
            CLVRName clvrName = new CLVRName();
            if (fhirName.hasFamily()) {
                clvrName.setFamilyName(fhirName.getFamily());
            }
            if (fhirName.hasGiven()) {
                clvrName.setGivenName(fhirName.getGivenAsSingleString());
            }
            payload.setName(clvrName);
        }

        // Populate date of birth
        if (patient.hasBirthDate()) {
            payload.setDateOfBirth(patient.getBirthDate());
        }

        // Populate person identifier (pid) if available, TODO functionality to choose Identifier
//		if (patient.hasIdentifier()) {
//			Identifier fhirIdentifier = patient.getIdentifier().stream()
//				.filter(businessIdentifier -> MRN_TYPE_VALUE.equals(businessIdentifier.getType().getCodingFirstRep().getCode()))
//				.findFirst()
//				.orElse(patient.getIdentifierFirstRep());
//			PersonIdentifier evcId = new PersonIdentifier();
//			if (fhirIdentifier.hasSystem()) {
//				evcId.setObjectIdentifier(fhirIdentifier.getSystem());
//			}
//			if (fhirIdentifier.hasValue()) {
//				evcId.setId(fhirIdentifier.getValue());
//			}
//			payload.setPersonIdentifier(evcId);
//		}

        // Initialize vaccination records list
        payload.setVaccinationRecords(new ArrayList<>());

        return payload;
    }

    @Override
    public CLVRPayload toCLVRPayloadFromBundle(Bundle ipsBundle) {
        Patient patient = null;
        List<Immunization> immunizations = new ArrayList<>();

        // Find the Patient and Immunization resources in the bundle
        for (Bundle.BundleEntryComponent entry : ipsBundle.getEntry()) {
            Resource resource = entry.getResource();
            if (resource.getResourceType() == ResourceType.Patient) {
                patient = (Patient) resource;
            } else if (resource.getResourceType() == ResourceType.Immunization) {
                immunizations.add((Immunization) resource);
            }
        }

        if (patient == null) {
            throw new IllegalArgumentException("FHIR IPS Bundle must contain a Patient resource.");
        }

        // Create the EvC Payload and populate patient data
        CLVRPayload payload = toCLVRPayload(patient);

        // Convert and add immunization records
        List<CLVRVaccinationRecord> CLVRVaccinationRecords = new ArrayList<>();
        for (Immunization immunization : immunizations) {
            CLVRVaccinationRecords.add(toVaccinationRecord(immunization, patient));
        }
        payload.setVaccinationRecords(CLVRVaccinationRecords);

        return payload;
    }
}
