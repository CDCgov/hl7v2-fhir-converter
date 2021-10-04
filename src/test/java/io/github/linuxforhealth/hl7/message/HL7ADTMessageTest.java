/*
 * (C) Copyright IBM Corp. 2020, 2021
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.linuxforhealth.fhir.FHIRContext;
import io.github.linuxforhealth.hl7.ConverterOptions;
import io.github.linuxforhealth.hl7.ConverterOptions.Builder;
import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HL7ADTMessageTest {
        private static FHIRContext context = new FHIRContext();
        private static final Logger LOGGER = LoggerFactory.getLogger(HL7ADTMessageTest.class);
        private static final ConverterOptions OPTIONS = new Builder().withValidateResource().build();
        private static final ConverterOptions OPTIONS_PRETTYPRINT = new Builder().withBundleType(BundleType.COLLECTION)
                        .withValidateResource().withPrettyPrint().build();

        @Test
        public void test_adt_a01_basic_resources_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A01|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 resources: 1 Encounter, 1 Patient
                assertThat(e.size()).isEqualTo(2);

                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);
        }

        @Test
        public void test_adt_a01_full_resources_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A01|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n"
                                + "PV2|||||||||||||||||||||||||AI|||||||||||||C|\n"
                                + "OBX|1|TX|1234^some text^SCT||First line: ECHOCARDIOGRAPHIC REPORT||||||F||\n"
                                + "AL1|1|DA|1605^acetaminophen^L|MO|Muscle Pain~hair loss\r" 
                                + "DG1|1||B45678|||A|\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 5 resources: Encounter, Patient, Observation, AllergyIntolerance, and Condition
                assertThat(e.size()).isEqualTo(5);

                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

                List<Resource> observationResource = e.stream()
                                .filter(v -> ResourceType.Observation == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(observationResource).hasSize(1);

                List<Resource> allergyIntoleranceResource = e.stream()
                                .filter(v -> ResourceType.AllergyIntolerance == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(allergyIntoleranceResource).hasSize(1);

                List<Resource> conditionResource = e.stream()
                                .filter(v -> ResourceType.Condition == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(conditionResource).hasSize(1);
        }

        @Test@Disabled
        public void test_adta02_patient_encounter_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A02|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "NK1|1|Kennedy^Joe|FTH|||+44 201 12345678||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n"
                                + "AL1|1|DA|1605^acetaminophen^L|MO|Muscle Pain~hair loss\r";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 total resources
                assertThat(e.size()).isEqualTo(2);

                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

        }

        @Test@Disabled
        public void test_adta03_patient_encounter_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A03|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "NK1|1|Kennedy^Joe|FTH|||+44 201 12345678||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS_PRETTYPRINT);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;                
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 total resources
                assertThat(e.size()).isEqualTo(2);

                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

        }



        @Test@Disabled
        public void test_adta04_patient_encounter_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A04|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "NK1|1|Kennedy^Joe|FTH|||+44 201 12345678||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS_PRETTYPRINT);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 total resources
                assertThat(e.size()).isEqualTo(2);

                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

        }

        @Test
        public void test_adta08_basic_resources_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A08|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 resources: Patient and Encounter
                assertThat(e.size()).isEqualTo(2);
                
                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

        }

        @Test
        public void test_adta08_full_resources_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A08|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n"
                                + "PV2|||||||||||||||||||||||||AI|||||||||||||C|\n"
                                + "OBX|1|TX|1234^some text^SCT||First line: ECHOCARDIOGRAPHIC REPORT||||||F||\n"
                                + "AL1|1|DA|1605^acetaminophen^L|MO|Muscle Pain~hair loss\n" 
                                + "DG1|1||B45678|||A|\r";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 5 total resources
                assertThat(e.size()).isEqualTo(5);
                
                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

                List<Resource> observationResource = e.stream()
                                .filter(v -> ResourceType.Observation == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(observationResource).hasSize(1);

                List<Resource> allergyIntoleranceResource = e.stream()
                                .filter(v -> ResourceType.AllergyIntolerance == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(allergyIntoleranceResource).hasSize(1);

                List<Resource> conditionResource = e.stream()
                                .filter(v -> ResourceType.Condition == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(conditionResource).hasSize(1);

        }

        @Test@Disabled
        public void test_adta28_patient_encounter_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A28|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "NK1|1|Kennedy^Joe|FTH|||+44 201 12345678||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS_PRETTYPRINT);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 total resources
                assertThat(e.size()).isEqualTo(2);
                                
                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

        }

        @Test@Disabled
        public void test_adta31_patient_encounter_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A31|controlID|P|2.6\n"
                                + "EVN|A01|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "NK1|1|Kennedy^Joe|FTH|||+44 201 12345678||\n"
                                + "PV1||I||||||||SUR||||||||S|VisitNumber^^^ACME|A||||||||||||||||||||||||20150502090000|\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();
                
                // Expecting 2 total resources
                assertThat(e.size()).isEqualTo(2);
                                
                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(1);

                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(1);

        }

        @Test
        public void test_adta34_basic_resources_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A34|controlID|P|2.6\n"
                                + "EVN|A40|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "MRG|456||||||\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 total resources
                assertThat(e.size()).isEqualTo(2);
                                
                // There should be two patient resources, the PID patient and the MRG patient.
                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(2);

                // We currently do not support Encounters for merging, in ADT_A34 merge
                // messages, so no Encounter should be created.
                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(0);

        }

        @Test
        public void test_adta40_basic_resources_present() throws IOException {
                String hl7message = "MSH|^~\\&|TestSystem||TestTransformationAgent||20150502090000||ADT^A40|controlID|P|2.6\n"
                                + "EVN|A40|20150502090000|\n"
                                + "PID|||1234^^^^MR||DOE^JANE^|||F||||||||||||||||||||||\n"
                                + "MRG|456||||||\n";

                HL7ToFHIRConverter ftv = new HL7ToFHIRConverter();
                String json = ftv.convert(hl7message, OPTIONS);
                assertThat(json).isNotBlank();
                LOGGER.info("FHIR json result:\n" + json);
                IBaseResource bundleResource = context.getParser().parseResource(json);
                assertThat(bundleResource).isNotNull();
                Bundle b = (Bundle) bundleResource;
                List<BundleEntryComponent> e = b.getEntry();

                // Expecting 2 total resources
                assertThat(e.size()).isEqualTo(2);
                                
                // There should be two patient resources, the PID patient and the MRG patient.
                List<Resource> patientResource = e.stream()
                                .filter(v -> ResourceType.Patient == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(patientResource).hasSize(2);

                // We currently do not support Encounters for merging, in ADT_A40 merge
                // messages, so no Encounter should be created.
                List<Resource> encounterResource = e.stream()
                                .filter(v -> ResourceType.Encounter == v.getResource().getResourceType())
                                .map(BundleEntryComponent::getResource).collect(Collectors.toList());
                assertThat(encounterResource).hasSize(0);

        }

}