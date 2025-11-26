---
marp: true
paginate: true
# theme: gaia
# style: |
#   section {
#     background-color: #ccc;
#   }
---

# IPS to CLVR

List of Registries

---

#Table of content

- [IPS to CLVR](#ips-to-clvr)
- [CLVR](#clvr)
- [Vaccination Record origin tracing](#vaccination-record-origin-tracing)
  - [CLVR Json payload before compression](#clvr-json-payload-before-compression)
- [Could we make this translatable for the United States ?](#could-we-make-this-translatable-for-the-united-states-)
- [FHIR ReportOrigin / Reference](#fhir-reportorigin--reference)
- [Proposition of a standard list of registries](#proposition-of-a-standard-list-of-registries)
  - [Automatic traceability ?](#automatic-traceability-)
- [Benefits](#benefits)
- [Stakes](#stakes)
  <!-- - [FHIR Immunization Data Source: R4 and R5](#fhir-immunization-data-source-r4-and-r5)
    - [**FHIR R4**](#fhir-r4)
    - [**FHIR R5**](#fhir-r5) -->

---

# CLVR

- European Vaccine Pass specification
- Everything within QR code Paradigm
  -> high compression and content size optimisation

---

# Vaccination Record origin tracing

| Definition            | name         | Compressed name | type            | bits uncompressed |
| :-------------------- | :----------- | :-------------- | :-------------- | :---------------- |
| Country code          | `registry`   | `reg`           | string (3 char) | 24                |
| Immunization Registry | `repository` | `rep`           | int             | 32                |
| Reference             | `index`      | `i`             | int             | 32                |

---

## CLVR Json payload before compression

```json
{
  "ver": "1.0.0",
  "nam": {
    "fnt": "DOË",
    "gnt": "John"
  },
  "dob": "2017-07-19",
  "v": [
    {
      "reg": "FRA", // Country code
      "rep": 36, // Repository indentifier
      "i": 1245, // Id within the repository
      "a": 1386, // Age in days when shot
      "mp": 29 // NUVA Vaccine code
    },
    {
      "reg": "FRA",
      "rep": 36,
      "i": 127,
      "a": 1688,
      "mp": 644
    }
  ]
}
```

---

# Could we make this translatable for the United States ?

---

# FHIR ReportOrigin / Reference

| Feature            | FHIR R4 (Immunization)          | FHIR R5 (Immunization)        |
| :----------------- | :------------------------------ | :---------------------------- |
| **Source Element** | **`reportOrigin`**              | **`informationSource`**       |
| **Typw**           | Codeable concept                | CodeableReference             |
| **Logic**          | List of Codings <System, Value> | CodeableConcept And Reference |
| **Cardinality**    | `reportOrigin`: $0..1$          | `informationSource`: $0..1$   |

---

```json
{
  // Within FHIR R4 Immunization
  "reportOrigin": {
    "coding": [
      {
        "system": "http://hl7.org/fhir/ValueSet/country",
        "code": "FRA",
        "display": "FRANCE"
      },
      {
        "system": "repositoryIndexCoding",
        "code": "36"
      },
      {
        "system": "reference",
        "code": "127"
      }
    ]
  }
}
```

---

# Proposition of a standard list of registries

Structure

- List of Organizations to define a ID for each registry
- Bundle with FHIR Endpoint Resource
  Automatic traceability ?

---

# Benefits

Unified processable Organizations and Endpoints
Automatisation of processes in traceability
Compressable References
Intersection of Code systems

---

# Stakes

- Registering
- Maintaining
- If including Endpoints
  - trusting endpoints ?
- Risk of request heavy workflows for endpoints
- Community based solution ?

<!-- ---

## FHIR Immunization Data Source: R4 and R5

#### **FHIR R4** -->
<!--
- **Element Name**: `Immunization.**reportOrigin**`
- **Data Type**: **CodeableConcept**
  - It specifies the **kind** of secondary source (e.g., patient recall, written record, school record).

#### **FHIR R5**

- **Element Name**: `Immunization.**informationSource**`
- **Data Type**: **CodeableReference** (A combination of CodeableConcept and Reference)
  - It uses a **CodeableConcept** to specify the **type** of source (e.g., 'provider', 'record', 'recall').
  - It uses a **Reference** to point to the actual **entity** that provided the information (e.g., a reference to the **`Patient`**, **`Practitioner`**, **`RelatedPerson`**, or **`Organization`**).

--- -->
