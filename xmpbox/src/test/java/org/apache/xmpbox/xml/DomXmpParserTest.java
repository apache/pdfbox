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

package org.apache.xmpbox.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.xmpbox.schema.XMPPageTextSchema;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.DimensionsType;
import org.apache.xmpbox.type.ResourceEventType;
import org.apache.xmpbox.type.ResourceRefType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class DomXmpParserTest
{
    @Test
    void testPDFBox5649() throws IOException, XmpParsingException
    {
        try (InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-5649.xml"))
        {
            DomXmpParser dxp = new DomXmpParser();
            dxp.setStrictParsing(false);
            XMPMetadata xmp = dxp.parse(fis);
            Assertions.assertNotNull(xmp);
        }
    }

    @Test
    void testPDFBox5835() throws IOException, XmpParsingException
    {
        try (InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-5835.xml"))
        {
            DomXmpParser dxp = new DomXmpParser();
            dxp.setStrictParsing(false);
            XMPMetadata xmp = dxp.parse(fis);
            Assertions.assertEquals("A", xmp.getPDFAIdentificationSchema().getConformance());
            Assertions.assertEquals((Integer) 3, xmp.getPDFAIdentificationSchema().getPart());
        }
    }

    @Test
    void testPDFBox5976() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                   "<?xpacket begin=\"\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                   "<rdf:RDF\n" +
                   "	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                   "	xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\"\n" +
                   "	xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\">\n" +
                   "	    <rdf:Description pdfaid:conformance=\"B\" pdfaid:part=\"3\" rdf:about=\"\"/>\n" +
                   "	    <rdf:Description pdf:Producer=\"WeasyPrint 64.1\" rdf:about=\"\"/>\n" +
                   "</rdf:RDF>\n" +
                   "<?xpacket end=\"r\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("B", xmp.getPDFAIdentificationSchema().getConformance());
        Assertions.assertEquals((Integer) 3, xmp.getPDFAIdentificationSchema().getPart());
    }

    /**
     * PDFBOX-6106: Check that "pdf:CreationDate='2004-01-30T17:21:50Z'" is detected as incorrect.
     * (Only Keywords, PDFVersion, and Producer are allowed in strict mode)
     *
     * @throws XmpParsingException
     */
    @Test
    void testPDFBox6106() throws XmpParsingException
    {
        // from file 001358.pdf
        String s = "<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d' bytes='647'?>\n" +
                    "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
                    "         xmlns:iX='http://ns.adobe.com/iX/1.0/'>\n" +
                    "	<rdf:Description about=''\n" +
                    "	                 xmlns='http://ns.adobe.com/pdf/1.3/'\n" +
                    "	                 xmlns:pdf='http://ns.adobe.com/pdf/1.3/'\n" +
                    "	                 pdf:CreationDate='2004-01-30T17:21:50Z'\n" +
                    "	                 pdf:ModDate='2004-01-30T17:21:50Z'\n" +
                    "	                 pdf:Producer='Acrobat Distiller 5.0.5 (Windows)'/>\n" +
                    "	<rdf:Description about=''\n" +
                    "	                 xmlns='http://ns.adobe.com/xap/1.0/'\n" +
                    "	                 xmlns:xap='http://ns.adobe.com/xap/1.0/'\n" +
                    "	                 xap:CreateDate='2004-01-30T17:21:50Z'\n" +
                    "	                 xap:ModifyDate='2004-01-30T17:21:50Z'\n" +
                    "	                 xap:MetadataDate='2004-01-30T17:21:50Z'/>\n" +
                    "</rdf:RDF><?xpacket end='r'?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = Assertions.assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        Assertions.assertEquals("No type defined for {http://ns.adobe.com/pdf/1.3/}CreationDate", ex.getMessage());
    }

    /**
     * PDFBOX-5288: check that namespace declaration within an "rdf:li" element is found.
     *
     * @throws XmpParsingException
     */
    @Test
    void testPDFBox5288() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Public XMP Toolkit Core 4.0  \">\n" +
                    " \n" +
                    " <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "  \n" +
                    "  <rdf:Description xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\" rdf:about=\"\">\n" +
                    "   <xmpMM:DocumentID>uidd:1f0e03977b90b6365a376454ffdf34a7</xmpMM:DocumentID>\n" +
                    "   <xmpMM:History>\n" +
                    "    <rdf:Seq>\n" +
                    "     <rdf:li xmlns:stEvt=\"http://ns.adobe.com/xap/1.0/sType/ResourceEvent#\">\n" +
                    "      <rdf:Description>\n" +
                    "       <stEvt:action>created</stEvt:action>\n" +
                    "       <stEvt:parameters>iDRS PDF output engine 7</stEvt:parameters>\n" +
                    "       <stEvt:when>2022-09-12T12:00:07+02:00</stEvt:when>\n" +
                    "      </rdf:Description>\n" +
                    "     </rdf:li>\n" +
                    "    </rdf:Seq>\n" +
                    "   </xmpMM:History>\n" +
                    "  </rdf:Description>\n" +
                    " </rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"w\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp.getXMPMediaManagementSchema();
        Assertions.assertEquals("uidd:1f0e03977b90b6365a376454ffdf34a7", xmpMediaManagementSchema.getDocumentID());
        ArrayProperty historyProperty = xmpMediaManagementSchema.getHistoryProperty();
        ResourceEventType firstHistoryEntry = (ResourceEventType) historyProperty.getAllProperties().iterator().next();
        Assertions.assertEquals("created", firstHistoryEntry.getAction());
        Assertions.assertEquals("iDRS PDF output engine 7", firstHistoryEntry.getParameters());
    }

    /**
     * Test PageTextSchema and XMPMediaManagementSchema.
     *
     * @throws XmpParsingException 
     */
    @Test
    void testPageTextSchema() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "           <rdf:Description xmlns:stRef=\"http://ns.adobe.com/xap/1.0/sType/ResourceRef#\"\n" +
                    "		                 xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\"\n" +
                    "		                 rdf:about=\"\">\n" +
                    "			<xmpMM:InstanceID>uuid:b429d411-e628-45ca-b932-d2c77fbe6cd3</xmpMM:InstanceID>\n" +
                    "			<xmpMM:DocumentID>adobe:docid:indd:db084a4d-dbb2-11dc-ac34-beb3cc4028ec</xmpMM:DocumentID>\n" +
                    "			<xmpMM:RenditionClass>proof:pdf</xmpMM:RenditionClass>\n" +
                    "			<xmpMM:DerivedFrom rdf:parseType=\"Resource\">\n" +
                    "				<stRef:documentID>adobe:docid:indd:fa7c6589-9f4a-11dc-9641-af983df728d7</stRef:documentID>\n" +
                    "			</xmpMM:DerivedFrom>\n" +
                    "		</rdf:Description>" +
                    "		<rdf:Description xmlns:xmpTPg=\"http://ns.adobe.com/xap/1.0/t/pg/\"\n" +
                    "		                 rdf:about=\"\">\n" +
                    "			<xmpTPg:MaxPageSize>\n" +
                    "				<rdf:Description xmlns:stDim=\"http://ns.adobe.com/xap/1.0/sType/Dimensions#\">\n" +
                    "					<stDim:w>4</stDim:w>\n" +
                    "					<stDim:h>3</stDim:h>\n" +
                    "					<stDim:unit>inch</stDim:unit>\n" +
                    "				</rdf:Description>\n" +
                    "			</xmpTPg:MaxPageSize>\n" +
                    "			<xmpTPg:NPages>7</xmpTPg:NPages>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"r\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPPageTextSchema pageTextSchema = xmp.getPageTextSchema();
        DimensionsType dim = (DimensionsType) pageTextSchema.getProperty(XMPPageTextSchema.MAX_PAGE_SIZE);
        Assertions.assertEquals("DimensionsType{4.0 x 3.0 inch}", dim.toString());
        Assertions.assertEquals("[NPages=IntegerType:7]", pageTextSchema.getProperty(XMPPageTextSchema.N_PAGES).toString());
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp.getXMPMediaManagementSchema();
        ResourceRefType derivedFromProperty = xmpMediaManagementSchema.getDerivedFromProperty();
        Assertions.assertEquals("uuid:b429d411-e628-45ca-b932-d2c77fbe6cd3", xmpMediaManagementSchema.getInstanceID());
        Assertions.assertEquals("proof:pdf", xmpMediaManagementSchema.getRenditionClass());
        Assertions.assertEquals("adobe:docid:indd:db084a4d-dbb2-11dc-ac34-beb3cc4028ec", xmpMediaManagementSchema.getDocumentID());
        Assertions.assertEquals("adobe:docid:indd:fa7c6589-9f4a-11dc-9641-af983df728d7", derivedFromProperty.getDocumentID());
    }
}
