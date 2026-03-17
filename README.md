# 🌍 IPS to CLVR Prototype

A Java-based prototype for translating a **FHIR International Patient Summary (IPS)** into the new **European CLVR (Clinical Linkage and Vaccination Record)** QR Code specification.

This project is designed to be utilized as a dependency within the **AIRA NIST IIS Sandbox** environment to demonstrate the conversion and utilization of patient data from a standardized FHIR document into a compact, interoperable European format suitable for QR code deployment.

---

## 🚀 Project Overview

The core purpose of the `IPS-to-CLVR` library is to act as a **data bridge**. It consumes a FHIR R4 IPS document (usually a `Bundle` or `Composition` resource) and maps the relevant clinical information—such as immunizations, allergies, and patient demographics—to the specific, highly constrained data elements defined by the CLVR specification.

### Key Capabilities

* **FHIR IPS Ingestion:** Parses and validates input against the FHIR IPS Implementation Guide.
* **CLVR Mapping:** Translates structured FHIR data elements (e.g., `Immunization`, `AllergyIntolerance`) into the corresponding CLVR data fields.
* **CLVR Payload Generation:** Creates the final, compact byte-string payload conforming to the European CLVR QR Code specification.
* **QR Code Generation:** Encodes the final CLVR payload into a standardized **QR Code** for physical or digital presentation.
* **Generate Sample Vaccine Certificate with CLVR:** Produces a demonstration certificate document containing the QR code and human-readable CLVR data.

---

## 🛠️ Technology and Specifications Used

* **Language:** **Java (JDK 17+)**
* **Build Tool:** Maven
* **FHIR Library:** HAPI FHIR 
* **Data Encoding:** **CBOR** (Concise Binary Object Representation) for compact data structuring.
* **Security:** **COSE** (CBOR Object Signing and Encryption) for signing and ensuring payload integrity.
* **Vaccination Terminology:** **NUVA** (Nomenclature Unifiee des Vaccins - Unified Vaccine Nomenclature) for standardizing vaccine codes.
* **Document Handling:** **PdfBox** for potential downstream generation or reading of PDF documentation related to the summary.

### Prerequisites

* Java Development Kit (**JDK 17 or later**).
* Maven installed and configured.

---

## ⚙️ Installation and Usage

### 1. Building the Project as dependency

Clone the repository and build the project using Maven:

```bash
git clone https://github.com/cerbeor/ips-to-clvr
cd ips-to-clvr
mvn clean install
```

### 2. Run the Test UI

Project provides a simple testing UI using JFRAME, use maven `standalone` profile and run:

```bash
git clone https://github.com/cerbeor/ips-to-clvr
cd ips-to-clvr
mvn clean package -Pstandalone
java -jar target/ips-to-clvr-1.0-SNAPSHOT.jar
```

---
## Incoming
FHIR IPS Sub profile formalizing the concepts necessary in CLVR (Country, RepositoryIndex,...)