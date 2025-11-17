package org.immregitries.clvr;

import com.syadem.nuva.Vaccine;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.immregitries.clvr.mapping.MappingHelper;
import org.immregitries.clvr.model.CLVRPayload;
import org.immregitries.clvr.model.Name;
import org.immregitries.clvr.model.PersonIdentifier;
import org.immregitries.clvr.model.VaccinationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.immregitries.clvr.mapping.MappingHelper.MRN_TYPE_VALUE;

public class FhirConversionUtilR4 extends FhirConversionUtil<Bundle, Immunization, Patient> {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public FhirConversionUtilR4(NUVAService nuvaService) {
		super(nuvaService);
	}

	/**
	 * Converts a FHIR Immunization resource into an EvC VaccinationRecord.
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
	@Override
	public VaccinationRecord toVaccinationRecord(Immunization immunization, Patient patient) {
		VaccinationRecord record = new VaccinationRecord();

		CodeableConcept immunizationReportOrigin = immunization.getReportOrigin();
        // TODO map for registry of registries
		// TODO define FHIR CodeableConcept profile
		if (immunizationReportOrigin != null && immunizationReportOrigin.hasCoding()) {

			Coding countryCoding = MappingHelper.filterCodeableConcept(immunizationReportOrigin, COUNTRY_ORIGIN_SYSTEM);
			if (countryCoding.hasCode()) {
				// TODO make a Registry of Registries to identify country with FHIR ReportOrigin
				countryCoding.setCode(countryCoding.getCode());
			}
			Coding repositoryIndexCoding = MappingHelper.filterCodeableConcept(immunizationReportOrigin, REPOSITORY_INDEX_SYSTEM);
			if (repositoryIndexCoding.hasCode()) {
				record.setRepositoryIndex(Integer.parseInt(repositoryIndexCoding.getCode()));
			}
			Coding reference = MappingHelper.filterCodeableConcept(immunizationReportOrigin, REFERENCE_SYSTEM);
			if (reference.hasCode()) {
				record.setReference(Integer.parseInt(reference.getCode()));
			}
			//Arbitrary value, since IIs sandbox is in no registry info
			if (StringUtils.isBlank(record.getRegistryCode())) {
				record.setRegistryCode("USA");
			}
		}

		try {
			record.setRepositoryIndex(immunization.getIdElement().getIdPartAsBigDecimal().intValue());
		} catch (NumberFormatException ignored) {
		}


		// TODO support more codes like snomed
		Coding cvxCoding = org.immregitries.clvr.mapping.MappingHelper.filterCodeableConcept(immunization.getVaccineCode(), MappingHelper.CVX_SYSTEM);
		if (cvxCoding.hasCode()) {
			Optional<Vaccine> byCvx = getNuvaService().findByCvx(cvxCoding.getCode());
			byCvx.ifPresent(vaccine -> record.setNuvaCode(vaccine.getCode()));
		}

		// Calculate age in days (a)
		if (immunization.hasOccurrenceDateTimeType() && patient.hasBirthDate()) {
			Date immunizationDate = immunization.getOccurrenceDateTimeType().getValue();
			Date birthDate = patient.getBirthDate();
			long diffInMillies = Math.abs(immunizationDate.getTime() - birthDate.getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			record.setAgeInDays((int) diff);
		}

		return record;
	}

	/**
	 * Converts a FHIR Patient resource into an EvCPayload.
	 * <p>
	 * This method populates the demographic information of the EvCPayload
	 * from the FHIR Patient resource.
	 * </p>
	 *
	 * @param patient The FHIR Patient resource to convert.
	 * @return An EvCPayload object populated with patient data.
	 */
	@Override
	public CLVRPayload toEvCPayload(Patient patient) {
		CLVRPayload payload = new CLVRPayload();

		// Set version
		payload.setVersion("1.0.0");

		// Populate name
		if (patient.hasName()) {
			HumanName fhirName = patient.getNameFirstRep();
			Name evcName = new Name();
			if (fhirName.hasFamily()) {
				evcName.setFamilyName(fhirName.getFamily());
			}
			if (fhirName.hasGiven()) {
				evcName.setGivenName(fhirName.getGivenAsSingleString());
			}
			payload.setName(evcName);
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

	/**
	 * Converts a FHIR IPS Bundle resource to an EvCPayload.
	 * <p>
	 * This method extracts patient and immunization data from the bundle
	 * to create the EvCPayload.
	 * </p>
	 *
	 * @param ipsBundle The FHIR IPS Bundle resource to convert.
	 * @return An EvCPayload object containing the data from the bundle.
	 */
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
		CLVRPayload payload = toEvCPayload(patient);

		// Convert and add immunization records
		List<VaccinationRecord> vaccinationRecords = new ArrayList<>();
		for (Immunization immunization : immunizations) {
			vaccinationRecords.add(toVaccinationRecord(immunization, patient));
		}
		payload.setVaccinationRecords(vaccinationRecords);

		return payload;
	}
}
