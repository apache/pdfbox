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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PDFAIdentificationOthersTest
{

    protected XMPMetadata metadata;

    @BeforeEach
    public void initTempMetaData() throws Exception
    {
        metadata = XMPMetadata.createXMPMetadata();
    }

    @Test
    public void testPDFAIdentification() throws Exception
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPFAIdentificationSchema();

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
        assertEquals(pdfaid, metadata.getPDFIdentificationSchema());

        // SaveMetadataHelper.serialize(metadata, true, System.out);
    }

    @Test
    public void testBadPDFAConformanceId() throws BadFieldValueException
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPFAIdentificationSchema();
        String conformance = "kiohiohiohiohio";
        assertThrows(BadFieldValueException.class, () -> {
	        pdfaid.setConformance(conformance);
	    });
    }

    @Test
    public void testBadVersionIdValueType() throws Exception
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPFAIdentificationSchema();
        assertThrows(IllegalArgumentException.class, () -> {
            pdfaid.setPartValueWithString("1");
            pdfaid.setPartValueWithString("ojoj");
	    });
    }

}
