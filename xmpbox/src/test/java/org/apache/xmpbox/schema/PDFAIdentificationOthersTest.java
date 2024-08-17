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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.BadFieldValueException;
import org.junit.jupiter.api.Test;

class PDFAIdentificationOthersTest
{
    @Test
    void testPDFAIdentification() throws Exception
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPDFAIdentificationSchema();

        Integer versionId = 1;
        String amdId = "2005";
        String conformance = "B";

        pdfaid.setPartValueWithInt(versionId);
        pdfaid.setAmd(amdId);
        pdfaid.setConformance(conformance);

        assertEquals(versionId, pdfaid.getPart());
        assertEquals(amdId, pdfaid.getAmendment());
        assertEquals(conformance, pdfaid.getConformance());

        assertEquals("" + versionId, pdfaid.getPartProperty().getStringValue());
        assertEquals(amdId, pdfaid.getAmdProperty().getStringValue());
        assertEquals(conformance, pdfaid.getConformanceProperty().getStringValue());

        // check retrieve this schema in metadata
        assertEquals(pdfaid, metadata.getPDFAIdentificationSchema());

        // SaveMetadataHelper.serialize(metadata, true, System.out);
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

    @Test
    void testBadVersionIdValueType() throws Exception
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPDFAIdentificationSchema();
        pdfaid.setPartValueWithString("1");
        assertThrows(IllegalArgumentException.class, () -> {
            pdfaid.setPartValueWithString("ojoj");
        });
    }

}
