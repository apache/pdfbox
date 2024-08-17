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

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.BadFieldValueException;
import org.junit.jupiter.api.Test;

class AdobePDFErrorsTest
{

    private final XMPMetadata metadata = XMPMetadata.createXMPMetadata();

    @Test
    void testPDFAIdentification() throws Exception
    {
        AdobePDFSchema schem = metadata.createAndAddAdobePDFSchema();

        String keywords = "keywords ihih";
        String pdfVersion = "1.4";
        String producer = "producer";

        schem.setKeywords(keywords);
        schem.setPDFVersion(pdfVersion);

        // Check get null if property not defined
        assertNull(schem.getProducer());

        schem.setProducer(producer);

        assertEquals("Keywords", schem.getKeywordsProperty().getPropertyName());
        assertEquals(keywords, schem.getKeywords());

        assertEquals("PDFVersion", schem.getPDFVersionProperty().getPropertyName());
        assertEquals(pdfVersion, schem.getPDFVersion());

        assertEquals("Producer", schem.getProducerProperty().getPropertyName());
        assertEquals(producer, schem.getProducer());

        // check retrieve this schema in metadata
        assertEquals(schem, metadata.getAdobePDFSchema());

        // SaveMetadataHelper.serialize(metadata, true, System.out);
    }

    @Test
    void testBadPDFAConformanceId() throws Exception
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPDFAIdentificationSchema();
        String conformance = "kiohiohiohiohio";
        assertThrows(BadFieldValueException.class, () -> {
            pdfaid.setConformance(conformance);
        });
    }

    @Test
    void testBadVersionIdValueType() throws Exception
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPDFAIdentificationSchema();
        pdfaid.setPartValueWithString("1");
        assertThrows(IllegalArgumentException.class, () -> {    
            pdfaid.setPartValueWithString("ojoj");
        });
    }
}
