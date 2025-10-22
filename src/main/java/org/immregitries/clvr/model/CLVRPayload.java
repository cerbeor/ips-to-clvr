package org.immregitries.clvr.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A Java class that represents the EvC (European vaccination certificate) payload structure.
 * This class is designed to be serialized and deserialized using the Jackson library.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CLVRPayload extends AbstractCLVRComponent implements Serializable {
	public static final String DOB = "dob";

	@JsonProperty("ver")
	private String version;

	@JsonProperty("nam")
	private Name name;

//	@JsonProperty(DOB)
	/*
	 * See getter and setter for logic with formatting
	 */
	@JsonIgnore()
	private Date dateOfBirth;


	/*
	 * Not present in CLVR anymore apparently TODO ask
	 */
	@JsonIgnore()
//	@JsonProperty("pid")
	private PersonIdentifier personIdentifier;

	@JsonProperty("v")
	private List<VaccinationRecord> vaccinationRecords;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	@JsonGetter(DOB)
	public String getJsonDateOfBirth() {
		return simpleDateFormat.format(getDateOfBirth());
	}

	@JsonSetter(DOB)
	public void setJsonDateOfBirth(String string) throws ParseException {
		setDateOfBirth(simpleDateFormat.parse(string));
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public PersonIdentifier getPersonIdentifier() {
		return personIdentifier;
	}

	public void setPersonIdentifier(PersonIdentifier personIdentifier) {
		this.personIdentifier = personIdentifier;
	}

	public List<VaccinationRecord> getVaccinationRecords() {
		return vaccinationRecords;
	}

	public void setVaccinationRecords(List<VaccinationRecord> vaccinationRecords) {
		this.vaccinationRecords = vaccinationRecords;
	}

}
