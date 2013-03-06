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

import junit.framework.Assert;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.BadFieldValueException;
import org.junit.Before;
import org.junit.Test;

public class PDFAIdentificationOthersTest
{

    protected XMPMetadata metadata;

    @Before
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

        Assert.assertEquals(versionId, pdfaid.getPart());
        Assert.assertEquals(amdId, pdfaid.getAmendment());
        Assert.assertEquals(conformance, pdfaid.getConformance());

        Assert.assertEquals("" + versionId, pdfaid.getPartProperty().getStringValue());
        Assert.assertEquals(amdId, pdfaid.getAmdProperty().getStringValue());
        Assert.assertEquals(conformance, pdfaid.getConformanceProperty().getStringValue());

        // check retrieve this schema in metadata
        Assert.assertEquals(pdfaid, metadata.getPDFIdentificationSchema());

        // SaveMetadataHelper.serialize(metadata, true, System.out);
    }

    @Test(expected = BadFieldValueException.class)
    public void testBadPDFAConformanceId() throws BadFieldValueException
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPFAIdentificationSchema();
        String conformance = "kiohiohiohiohio";
        pdfaid.setConformance(conformance);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadVersionIdValueType() throws Exception
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPFAIdentificationSchema();
        pdfaid.setPartValueWithString("1");
        pdfaid.setPartValueWithString("ojoj");
    }

}
