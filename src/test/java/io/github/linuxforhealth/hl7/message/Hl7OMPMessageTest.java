/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.linuxforhealth.hl7.message;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;
import io.github.linuxforhealth.fhir.FHIRContext;
import io.github.linuxforhealth.hl7.ConverterOptions;
import io.github.linuxforhealth.hl7.ConverterOptions.Builder;
import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;

public class Hl7OMPMessageTest {
  private static FHIRContext context = new FHIRContext();
  private static final ConverterOptions OPTIONS = new Builder().withValidateResource().build();


  @Test
  public void test_OMPO09_patient_encounter_present() throws IOException {
	  String hl7message =
		        "MSH|^~\\&|WHI_LOAD_GENERATOR|IBM_TORONTO_LAB|MEDORDER|IBM|20210407191342|9022934|OMP^O09|MSGID_bae9ce6a-e35d-4ff5-8d50-c5dde19cc1aa|T|2.5.1\n"
		        + "PID|1||0a3be81e-144b-4885-9b4e-c5cd33c8f038^^^MRN||Patient^Load^Generator||19690720|M|Patient^Alias^Generator|C|9999^^CITY^STATE^ZIP^CAN|COUNTY|(866)845-0900||ENGLISH^ENGLISH|Married|Baptist|Account_0a3be81e-144b-4885-9b4e-c5cd33c8f038|123-456-7890|||N|BIRTH PLACE\n"
		        + "NTE|1||Generated by whi-hl7-deidentifier service for patient_id: 0a3be81e-144b-4885-9b4e-c5cd33c8f038\n"
		        + "PV1||I|^^^Toronto^^5642 Hilly Av||||2905^Doctor^Attending^M^IV^^M.D|5755^Doctor^Referring^^Sr|770542^Doctor^Consulting^Jr||||||||59367^Doctor^Admitting||Visit_0a3be81e-144b-4885-9b4e-c5cd33c8f038|||||||||||||||||||||||||20210407191342\n"
		        + "ORC|OP|ACCESSION_c977e88c-fd4b-47b6-a4bc-71d04618d1c4|ACCESSION_c977e88c-fd4b-47b6-a4bc-71d04618d1c4|6279|||^3 times daily^^20210401|381227400|20210407191342|2739^BY^ENTERED|2799^BY^VERIFIED|3122^PROVIDER^ORDERING||(696)901-1300|20210407191342||||||ORDERING FAC NAME|ADDR^^CITY^STATE^ZIP^USA|(515)-290-8888|9999^^CITY^STATE^ZIP^CAN\n"
		        + "RXO|50111032701^hydrALAZINE HCl 25 MG Oral Tablet^NDC^^^^^^hydrALAZINE (APRESOLINE) 25 MG TABS||||||Take 1 tablet by mouth 3 (three) times daily.||G||120|tablet^tablet|0|FG5789740^GETREU^THOMAS^H.||||||||||^APRESOLINE\n"
		        + "RXR|PO^Oral";

      HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
      String json = ftv.convert(hl7message, OPTIONS);
      assertThat(json).isNotBlank();
      System.out.println(json);
      IBaseResource bundleResource = context.getParser().parseResource(json);
      assertThat(bundleResource).isNotNull();
      Bundle b = (Bundle) bundleResource;
      List<BundleEntryComponent> e = b.getEntry();
      List<Resource> patientResource = e.stream()
              .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
              .map(BundleEntryComponent::getResource).collect(Collectors.toList());
      assertThat(patientResource).hasSize(1);

      List<Resource> encounterResource = e.stream()
              .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
              .map(BundleEntryComponent::getResource).collect(Collectors.toList());
      assertThat(encounterResource).hasSize(1);

  }

}
