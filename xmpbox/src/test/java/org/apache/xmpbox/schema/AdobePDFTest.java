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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AdobePDFTest extends AbstractXMPSchemaTest
{

    @Before
    public void initTempMetaData() throws Exception
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddAdobePDFSchema();
        schemaClass = AdobePDFSchema.class;
    }

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        List<Object[]> data = new ArrayList<Object[]>();
        data.add(wrapProperty("Keywords", Types.Text, "kw1 kw2 kw3"));
        data.add(wrapProperty("PDFVersion", Types.Text, "1.4"));
        data.add(wrapProperty("Producer", Types.Text, "testcase"));

        return data;
    }

    public AdobePDFTest(String property, PropertyType type, Object value)
    {
        super(property, type, value);
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

        Assert.assertEquals("pdf", schem.getKeywordsProperty().getPrefix());
        Assert.assertEquals("Keywords", schem.getKeywordsProperty().getPropertyName());
        Assert.assertEquals(keywords, schem.getKeywords());

        Assert.assertEquals("pdf", schem.getPDFVersionProperty().getPrefix());
        Assert.assertEquals("PDFVersion", schem.getPDFVersionProperty().getPropertyName());
        Assert.assertEquals(pdfVersion, schem.getPDFVersion());

        Assert.assertEquals("pdf", schem.getProducerProperty().getPrefix());
        Assert.assertEquals("Producer", schem.getProducerProperty().getPropertyName());
        Assert.assertEquals(producer, schem.getProducer());

    }

    @Test(expected = BadFieldValueException.class)
    public void testBadPDFAConformanceId() throws BadFieldValueException
    {
        PDFAIdentificationSchema pdfaid = metadata.createAndAddPFAIdentificationSchema();
        String conformance = "kiohiohiohiohio";
        pdfaid.setConformance(conformance);
    }

}
