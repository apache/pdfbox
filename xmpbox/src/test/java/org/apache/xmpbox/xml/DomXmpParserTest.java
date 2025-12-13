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
import java.util.Calendar;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.PhotoshopSchema;
import org.apache.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XMPageTextSchema;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.DefinedStructuredType;
import org.apache.xmpbox.type.DimensionsType;
import org.apache.xmpbox.type.LayerType;
import org.apache.xmpbox.type.PDFASchemaType;
import org.apache.xmpbox.type.ResourceEventType;
import org.apache.xmpbox.type.ResourceRefType;
import org.apache.xmpbox.type.TextType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
            XMPMetadata xmp = dxp.parse(fis);
            assertNotNull(xmp);
        }
    }

    @Test
    void testPDFBox5835() throws IOException, XmpParsingException
    {
        try (InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-5835.xml"))
        {
            DomXmpParser dxp = new DomXmpParser();
            XMPMetadata xmp = dxp.parse(fis);
            assertEquals("A", xmp.getPDFAIdentificationSchema().getConformance());
            assertEquals((Integer) 3, xmp.getPDFAIdentificationSchema().getPart());
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
        assertEquals("B", xmp.getPDFAIdentificationSchema().getConformance());
        assertEquals((Integer) 3, xmp.getPDFAIdentificationSchema().getPart());
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
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("No type defined for {http://ns.adobe.com/pdf/1.3/}CreationDate", ex.getMessage());
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
        assertEquals("uidd:1f0e03977b90b6365a376454ffdf34a7", xmpMediaManagementSchema.getDocumentID());
        ArrayProperty historyProperty = xmpMediaManagementSchema.getHistoryProperty();
        ResourceEventType firstHistoryEntry = (ResourceEventType) historyProperty.getAllProperties().iterator().next();
        assertEquals("created", firstHistoryEntry.getAction());
        assertEquals("iDRS PDF output engine 7", firstHistoryEntry.getParameters());
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
        XMPageTextSchema pageTextSchema = xmp.getPageTextSchema();
        DimensionsType dim = (DimensionsType) pageTextSchema.getProperty(XMPageTextSchema.MAX_PAGE_SIZE);
        assertEquals("DimensionsType{4.0 x 3.0 inch}", dim.toString());
        assertEquals("[NPages=IntegerType:7]", pageTextSchema.getProperty(XMPageTextSchema.N_PAGES).toString());
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp.getXMPMediaManagementSchema();
        ResourceRefType derivedFromProperty = xmpMediaManagementSchema.getDerivedFromProperty();
        assertEquals("uuid:b429d411-e628-45ca-b932-d2c77fbe6cd3", xmpMediaManagementSchema.getInstanceID());
        assertEquals("proof:pdf", xmpMediaManagementSchema.getRenditionClass());
        assertEquals("adobe:docid:indd:db084a4d-dbb2-11dc-ac34-beb3cc4028ec", xmpMediaManagementSchema.getDocumentID());
        assertEquals("adobe:docid:indd:fa7c6589-9f4a-11dc-9641-af983df728d7", derivedFromProperty.getDocumentID());
    }

    /**
     * PDFBOX-3882: Test PageTextSchema with dimensions mixed as children or attributes.
     *
     * @throws XmpParsingException
     */
    @Test
    void testPageTextSchema2() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "           <rdf:Description xmlns:xmpTPg=\"http://ns.adobe.com/xap/1.0/t/pg/\"" +
                    "                            xmlns:stDim=\"http://ns.adobe.com/xap/1.0/sType/Dimensions#\"" +
                    "		                 rdf:about=\"\">\n" +
                    "			<xmpTPg:MaxPageSize>\n" +
                    "				<rdf:Description stDim:w=\"4\" stDim:h=\"3\">\n" +
                    "					<stDim:unit>inch</stDim:unit>\n" +
                    "				</rdf:Description>\n" +
                    "			</xmpTPg:MaxPageSize>\n" +
                    "			<xmpTPg:NPages>7</xmpTPg:NPages>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"r\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPageTextSchema pageTextSchema = xmp.getPageTextSchema();
        DimensionsType dim = (DimensionsType) pageTextSchema.getProperty(XMPageTextSchema.MAX_PAGE_SIZE);
        assertEquals("DimensionsType{4.0 x 3.0 inch}", dim.toString());
        assertEquals("[NPages=IntegerType:7]", pageTextSchema.getProperty(XMPageTextSchema.N_PAGES).toString());
    }

    /**
     * PDFBOX-3882: Test PageTextSchema with dimensions as attributes only.
     *
     * @throws XmpParsingException
     */
    @Test
    void testPageTextSchema3() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "           <rdf:Description xmlns:xmpTPg=\"http://ns.adobe.com/xap/1.0/t/pg/\"" +
                    "                            xmlns:stDim=\"http://ns.adobe.com/xap/1.0/sType/Dimensions#\"" +
                    "		                 rdf:about=\"\">\n" +
                    "			<xmpTPg:MaxPageSize>\n" +
                    "				<rdf:Description stDim:w=\"4\" stDim:h=\"3\" stDim:unit=\"inch\"/>\n" +
                    "			</xmpTPg:MaxPageSize>\n" +
                    "			<xmpTPg:NPages>7</xmpTPg:NPages>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"r\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPageTextSchema pageTextSchema = xmp.getPageTextSchema();
        DimensionsType dim = (DimensionsType) pageTextSchema.getProperty(XMPageTextSchema.MAX_PAGE_SIZE);
        assertEquals("DimensionsType{4.0 x 3.0 inch}", dim.toString());
        assertEquals("[NPages=IntegerType:7]", pageTextSchema.getProperty(XMPageTextSchema.N_PAGES).toString());
    }


    /**
     * PDFBOX-3882: Test attributes being used as properties to define an extension schema. Also
     * verify the content of the actual extension schema.
     *
     * @throws IOException
     * @throws XmpParsingException 
     */
    @Test
    void testPDFBox3882() throws IOException, XmpParsingException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-3882-dematbox.xml"))
        {
            DomXmpParser dxp = new DomXmpParser();
            XMPMetadata xmp = dxp.parse(is);
            List<AbstractField> allProperties = xmp.getPDFExtensionSchema().getSchemasProperty().getAllProperties();
            assertEquals(1, allProperties.size());
            PDFASchemaType pdfExtensionSchema = (PDFASchemaType) allProperties.get(0);
            assertEquals("http://www.sagemcom.com/documents/xmlns/dematbox", pdfExtensionSchema.getNamespaceURI());
            assertEquals("dematbox", pdfExtensionSchema.getPrefixValue());
            XMPSchema extensionSchema = xmp.getSchema(pdfExtensionSchema.getNamespaceURI());
            assertEquals(pdfExtensionSchema.getNamespaceURI(), extensionSchema.getNamespace());
            assertEquals(pdfExtensionSchema.getPrefixValue(), extensionSchema.getPrefix());
            ArrayProperty pageInfoProp = (ArrayProperty) extensionSchema.getProperty("PageInfo");
            DefinedStructuredType dst = (DefinedStructuredType) pageInfoProp.getAllProperties().get(0);
            assertEquals("[number=IntegerType:1]", dst.getProperty("number").toString());
            assertEquals("[origNumber=IntegerType:1]", dst.getProperty("origNumber").toString());            
        }
    }

    /**
     * PDFBOX-3882: Test ResourceEventType properties as attributes instead of properties (call of
     * tryParseAttributesAsProperties() at the end of parseLiElement())
     *
     * @throws XmpParsingException
     */
    @Test
    void testPDFBox3882_2() throws XmpParsingException, BadFieldValueException
    {
        // data modified from XMP data in the JPEG file in Apache Tika JpegParserTest.testJPEGXMPMM()
        String s = "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"Adobe XMP Core 5.0-c060 61.134777, 2010/02/12-17:32:00        \">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "		<rdf:Description rdf:about=\"\"\n" +
                    "		                 xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n" +
                    "		                 xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                    "		                 xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\"\n" +
                    "		                 xmlns:stEvt=\"http://ns.adobe.com/xap/1.0/sType/ResourceEvent#\"\n" +
                    "		                 xmlns:stRef=\"http://ns.adobe.com/xap/1.0/sType/ResourceRef#\"\n" +
                    "		                 xmlns:photoshop=\"http://ns.adobe.com/photoshop/1.0/\"\n" +
                    "		                 xmp:CreatorTool=\"Adobe Photoshop CS5 Macintosh\"\n" +
                    "		                 xmp:CreateDate=\"2012-04-30T12:52:07-04:00\"\n" +
                    "		                 xmp:MetadataDate=\"2012-05-03T13:36:11-04:00\"\n" +
                    "		                 xmp:ModifyDate=\"2012-05-03T13:36:11-04:00\"\n" +
                    "		                 dc:format=\"image/jpeg\"\n" +
                    "		                 xmpMM:InstanceID=\"xmp.iid:49E997338D4911E1AB62EBF9B374B234\"\n" +
                    "		                 xmpMM:DocumentID=\"xmp.did:49E997348D4911E1AB62EBF9B374B234\"\n" +
                    "		                 xmpMM:OriginalDocumentID=\"xmp.did:01801174072068118A6D9A879C818256\"\n" +
                    "		                 photoshop:History=\"2012-05-03T09:34:50-04:00&#x9;File i1222b.jpg opened&#xA;\">\n" +
                    "			<xmpMM:History>\n" +
                    "				<rdf:Seq>\n" +
                    "					<rdf:li stEvt:action=\"created\"\n" +
                    "					        stEvt:instanceID=\"xmp.iid:01801174072068118A6D9A879C818256\"\n" +
                    "					        stEvt:when=\"2012-04-30T12:52:07-04:00\"\n" +
                    "					        stEvt:softwareAgent=\"Adobe Photoshop CS5 Macintosh\"/>\n" +
                    "					<rdf:li stEvt:action=\"saved\"\n" +
                    "					        stEvt:instanceID=\"xmp.iid:02801174072068118A6D9A879C818256\"\n" +
                    "					        stEvt:when=\"2012-04-30T12:54:04-04:00\"\n" +
                    "					        stEvt:softwareAgent=\"Adobe Photoshop CS5 Macintosh\"\n" +
                    "					        stEvt:changed=\"/\"/>\n" +
                    "					<rdf:li stEvt:action=\"saved\"\n" +
                    "					        stEvt:instanceID=\"xmp.iid:03801174072068118A6D9A879C818256\"\n" +
                    "					        stEvt:when=\"2012-04-30T12:54:48-04:00\"\n" +
                    "					        stEvt:softwareAgent=\"Adobe Photoshop CS5 Macintosh\"\n" +
                    "					        stEvt:changed=\"/\"/>\n" +
                    "				</rdf:Seq>\n" +
                    "			</xmpMM:History>\n" +
                    "			<xmpMM:DerivedFrom stRef:instanceID=\"xmp.iid:21F0677BA22168118A6D9A879C818256\"\n" +
                    "			                   stRef:documentID=\"xmp.did:01801174072068118A6D9A879C818256\"\n" +
                    "			                   stRef:originalDocumentID=\"xmp.did:01801174072068118A6D9A879C818256\"/>\n" +
                    "			<photoshop:DocumentAncestors>\n" +
                    "				<rdf:Bag>\n" +
                    "					<rdf:li>adobe:docid:photoshop:11d3ec5a-c131-11d8-9274-ec65c7d7e0c6</rdf:li>\n" +
                    "					<rdf:li>adobe:docid:photoshop:aadc7027-309c-11d8-9596-9cf45d2f630b</rdf:li>\n" +
                    "					<rdf:li>adobe:docid:photoshop:c7961c59-6e0f-11d8-87b7-d67539df12d8</rdf:li>\n" +
                    "				</rdf:Bag>\n" +
                    "			</photoshop:DocumentAncestors>\n" +
                    "			<photoshop:DateCreated>2012-04-30T12:54:48Z</photoshop:DateCreated>\n" +
                    "			<photoshop:TextLayers>\n" +
                    "				<rdf:Seq>\n" +
                    "                               <rdf:li photoshop:LayerName=\"Name1\" photoshop:LayerText=\"Text1\"/>\n" +
                    "                               <rdf:li photoshop:LayerName=\"Name2\" photoshop:LayerText=\"Text2\"/>\n" +
                    "				</rdf:Seq>\n" +
                    "			</photoshop:TextLayers>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta>\n" +
                    "<?xpacket end=\"w\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp.getXMPMediaManagementSchema();
        ArrayProperty historyProperty = xmpMediaManagementSchema.getHistoryProperty();
        List<AbstractField> historyProperties = historyProperty.getAllProperties();
        assertEquals(3, historyProperties.size());
        ResourceEventType ret0 = (ResourceEventType) historyProperties.get(0);
        ResourceEventType ret1 = (ResourceEventType) historyProperties.get(1);
        ResourceEventType ret2 = (ResourceEventType) historyProperties.get(2);
        assertEquals("created", ret0.getAction());
        assertEquals("xmp.iid:01801174072068118A6D9A879C818256", ret0.getInstanceID());
        assertEquals(2012, ret0.getWhen().get(Calendar.YEAR));
        assertEquals(52, ret0.getWhen().get(Calendar.MINUTE));
        assertEquals("Adobe Photoshop CS5 Macintosh", ret0.getSoftwareAgent());
        assertEquals("xmp.iid:02801174072068118A6D9A879C818256", ret1.getInstanceID());
        assertEquals("xmp.iid:03801174072068118A6D9A879C818256", ret2.getInstanceID());
        assertEquals(2012, ret1.getWhen().get(Calendar.YEAR));
        assertEquals(54, ret1.getWhen().get(Calendar.MINUTE));
        assertEquals(4, ret1.getWhen().get(Calendar.SECOND));
        assertEquals(2012, ret2.getWhen().get(Calendar.YEAR));
        assertEquals(54, ret2.getWhen().get(Calendar.MINUTE));
        assertEquals(48, ret2.getWhen().get(Calendar.SECOND));
        assertEquals("xmp.iid:49E997338D4911E1AB62EBF9B374B234", xmpMediaManagementSchema.getInstanceID());
        assertEquals("xmp.did:49E997348D4911E1AB62EBF9B374B234", xmpMediaManagementSchema.getDocumentID());
        assertEquals("xmp.did:01801174072068118A6D9A879C818256", xmpMediaManagementSchema.getOriginalDocumentID());
        PhotoshopSchema photoshopSchema = xmp.getPhotoshopSchema();
        List<LayerType> textLayers = photoshopSchema.getTextLayers();
        assertEquals(2, textLayers.size());
        assertEquals("Name1", textLayers.get(0).getLayerName());
        assertEquals("Text1", textLayers.get(0).getLayerText());
        assertEquals("Name2", textLayers.get(1).getLayerName());
        assertEquals("Text2", textLayers.get(1).getLayerText());
        assertEquals("2012-04-30T12:54:48+00:00", photoshopSchema.getDateCreated());
        assertEquals("2012-05-03T09:34:50-04:00\tFile i1222b.jpg opened\n", photoshopSchema.getHistory());
        ArrayProperty ancestorsProperty = photoshopSchema.getDocumentAncestorsProperty();
        List<AbstractField> ancestors = ancestorsProperty.getAllProperties();
        assertEquals(3, ancestors.size());
        assertEquals("adobe:docid:photoshop:11d3ec5a-c131-11d8-9274-ec65c7d7e0c6", ((TextType) ancestors.get(0)).getStringValue());
        assertEquals("adobe:docid:photoshop:aadc7027-309c-11d8-9596-9cf45d2f630b", ((TextType) ancestors.get(1)).getStringValue());
        assertEquals("adobe:docid:photoshop:c7961c59-6e0f-11d8-87b7-d67539df12d8", ((TextType) ancestors.get(2)).getStringValue());
        // xmpMediaManagementSchema.getDerivedFromProperty() doesn't work.
        // However the PDFLib XMP validator considers this file to be invalid, so lets not bother more
    }

    /**
     * PDFBOX-5292: Test whether inline extension schema is detected.
     *
     * @throws XmpParsingException 
     */
    @Test
    void testPDFBox5292() throws XmpParsingException, BadFieldValueException
    {
        String s = "<?xpacket begin=\"ï»¿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.6-c015 84.159810, 2016/09/10-02:41:30        \">\n" +
                    "    <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "        <rdf:Description rdf:about=\"\"\n" +
                    "                         xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n" +
                    "                         xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                    "                         xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\"\n" +
                    "                         xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\"\n" +
                    "                         xmlns:pdfaExtension=\"http://www.aiim.org/pdfa/ns/extension/\"\n" +
                    "                         xmlns:pdfaSchema=\"http://www.aiim.org/pdfa/ns/schema#\"\n" +
                    "                         xmlns:pdfaProperty=\"http://www.aiim.org/pdfa/ns/property#\"\n" +
                    "                         xmlns:example=\"http://ns.example.org/default/1.0/\">\n" +
                    "            <xmp:CreateDate>2021-05-21T11:42:49+01:00</xmp:CreateDate>\n" +
                    "            <xmp:ModifyDate>2021-05-21T11:47:16+02:00</xmp:ModifyDate>\n" +
                    "            <xmp:MetadataDate>2021-05-21T11:47:16+02:00</xmp:MetadataDate>\n" +
                    "            <dc:format>application/pdf</dc:format>\n" +
                    "            <dc:title>\n" +
                    "                <rdf:Alt>\n" +
                    "                    <rdf:li xml:lang=\"x-default\">Inline XMP Extension PoC</rdf:li>\n" +
                    "                </rdf:Alt>\n" +
                    "            </dc:title>\n" +
                    "            <dc:creator>\n" +
                    "                <rdf:Seq>\n" +
                    "                    <rdf:li>DSO</rdf:li>\n" +
                    "                </rdf:Seq>\n" +
                    "            </dc:creator>\n" +
                    "            <dc:description>\n" +
                    "                <rdf:Alt>\n" +
                    "                    <rdf:li xml:lang=\"x-default\">Inline XMP Extension PoC</rdf:li>\n" +
                    "                </rdf:Alt>\n" +
                    "            </dc:description>\n" +
                    "            <pdf:Keywords/>\n" +
                    "            <pdfaid:part>2</pdfaid:part>\n" +
                    "            <pdfaid:conformance>A</pdfaid:conformance>\n" +
                    "            <example:Data>Example</example:Data>\n" +
                    "            <pdfaExtension:schemas>\n" +
                    "                <rdf:Bag>\n" +
                    "                    <rdf:li rdf:parseType=\"Resource\">\n" +
                    "                        <pdfaSchema:schema>Simple Schema</pdfaSchema:schema>\n" +
                    "                        <pdfaSchema:namespaceURI>http://ns.example.org/default/1.0/</pdfaSchema:namespaceURI>\n" +
                    "                        <pdfaSchema:prefix>example</pdfaSchema:prefix>\n" +
                    "                        <pdfaSchema:property>\n" +
                    "                            <rdf:Seq>\n" +
                    "                                <rdf:li rdf:parseType=\"Resource\">\n" +
                    "                                    <pdfaProperty:name>Data</pdfaProperty:name>\n" +
                    "                                    <pdfaProperty:valueType>Text</pdfaProperty:valueType>\n" +
                    "                                    <pdfaProperty:category>internal</pdfaProperty:category>\n" +
                    "                                    <pdfaProperty:description>Example Data</pdfaProperty:description>\n" +
                    "                                </rdf:li>\n" +
                    "                            </rdf:Seq>\n" +
                    "                        </pdfaSchema:property>\n" +
                    "                    </rdf:li>\n" +
                    "                    <rdf:li rdf:parseType=\"Resource\">\n" +
                    "                        <pdfaSchema:namespaceURI>http://www.aiim.org/pdfa/ns/id/</pdfaSchema:namespaceURI>\n" +
                    "                        <pdfaSchema:prefix>pdfaid</pdfaSchema:prefix>\n" +
                    "                        <pdfaSchema:schema>PDF/A ID Schema</pdfaSchema:schema>\n" +
                    "                        <pdfaSchema:property>\n" +
                    "                            <rdf:Seq>\n" +
                    "                                <rdf:li rdf:parseType=\"Resource\">\n" +
                    "                                    <pdfaProperty:category>internal</pdfaProperty:category>\n" +
                    "                                    <pdfaProperty:description>Part of PDF/A standard</pdfaProperty:description>\n" +
                    "                                    <pdfaProperty:name>part</pdfaProperty:name>\n" +
                    "                                    <pdfaProperty:valueType>Integer</pdfaProperty:valueType>\n" +
                    "                                </rdf:li>\n" +
                    "                                <rdf:li rdf:parseType=\"Resource\">\n" +
                    "                                    <pdfaProperty:category>internal</pdfaProperty:category>\n" +
                    "                                    <pdfaProperty:description>Conformance level of PDF/A standard</pdfaProperty:description>\n" +
                    "                                    <pdfaProperty:name>conformance</pdfaProperty:name>\n" +
                    "                                    <pdfaProperty:valueType>Text</pdfaProperty:valueType>\n" +
                    "                                </rdf:li>\n" +
                    "                            </rdf:Seq>\n" +
                    "                        </pdfaSchema:property>\n" +
                    "                    </rdf:li>\n" +
                    "                </rdf:Bag>\n" +
                    "            </pdfaExtension:schemas>\n" +
                    "        </rdf:Description>\n" +
                    "    </rdf:RDF>\n" +
                    "</x:xmpmeta>\n" +
                    "\n" +
                    "<?xpacket end=\"w\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        PDFAIdentificationSchema pdfaIdSchema = xmp.getPDFAIdentificationSchema();
        assertEquals(2, pdfaIdSchema.getPart());
        String dataValue = xmp.getSchema("http://ns.example.org/default/1.0/").getUnqualifiedTextPropertyValue("Data");
        assertEquals("Example", dataValue);
    }
}
