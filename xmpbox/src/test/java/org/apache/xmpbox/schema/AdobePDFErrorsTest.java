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
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.Before;
import org.junit.Test;

public class AdobePDFErrorsTest
{

    protected XMPMetadata metadata;

    protected DomXmpParser builder;

    @Before
    public void initTempMetaData() throws Exception
    {
        builder = new DomXmpParser();
        metadata = XMPMetadata.createXMPMetadata();
    }

    @Test
    public void testPDFAIdentification() throws Exception
    {
        AdobePDFSchema schem = metadata.createAndAddAdobePDFSchema();

        String keywords = "keywords ihih";
        String pdfVersion = "1.4";
        String producer = "producer";

        schem.setKeywords(keywords);
        schem.setPDFVersion(pdfVersion);

        // Check get null if property not defined
        Assert.assertNull(schem.getProducer());

        schem.setProducer(producer);

        Assert.assertEquals("Keywords", schem.getKeywordsProperty().getPropertyName());
        Assert.assertEquals(keywords, schem.getKeywords());

        Assert.assertEquals("PDFVersion", schem.getPDFVersionProperty().getPropertyName());
        Assert.assertEquals(pdfVersion, schem.getPDFVersion());

        Assert.assertEquals("Producer", schem.getProducerProperty().getPropertyName());
        Assert.assertEquals(producer, schem.getProducer());

        // check retrieve this schema in metadata
        Assert.assertEquals(schem, metadata.getAdobePDFSchema());

        // SaveMetadataHelper.serialize(metadata, true, System.out);
    }

    @Test(expected = BadFieldValueException.class)
    public void testBadPDFAConformanceId() throws Exception
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
