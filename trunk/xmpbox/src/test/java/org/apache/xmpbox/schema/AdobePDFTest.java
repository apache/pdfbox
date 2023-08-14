/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.xmpbox.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AdobePDFTest
{

    private XMPMetadata metadata;
    private XMPSchema schema;
    private Class<?> schemaClass;

    @BeforeEach
    void initMetadata()
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddAdobePDFSchema();
        schemaClass = AdobePDFSchema.class;
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testElementValue(String property, PropertyType type, String value) throws Exception
    {
        XMPSchemaTester xmpSchemaTester = new XMPSchemaTester(metadata, schema, schemaClass, property, type, value);
        xmpSchemaTester.testGetSetValue();
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testElementProperty(String property, PropertyType type, String value) throws Exception
    {
        XMPSchemaTester xmpSchemaTester = new XMPSchemaTester(metadata, schema, schemaClass, property, type, value);
        xmpSchemaTester.testGetSetProperty();
    }

    static Stream<Arguments> initializeParameters() throws Exception
    {  
        return Stream.of(
            Arguments.of("Keywords", XMPSchemaTester.createPropertyType(Types.Text), "kw1 kw2 kw3"),
            Arguments.of("PDFVersion", XMPSchemaTester.createPropertyType(Types.Text), "1.4"),
            Arguments.of("Producer", XMPSchemaTester.createPropertyType(Types.Text), "testcase")
        );
    }

    @Test
    void testPDFAIdentification() throws Exception
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        AdobePDFSchema schem = metadata.createAndAddAdobePDFSchema();

        String keywords = "keywords ihih";
        String pdfVersion = "1.4";
        String producer = "producer";

        schem.setKeywords(keywords);
        schem.setPDFVersion(pdfVersion);

        // Check get null if property not defined
        assertNull(schem.getProducer());

        schem.setProducer(producer);

        assertEquals("pdf", schem.getKeywordsProperty().getPrefix());
        assertEquals("Keywords", schem.getKeywordsProperty().getPropertyName());
        assertEquals(keywords, schem.getKeywords());

        assertEquals("pdf", schem.getPDFVersionProperty().getPrefix());
        assertEquals("PDFVersion", schem.getPDFVersionProperty().getPropertyName());
        assertEquals(pdfVersion, schem.getPDFVersion());

        assertEquals("pdf", schem.getProducerProperty().getPrefix());
        assertEquals("Producer", schem.getProducerProperty().getPropertyName());
        assertEquals(producer, schem.getProducer());
    }

    @Test
    void testBadPDFAConformanceId() throws BadFieldValueException
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPDFAIdentificationSchema();
        String conformance = "kiohiohiohiohio";
        assertThrows(BadFieldValueException.class, () -> {
            pdfaid.setConformance(conformance);
        }); 
    }
}
