package org.immregitries.clvr;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.fhir.r4.model.Bundle;
import org.immregitries.clvr.mapping.FhirConversionUtilR4;
import org.immregitries.clvr.model.CLVRPayload;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class FhirConversionUtilTest extends BaseCLVRTest {

    public static final String IPS_SAMPLE = "{\n" +
            "  \"resourceType\": \"Bundle\",\n" +
            "  \"type\": \"collection\",\n" +
            "  \"entry\": [\n" +
            "    {\n" +
            "      \"fullUrl\": \"http://EVC/Patient/this\",\n" +
            "      \"resource\": {\n" +
            "        \"text\": {\n" +
            "          \"status\": \"generated\",\n" +
            "          \"div\": \"<div xmlns='http://www.w3.org/1999/xhtml'>Patient John DOË</div>\"\n" +
            "        },\n" +
            "        \"id\": \"this\",\n" +
            "        \"resourceType\": \"Patient\",\n" +
            "        \"name\": [\n" +
            "          {\n" +
            "            \"family\": \"DOË\",\n" +
            "            \"given\": [\n" +
            "              \"John\"\n" +
            "            ]\n" +
            "          }\n" +
            "        ],\n" +
            "        \"birthDate\": \"2017-07-19\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"fullUrl\": \"http://EVC/Immunization/1\",\n" +
            "      \"resource\": {\n" +
            "        \"text\": {\n" +
            "          \"status\": \"generated\",\n" +
            "          \"div\": \"<div xmlns='http://www.w3.org/1999/xhtml'>REPEVAX administered on 2021-05-05</div>\"\n" +
            "        },\n" +
            "        \"id\": \"1\",\n" +
            "        \"resourceType\": \"Immunization\",\n" +
            "        \"identifier\": [\n" +
            "          {\n" +
            "            \"system\": \"http://EVC/MasterRecord\",\n" +
            "            \"value\": \"FRA/36/2021-05-05/1245\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"status\": \"completed\",\n" +
            "        \"vaccineCode\": {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"urn:oid:1.3.6.1.4.1.48601.1.1.1\",\n" +
            "              \"code\": \"VAC0029\",\n" +
            "              \"display\": \"REPEVAX\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"patient\": {\n" +
            "          \"reference\": \"Patient/this\"\n" +
            "        },\n" +
            "        \"occurrenceDateTime\": \"2021-05-05\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"fullUrl\": \"http://EVC/Immunization/2\",\n" +
            "      \"resource\": {\n" +
            "        \"text\": {\n" +
            "          \"status\": \"generated\",\n" +
            "          \"div\": \"<div  xmlns='http://www.w3.org/1999/xhtml'>QDENGA administered on 2022-03-03</div>\"\n" +
            "        },\n" +
            "        \"id\": \"2\",\n" +
            "        \"resourceType\": \"Immunization\",\n" +
            "        \"identifier\": [\n" +
            "          {\n" +
            "            \"system\": \"http://EVC/MasterRecord\",\n" +
            "            \"value\": \"FRA/36/2022-03-03/127\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"status\": \"completed\",\n" +
            "        \"vaccineCode\": {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"urn:oid:1.3.6.1.4.1.48601.1.1.1\",\n" +
            "              \"code\": \"VAC0644\",\n" +
            "              \"display\": \"QDENGA\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        \"patient\": {\n" +
            "          \"reference\": \"Patient/this\"\n" +
            "        },\n" +
            "        \"occurrenceDateTime\": \"2022-03-03\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";
    private FhirConversionUtilR4 fhirConversionUtilImpl;
    private FhirContext fhirContext = FhirContext.forR4();

    public FhirConversionUtilTest() throws IOException {
        super();
        fhirConversionUtilImpl = new FhirConversionUtilR4(nuvaService);
    }

    @Test
    void toCLVRPayload() {
    }



    @Test
    void toVaccinationRecord() {

    }


    /**
     * Currently invalid as the Registry identifier is maybe bound to change in its FHIR representation
     * @throws JsonProcessingException
     */
    @Test
    void toCLVRPayloadFromBundle() throws JsonProcessingException {
        String ipsSample = IPS_SAMPLE;
        String testSample = TEST_SAMPLE;
        Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, ipsSample);
        CLVRPayload clvrPayloadFromBundle = fhirConversionUtilImpl.toCLVRPayloadFromBundle(bundle);
//        assertNotNull(clvrPayloadFromBundle);
//        assertEquals(clvrPayloadFromBundle.toString(), objectMapper.readValue(testSample,CLVRPayload.class).toString());
    }
}