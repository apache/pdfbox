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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import javax.xml.transform.TransformerException;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.ExifSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.PhotoshopSchema;
import org.apache.xmpbox.schema.TiffSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    /**
     * Test that a Seq / Mag mixup gets detected in strict mode and gets read in lenient mode.
     * @throws XmpParsingException
     */
    @Test
    void testLenientBagSeqMixup() throws XmpParsingException
    {
        String s = "<?xpacket begin='﻿' id='W5M0MpCehiHzreSzNTczkc9d'?>\n" +
                    "<?adobe-xap-filters esc=\"CRLF\"?>\n" +
                    "<x:xmpmeta xmlns:x='adobe:ns:meta/'>\n" +
                    "	<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n" +
                    "		<rdf:Description xmlns:dc='http://purl.org/dc/elements/1.1/'\n" +
                    "		                 dc:format='application/pdf'>\n" +
                    "			<dc:subject>\n" +
                    "				<rdf:Seq>\n" +
                    "					<rdf:li>Important subject</rdf:li>\n" +
                    "					<rdf:li>Unimportant subject</rdf:li>\n" +
                    "				</rdf:Seq>\n" +
                    "			</dc:subject>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta>\n" +
                    "<?xpacket end='w'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Invalid array type, expecting Bag and found Seq [prefix=dc; name=subject]", ex.getMessage());
        final DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        DublinCoreSchema dublinCoreSchema = xmp.getDublinCoreSchema();
        List<String> subjects = dublinCoreSchema.getSubjects();
        assertEquals(2, subjects.size());
        assertEquals("Important subject", subjects.get(0));
        assertEquals("Unimportant subject", subjects.get(1));
    }

    @Test
    void testBadAttr() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "           <rdf:Description xmlns:xmpTPg=\"http://ns.adobe.com/xap/1.0/t/pg/\"" +
                    "                            xmlns:stDim=\"http://ns.adobe.com/xap/1.0/sType/Dimensions#\"" +
                    "		                 rdf:about=\"\">\n" +
                    "			<xmpTPg:MaxPageSize>\n" +
                    "				<rdf:Description stDim:X=\"4\" stDim:Y=\"3\" stDim:Z=\"inch\"/>\n" +
                    "			</xmpTPg:MaxPageSize>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"r\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("No type defined for {http://ns.adobe.com/xap/1.0/sType/Dimensions#}X", ex.getMessage());
        final DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPageTextSchema pageTextSchema = xmp.getPageTextSchema();
        DimensionsType dim = (DimensionsType) pageTextSchema.getProperty(XMPageTextSchema.MAX_PAGE_SIZE);
        assertEquals("DimensionsType{null x null null}", dim.toString());
        assertEquals("[X=TextType:4]", dim.getProperty("X").toString());
        assertEquals("[Y=TextType:3]", dim.getProperty("Y").toString());
        assertEquals("[Z=TextType:inch]", dim.getProperty("Z").toString());
    }

    @Test
    void testBadType() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d'?>\n" +
                    "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                    "         xmlns:iX=\"http://ns.adobe.com/iX/1.0/\">\n" +
                    "	<rdf:Description xmlns=\"http://ns.adobe.com/pdf/1.3/\"\n" +
                    "	                 xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\"\n" +
                    "	                 about=\"\"\n" +
                    "	                 pdf:Author=\"edocslib\"/>\n" +
                    "</rdf:RDF>\n" +
                    "<?xpacket end='r'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("No type defined for {http://ns.adobe.com/pdf/1.3/}Author", ex.getMessage());
        final DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        AdobePDFSchema adobePDFSchema = xmp.getAdobePDFSchema();
        TextType tt = (TextType) adobePDFSchema.getProperty("Author");
        assertEquals("[Author=TextType:edocslib]", tt.toString());
    }

    @Test
    void testBadType2() throws XmpParsingException, BadFieldValueException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"3.1.1-111\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "		<rdf:Description xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\"\n" +
                    "		                 rdf:about=\"\">\n" +
                    "			<pdf:Bad>Value</pdf:Bad>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"r\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("No type defined for {http://ns.adobe.com/pdf/1.3/}Bad", ex.getMessage());
        final DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        assertEquals("Value", xmp.getAdobePDFSchema().getUnqualifiedTextPropertyValue("Bad"));
    }

    @Test
    void testBadLocalName() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin='﻿' id='W5M0MpCehiHzreSzNTczkc9d'?><?adobe-xap-filters esc=\"CR\"?>\n" +
                    "<x:xapmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "	<rdf:RDF xmlns:iX=\"http://ns.adobe.com/iX/1.0/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xapmeta><?xpacket end='w'?>";
        final DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Expecting local name 'xmpmeta' and found 'xapmeta'", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        assertEquals(0, xmp2.getAllSchemas().size());
    }

    @Test
    void testBadXPacketEnd1() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\" ?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "    <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "        <rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:about=\"\">\n" +
                    "            <dc:format>application/pdf</dc:format>\n" +
                    "        </rdf:Description>\n" +
                    "    </rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket ends=\"w\" ?>";
        final DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Expected xpacket 'end' attribute (must be present and placed in first)", ex.getMessage());
    }

    @Test
    void testBadXPacketEnd2() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\" ?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "    <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "        <rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:about=\"\">\n" +
                    "            <dc:format>application/pdf</dc:format>\n" +
                    "        </rdf:Description>\n" +
                    "    </rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"k\" ?>";
        final DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Expected xpacket 'end' attribute with value 'r' or 'w' ", ex.getMessage());
    }

    @Test
    void testNoRdfChildren() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\" ?>" +
                    "  <x:xmpmeta xmlns:x=\"adobe:ns:meta/\"/>\n" +
                    "<?xpacket end=\"w\" ?>";
        final DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("No rdf description found in xmp", ex.getMessage());
    }

    @Test
    void testTextInsteadOfArray() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"3.1-701\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "		<rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                    "		                 rdf:about=\"\">\n" +
                    "			<dc:title>Title</dc:title>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"w\"?>";
        final DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Invalid array definition, expecting Alt and found Text [prefix=dc; name=title]", ex.getMessage());
    }

    @Test
    void testPropertyNotDefined() throws XmpParsingException
    {
        // While "Fired" does exist as a type, it's not the correct syntax, the PDFLib XMP validator complains too.
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin='﻿' id='W5M0MpCehiHzreSzNTczkc9d'?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"XMP toolkit 3.0-28, framework 1.6\">\n" +
                    "	<rdf:RDF xmlns:iX=\"http://ns.adobe.com/iX/1.0/\"\n" +
                    "	         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "		<rdf:Description xmlns:exif=\"http://ns.adobe.com/exif/1.0/\"\n" +
                    "		                 rdf:about=\"uuid:d9974396-53ee-11d9-9542-81b7ec7f4613\">\n" +
                    "			<exif:Flash rdf:parseType=\"Resource\">\n" +
                    "				<exif:Fired>False</exif:Fired>\n" +
                    "			</exif:Flash>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end='w'?>";
        final DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Property 'Fired' not defined in http://ns.adobe.com/exif/1.0/", ex.getMessage());
    }

    @Test
    void testBadAttr2() throws XmpParsingException
    {
        // File from image on page 14 from file 006054.pdf
        // exif:Flash is a structured type.
        // However the PDFLib XMP validator approves the file.
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin='﻿' id='W5M0MpCehiHzreSzNTczkc9d'?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"XMP toolkit 2.9.1-13, framework 1.6\">\n" +
                    "	<rdf:RDF xmlns:iX=\"http://ns.adobe.com/iX/1.0/\"\n" +
                    "	         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "		<rdf:Description xmlns:exif=\"http://ns.adobe.com/exif/1.0/\"\n" +
                    "		                 exif:FNumber=\"36/10\"\n" +
                    "		                 exif:FileSource=\"3\"\n" +
                    "		                 exif:Flash=\"1\"\n" +
                    "		                 rdf:about=\"\">\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end='r'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("The type 'Flash' in 'exif:Flash=1' is a structured or array type, but attributes are simple types", ex.getMessage());
        final DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        ExifSchema exifSchema = (ExifSchema) xmp.getSchema(ExifSchema.class);
        assertEquals("[Flash=TextType:1]", exifSchema.getProperty(ExifSchema.FLASH).toString());
    }

    @Test
    void testBadAttr3() throws XmpParsingException, TransformerException
    {
        // test text in attribute which should have been an array property
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d' bytes='1064'?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
"    <rdf:Description xmlns=\"http://purl.org/dc/elements/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" about=\"\" dc:creator=\"Creator\" />\n" +
"</rdf:RDF><?xpacket end='r'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("The type 'Text' in 'dc:creator=Creator' is a structured or array type, but attributes are simple types", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // make sure that nothing is lost in serialization
        serializer.serialize(xmp2, baos, true);
        final DomXmpParser xmpParser3 = new DomXmpParser();
        ex = assertThrows(XmpParsingException.class, () -> xmpParser3.parse(baos.toByteArray()));
        assertEquals("Invalid array definition, expecting Seq and found Text [prefix=dc; name=creator]", ex.getMessage());
        DomXmpParser xmpParser4 = new DomXmpParser();
        xmpParser4.setStrictParsing(false);
        XMPMetadata xmp4 = xmpParser4.parse(baos.toByteArray());
        DublinCoreSchema dublinCoreSchema = xmp4.getDublinCoreSchema();
        assertEquals("[creator=TextType:Creator]", dublinCoreSchema.getProperty(DublinCoreSchema.CREATOR).toString());
    }

    /**
     * Test empty attribute where an array is expected. The attribute is skipped in lenient mode.
     *
     * @throws XmpParsingException
     * @throws TransformerException
     * @throws BadFieldValueException
     */
    @Test
    void testBadAttr4() throws XmpParsingException, TransformerException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d' bytes='1206'?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" +
"    <rdf:Description xmlns=\"http://purl.org/dc/elements/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" about=\"\" dc:creator=\"\">\n" +
"        <dc:coverage>Coverage</dc:coverage>\n" +
"    </rdf:Description>\n" +
"</rdf:RDF><?xpacket end='r'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("The type 'Text' in 'dc:creator=' is a structured or array type, but attributes are simple types", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        DublinCoreSchema dublinCoreSchema2 = xmp2.getDublinCoreSchema();
        assertEquals("Coverage", dublinCoreSchema2.getCoverage());
        assertNull(dublinCoreSchema2.getProperty(DublinCoreSchema.CREATOR));
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp2, baos, true);
        DomXmpParser xmpParser3 = new DomXmpParser();
        xmpParser3.setStrictParsing(false);
        XMPMetadata xmp3 = xmpParser3.parse(baos.toByteArray());
        DublinCoreSchema dublinCoreSchema3 = xmp3.getDublinCoreSchema();
        assertEquals("Coverage", dublinCoreSchema3.getCoverage());
        assertNull(dublinCoreSchema2.getProperty(DublinCoreSchema.CREATOR));
    }

    /**
     * Test empty attribute where an LangAlt is expected. The attribute is skipped in lenient mode.
     *
     * @throws XmpParsingException
     * @throws TransformerException
     * @throws BadFieldValueException
     */
    @Test
    void testBadAttr5() throws XmpParsingException, TransformerException, BadFieldValueException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d' bytes='987'?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:iX=\"http://ns.adobe.com/iX/1.0/\">\n" +
"    <rdf:Description xmlns=\"http://purl.org/dc/elements/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" about=\"\" dc:title=\"\" dc:coverage=\"COVER\"/>\n" +
"</rdf:RDF><?xpacket end='r'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("The type 'LangAlt' in 'dc:title=' is a structured or array type, but attributes are simple types", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        DublinCoreSchema dublinCoreSchema2 = xmp2.getDublinCoreSchema();
        assertNull(dublinCoreSchema2.getTitle());
        assertNull(dublinCoreSchema2.getProperty(DublinCoreSchema.TITLE));
        assertEquals("COVER", dublinCoreSchema2.getCoverage());
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp2, baos, true);
        DomXmpParser xmpParser3 = new DomXmpParser();
        xmpParser3.setStrictParsing(false);
        XMPMetadata xmp3 = xmpParser3.parse(baos.toByteArray());
        DublinCoreSchema dublinCoreSchema3 = xmp3.getDublinCoreSchema();
        assertNull(dublinCoreSchema3.getTitle());
        assertNull(dublinCoreSchema3.getProperty(DublinCoreSchema.TITLE));
        assertEquals("COVER", dublinCoreSchema3.getCoverage());
    }

    @Test
    void testBadSchema() throws XmpParsingException
    {
        // from file 130841.pdf
        // structured type used like a schema
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin='﻿' id='W5M0MpCehiHzreSzNTczkc9d'?><?adobe-xap-filters esc=\"CRLF\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"XMP toolkit\">\n" +
                    "	<rdf:RDF xmlns:iX=\"http://ns.adobe.com/iX/1.0/\"\n" +
                    "	         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "		<rdf:Description xmlns:stJob=\"http://ns.adobe.com/xap/1.0/sType/Job#\"\n" +
                    "		                 rdf:about=\"uuid\"\n" +
                    "		                 stJob:id=\"jobid\"\n" +
                    "		                 stJob:name=\"some name\">\n" +
                    "			<stJob:URL>https://pdfbox.apache.org</stJob:URL>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end='w'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(
                XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("This namespace is not from a schema: http://ns.adobe.com/xap/1.0/sType/Job#", ex.getMessage());
    }

    @Test
    void testPDFBOX6126() throws XmpParsingException, BadFieldValueException, TransformerException
    {
        // XMP originally from PDFBOX-4325, had this exception:
        // Cannot find a definition for the namespace http://www.w3.org/1999/02/22-rdf-syntax-ns#, property: rdf:Description
        // Cause: "<rdf:Description" as child of <rdf:li .
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"Adobe XMP Core 5.1.0-jc003\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "		<rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                    "		                 xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\"\n" +
                    "		                 xmlns:pdfaExtension=\"http://www.aiim.org/pdfa/ns/extension/\"\n" +
                    "		                 xmlns:pdfaProperty=\"http://www.aiim.org/pdfa/ns/property#\"\n" +
                    "		                 xmlns:pdfaSchema=\"http://www.aiim.org/pdfa/ns/schema#\"\n" +
                    "		                 xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\"\n" +
                    "		                 xmlns:pdfuaid=\"http://www.aiim.org/pdfua/ns/id/\"\n" +
                    "		                 xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n" +
                    "		                 dc:format=\"application/pdf\"\n" +
                    "		                 pdf:Producer=\"iText® 5.5.13 ©2000-2018 iText Group NV (AGPL-version)\"\n" +
                    "		                 pdfaid:conformance=\"B\"\n" +
                    "		                 pdfaid:part=\"1\"\n" +
                    "		                 rdf:about=\"\"\n" +
                    "		                 xmp:CreateDate=\"2018-09-24T09:00:57+02:00\"\n" +
                    "		                 xmp:ModifyDate=\"2018-09-24T09:00:57+02:00\">\n" +
                    "			<pdfaExtension:schemas>\n" +
                    "				<rdf:Bag>\n" +
                    "					<rdf:li rdf:parseType=\"Resource\">\n" +
                    "						<rdf:Description pdfaSchema:namespaceURI=\"http://www.aiim.org/pdfua/ns/id/\"\n" +
                    "						                 pdfaSchema:prefix=\"pdfuaid\"\n" +
                    "						                 pdfaSchema:schema=\"PDF/UA identification schema\">\n" +
                    "							<pdfaSchema:property>\n" +
                    "								<rdf:Seq>\n" +
                    "									<rdf:li pdfaProperty:category=\"internal\"\n" +
                    "									        pdfaProperty:description=\"PDF/UA version identifier\"\n" +
                    "									        pdfaProperty:name=\"part\"\n" +
                    "									        pdfaProperty:valueType=\"Integer\"/>\n" +
                    "									<rdf:li pdfaProperty:category=\"internal\"\n" +
                    "									        pdfaProperty:description=\"PDF/UA amendment identifier\"\n" +
                    "									        pdfaProperty:name=\"amd\"\n" +
                    "									        pdfaProperty:valueType=\"Text\"/>\n" +
                    "									<rdf:li pdfaProperty:category=\"internal\"\n" +
                    "									        pdfaProperty:description=\"PDF/UA corrigenda identifier\"\n" +
                    "									        pdfaProperty:name=\"corr\"\n" +
                    "									        pdfaProperty:valueType=\"Text\"/>\n" +
                    "								</rdf:Seq>\n" +
                    "							</pdfaSchema:property>\n" +
                    "						</rdf:Description>\n" +
                    "					</rdf:li>\n" +
                    "				</rdf:Bag>\n" +
                    "			</pdfaExtension:schemas>\n" +
                    "			<pdfuaid:part>1</pdfuaid:part>\n" +
                    "		</rdf:Description>\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"w\"?>";
        DomXmpParser xmpParser1 = new DomXmpParser();
        XMPMetadata xmp1 = xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPSchema uaSchema1 = xmp1.getSchema("http://www.aiim.org/pdfua/ns/id/");
        assertEquals(1, uaSchema1.getIntegerPropertyValueAsSimple("part"));
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // make sure that nothing is lost in serialization
        serializer.serialize(xmp1, baos, true);
        DomXmpParser xmpParser2 = new DomXmpParser();
        XMPMetadata xmp2 = xmpParser2.parse(baos.toByteArray());
        XMPSchema uaSchema2  = xmp2.getSchema("http://www.aiim.org/pdfua/ns/id/");
        assertEquals(1, uaSchema2.getIntegerPropertyValueAsSimple("part"));
    }

    @Test
    void testNonStandardURIinRDF() throws XmpParsingException, TransformerException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 4.2.1-c041 52.342996, 2008/05/07-20:48:00        \">\n" +
"    <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
"        <rdf:Description xmlns:pdfx=\"http://ns.adobe.com/pdfx/1.3/\" rdf:about=\"\">\n" +
"            <pdfx:XPressPrivate>private</pdfx:XPressPrivate>\n" +
"        </rdf:Description>\n" +
"    </rdf:RDF>\n" +
"</x:xmpmeta><?xpacket end=\"w\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Cannot find a definition for the namespace http://ns.adobe.com/pdfx/1.3/, property: pdfx:XPressPrivate", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPSchema schema2 = xmp2.getSchema("http://ns.adobe.com/pdfx/1.3/");
        assertEquals("[XPressPrivate=TextType:private]", schema2.getProperty("XPressPrivate").toString());
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp2, baos, true);
        final DomXmpParser xmpParser3 = new DomXmpParser();
        ex = assertThrows(XmpParsingException.class, () -> xmpParser3.parse(baos.toByteArray()));
        assertEquals("Cannot find a definition for the namespace http://ns.adobe.com/pdfx/1.3/, property: pdfx:XPressPrivate", ex.getMessage());
        DomXmpParser xmpParser4 = new DomXmpParser();
        xmpParser4.setStrictParsing(false);
        XMPMetadata xmp4 = xmpParser4.parse(baos.toByteArray());
        XMPSchema schema4 = xmp4.getSchema("http://ns.adobe.com/pdfx/1.3/");
        assertEquals("[XPressPrivate=TextType:private]", schema4.getProperty("XPressPrivate").toString());
    }

    /**
     * Test empty property where an LangAlt is expected. The property is skipped in lenient mode.
     *
     * @throws XmpParsingException
     * @throws TransformerException
     */
    @Test
    void testBadProp() throws XmpParsingException, TransformerException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d' bytes='1506'?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:iX=\"http://ns.adobe.com/iX/1.0/\">\n" +
"    <rdf:Description xmlns=\"http://purl.org/dc/elements/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" about=\"\">\n" +
"        <dc:creator/>\n" +
"        <dc:coverage>Cover</dc:coverage>\n" +
"    </rdf:Description>\n" +
"</rdf:RDF><?xpacket end='r'?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Invalid array definition, expecting Seq and found nothing [prefix=dc; name=creator]", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        DublinCoreSchema dublinCoreSchema2 = xmp2.getDublinCoreSchema();
        assertNull(dublinCoreSchema2.getCreators());
        assertNull(dublinCoreSchema2.getProperty(DublinCoreSchema.CREATOR));
        assertEquals("Cover", dublinCoreSchema2.getCoverage());
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp2, baos, true);
        DomXmpParser xmpParser3 = new DomXmpParser();
        xmpParser3.setStrictParsing(false);
        XMPMetadata xmp3 = xmpParser3.parse(baos.toByteArray());
        DublinCoreSchema dublinCoreSchema3 = xmp3.getDublinCoreSchema();
        assertNull(dublinCoreSchema3.getCreators());
        assertNull(dublinCoreSchema3.getProperty(DublinCoreSchema.CREATOR));
        assertEquals("Cover", dublinCoreSchema3.getCoverage());
    }

    @Test
    void testParseFailure() throws XmpParsingException
    {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertTrue(ex.getMessage().startsWith("Failed to parse: "));
    }

    @Test
    void testNoXPacket() throws XmpParsingException
    {
        // must be "xpacket", not "packet"
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?packet begin=\"\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"3.1-701\">\n" +
                    "</x:xmpmeta><?packet end=\"w\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Bad processing instruction name : packet", ex.getMessage());
    }

    @Test
    void testDoubleEnd() throws XmpParsingException
    {
        String s = "<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d'?> \n" +
                    "<?xpacket begin='' id='W5M0MpCehiHzreSzNTczkc9d'?> \n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
                    "           x:xmptk=\"Adobe XMP Core 4.0-c316 44.253921, Sun Oct 01 2006 17:14:39\">\n" +
                    "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "	</rdf:RDF>\n" +
                    "</x:xmpmeta> \n" +
                    "<?xpacket end=\"w\"?> \n" +
                    "<?xpacket end='r'?> ";
        final DomXmpParser xmpParser = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("xmp should end after xpacket end processing instruction", ex.getMessage());
    }

    @Test
    void testBadInner() throws XmpParsingException
    {
        // file has "xmpMM:parseType". Changing this to "rdf:parseType" makes it work.
        final String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.2-c001 63.139439, 2010/09/27-13:37:26        \">\n" +
                    "    <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "        <rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\" xmlns:photoshop=\"http://ns.adobe.com/photoshop/1.0/\" xmlns:stEvt=\"http://ns.adobe.com/xap/1.0/sType/ResourceEvent#\" xmlns:stRef=\"http://ns.adobe.com/xap/1.0/sType/ResourceRef#\"  xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\" xmlns:xmpRights=\"http://ns.adobe.com/xap/1.0/rights/\">\n" +
                    "            <xmpMM:DerivedFrom xmpMM:parseType=\"Resource\">\n" +
                    "                <stRef:instanceID>uuid:6b838c4d-07e2-0611-2333-558805f93988</stRef:instanceID>\n" +
                    "                <stRef:documentID>uuid:6b838c4d-07e2-0611-2333-558805f93988</stRef:documentID>\n" +
                    "            </xmpMM:DerivedFrom>\n" +
                    "        </rdf:Description>\n" +
                    "    </rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"w\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("inner element should contain child elements : [stRef:instanceID: null]", ex.getMessage());
        String s2 = s.replace("xmpMM:parseType", "rdf:parseType");
        DomXmpParser xmpParser2 = new DomXmpParser();
        XMPMetadata xmp2 = xmpParser2.parse(s2.getBytes(StandardCharsets.UTF_8));
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp2.getXMPMediaManagementSchema();
        ResourceRefType derivedFromProperty = xmpMediaManagementSchema.getDerivedFromProperty();
        assertEquals("uuid:6b838c4d-07e2-0611-2333-558805f93988", derivedFromProperty.getInstanceID());
        assertEquals("uuid:6b838c4d-07e2-0611-2333-558805f93988", derivedFromProperty.getDocumentID());
    }

    @Test
    void testBadRdfNameSpace() throws XmpParsingException
    {
        // has https in rdf namespace
        final String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"XXX\">\n" +
                    "    <rdf:RDF xmlns:rdf=\"https://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "    </rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"w\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Expecting namespace 'http://www.w3.org/1999/02/22-rdf-syntax-ns#' and found 'https://www.w3.org/1999/02/22-rdf-syntax-ns#'", ex.getMessage());
    }

    @Test
    void testTypeInLiResourceElement() throws XmpParsingException
    {
        // <rdf:li xmlns:stEvt="http://ns.adobe.com/xap/1.0/sType/ResourceEvent#" rdf:parseType="Resource"
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "    <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "        <rdf:Description xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\" rdf:about=\"\">\n" +
                    "            <xmpMM:History>\n" +
                    "                <rdf:Seq>\n" +
                    "                    <rdf:li xmlns:stEvt=\"http://ns.adobe.com/xap/1.0/sType/ResourceEvent#\" rdf:parseType=\"Resource\">\n" +
                    "                        <stEvt:action>created</stEvt:action>\n" +
                    "                        <stEvt:parameters>original PDF file</stEvt:parameters>\n" +
                    "                    </rdf:li>\n" +
                    "                </rdf:Seq>\n" +
                    "            </xmpMM:History>\n" +
                    "        </rdf:Description>\n" +
                    "    </rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"w\"?>";
        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp2 = xmpParser.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp2.getXMPMediaManagementSchema();
        ArrayProperty historyProperty = xmpMediaManagementSchema.getHistoryProperty();
        ResourceEventType firstHistoryEntry = (ResourceEventType) historyProperty.getAllProperties().iterator().next();
        assertEquals("created", firstHistoryEntry.getAction());
        assertEquals("original PDF file", firstHistoryEntry.getParameters());
    }

    @Test
    void testLenientPdfaExtension() throws XmpParsingException
    {
        // First bag in pdfaExtension is incomplete.
        final String s = 
            "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
            "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"\n" +
            "           x:xmptk=\"Adobe XMP Core 4.2.1-c043 52.372728, 2009/01/18-15:08:04\">\n" +
            "	<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
            "		<rdf:Description rdf:about=\"\"\n" +
            "		                 xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\">\n" +
            "			<xmpMM:DocumentID>uuid:0b306144-6a43-dcbd-6b3e-c6b6b1df873d</xmpMM:DocumentID>\n" +
            "			<xmpMM:InstanceID>uuid:0b306144-6a43-dcbd-6b3e-c6b6b1df873d</xmpMM:InstanceID>\n" +
            "		</rdf:Description>\n" +
            "		<rdf:Description rdf:about=\"\"\n" +
            "		                 xmlns:pdfaExtension=\"http://www.aiim.org/pdfa/ns/extension/\"\n" +
            "		                 xmlns:pdfaSchema=\"http://www.aiim.org/pdfa/ns/schema#\"\n" +
            "		                 xmlns:pdfaProperty=\"http://www.aiim.org/pdfa/ns/property#\">\n" +
            "			<pdfaExtension:schemas>\n" +
            "				<rdf:Bag>\n" +
            "					<rdf:li rdf:parseType=\"Resource\">\n" +
            "						<pdfaSchema:namespaceURI>http://ns.adobe.com/pdf/1.3/</pdfaSchema:namespaceURI>\n" +
            "						<pdfaSchema:prefix>pdf</pdfaSchema:prefix>\n" +
            "						<pdfaSchema:schema>Adobe PDF Schema</pdfaSchema:schema>\n" +
            "					</rdf:li>\n" +
            "					<rdf:li rdf:parseType=\"Resource\">\n" +
            "						<pdfaSchema:namespaceURI>http://ns.adobe.com/xap/1.0/mm/</pdfaSchema:namespaceURI>\n" +
            "						<pdfaSchema:prefix>xmpMM</pdfaSchema:prefix>\n" +
            "						<pdfaSchema:schema>XMP Media Management Schema</pdfaSchema:schema>\n" +
            "						<pdfaSchema:property>\n" +
            "							<rdf:Seq>\n" +
            "								<rdf:li rdf:parseType=\"Resource\">\n" +
            "									<pdfaProperty:category>internal</pdfaProperty:category>\n" +
            "									<pdfaProperty:description>UUID based identifier for specific incarnation of a document</pdfaProperty:description>\n" +
            "									<pdfaProperty:name>InstanceID</pdfaProperty:name>\n" +
            "									<pdfaProperty:valueType>URI</pdfaProperty:valueType>\n" +
            "								</rdf:li>\n" +
            "							</rdf:Seq>\n" +
            "						</pdfaSchema:property>\n" +
            "					</rdf:li>\n" +
            "					<rdf:li rdf:parseType=\"Resource\">\n" +
            "						<pdfaSchema:namespaceURI>http://www.aiim.org/pdfa/ns/id/</pdfaSchema:namespaceURI>\n" +
            "						<pdfaSchema:prefix>pdfaid</pdfaSchema:prefix>\n" +
            "						<pdfaSchema:schema>PDF/A ID Schema</pdfaSchema:schema>\n" +
            "						<pdfaSchema:property>\n" +
            "							<rdf:Seq>\n" +
            "								<rdf:li rdf:parseType=\"Resource\">\n" +
            "									<pdfaProperty:category>internal</pdfaProperty:category>\n" +
            "									<pdfaProperty:description>Part of PDF/A standard</pdfaProperty:description>\n" +
            "									<pdfaProperty:name>part</pdfaProperty:name>\n" +
            "									<pdfaProperty:valueType>Integer</pdfaProperty:valueType>\n" +
            "								</rdf:li>\n" +
            "								<rdf:li rdf:parseType=\"Resource\">\n" +
            "									<pdfaProperty:category>internal</pdfaProperty:category>\n" +
            "									<pdfaProperty:description>Amendment of PDF/A standard</pdfaProperty:description>\n" +
            "									<pdfaProperty:name>amd</pdfaProperty:name>\n" +
            "									<pdfaProperty:valueType>Text</pdfaProperty:valueType>\n" +
            "								</rdf:li>\n" +
            "								<rdf:li rdf:parseType=\"Resource\">\n" +
            "									<pdfaProperty:category>internal</pdfaProperty:category>\n" +
            "									<pdfaProperty:description>Conformance level of PDF/A standard</pdfaProperty:description>\n" +
            "									<pdfaProperty:name>conformance</pdfaProperty:name>\n" +
            "									<pdfaProperty:valueType>Text</pdfaProperty:valueType>\n" +
            "								</rdf:li>\n" +
            "							</rdf:Seq>\n" +
            "						</pdfaSchema:property>\n" +
            "					</rdf:li>\n" +
            "				</rdf:Bag>\n" +
            "			</pdfaExtension:schemas>\n" +
            "		</rdf:Description>\n" +
            "	</rdf:RDF>\n" +
            "</x:xmpmeta>\n" +
            "<?xpacket end=\"w\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Missing pdfaSchema:property in type definition", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        assertTrue(xmpParser2.isStrictParsing());
        xmpParser2.setStrictParsing(false);
        assertFalse(xmpParser2.isStrictParsing());
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp2.getXMPMediaManagementSchema();
        assertEquals("uuid:0b306144-6a43-dcbd-6b3e-c6b6b1df873d", xmpMediaManagementSchema.getInstanceID());
        assertEquals("uuid:0b306144-6a43-dcbd-6b3e-c6b6b1df873d", xmpMediaManagementSchema.getDocumentID());
    }

    @Test
    void testNoProcessingInstruction() throws XmpParsingException, TransformerException, UnsupportedEncodingException
    {
        // From file 000163.pdf
        // Coastal Services Magazine Volume 11_6 November/December
        String s = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 4.1-c037 46.282696, Mon Apr 02 2007 18:36:42        \">\n" +
                " <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "  <rdf:Description rdf:about=\"\"\n" +
                "    xmlns:xapMM=\"http://ns.adobe.com/xap/1.0/mm/\"\n" +
                "    xmlns:stRef=\"http://ns.adobe.com/xap/1.0/sType/ResourceRef#\"\n" +
                "    xmlns:tiff=\"http://ns.adobe.com/tiff/1.0/\"\n" +
                "    xmlns:xap=\"http://ns.adobe.com/xap/1.0/\"\n" +
                "    xmlns:exif=\"http://ns.adobe.com/exif/1.0/\"\n" +
                "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                "    xmlns:photoshop=\"http://ns.adobe.com/photoshop/1.0/\"\n" +
                "   xapMM:DocumentID=\"uuid:F1FEDA1D7D03DA11B0F6E4B4E63B0143\"\n" +
                "   xapMM:InstanceID=\"uuid:7A28FBF56920DA11B4BBB356C0A5C72B\"\n" +
                "   tiff:Orientation=\"1\"\n" +
                "   tiff:XResolution=\"3050000/10000\"\n" +
                "   tiff:YResolution=\"3050000/10000\"\n" +
                "   tiff:ResolutionUnit=\"2\"\n" +
                "   tiff:NativeDigest=\"123456\"\n" +
                "   xap:ModifyDate=\"2005-09-08T09:13:10-04:00\"\n" +
                "   xap:CreatorTool=\"Adobe Photoshop CS2 Windows\"\n" +
                "   xap:CreateDate=\"2005-08-02T13:47:24-04:00\"\n" +
                "   xap:MetadataDate=\"2005-09-08T09:13:10-04:00\"\n" +
                "   exif:ColorSpace=\"-1\"\n" +
                "   exif:PixelXDimension=\"1525\"\n" +
                "   exif:PixelYDimension=\"387\"\n" +
                "   exif:NativeDigest=\"12345678\"\n" +
                "   dc:format=\"image/tiff\"\n" +
                "   photoshop:ColorMode=\"4\"\n" +
                "   photoshop:ICCProfile=\"U.S. Web Coated (SWOP) v2\"\n" +
                "   photoshop:History=\"\">\n" +
                "   <xapMM:DerivedFrom\n" +
                "    stRef:instanceID=\"adobe:docid:photoshop:28ff3dc5-4801-11d8-85d1-bb49d244e2ef\"\n" +
                "    stRef:documentID=\"adobe:docid:photoshop:28ff3dc5-4801-11d8-85d1-bb49d244e2ef\"/>\n" +
                "  </rdf:Description>\n" +
                " </rdf:RDF>\n" +
                "</x:xmpmeta>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("xmp should start with a processing instruction", ex.getMessage());
        DomXmpParser xmpParser2 = new DomXmpParser();
        xmpParser2.setStrictParsing(false);
        XMPMetadata xmp2 = xmpParser2.parse(s.getBytes(StandardCharsets.UTF_8));
        DublinCoreSchema dublinCoreSchema = xmp2.getDublinCoreSchema();
        assertEquals("image/tiff", dublinCoreSchema.getFormat());
        XMPMediaManagementSchema xmpMediaManagementSchema = xmp2.getXMPMediaManagementSchema();
        assertEquals("uuid:F1FEDA1D7D03DA11B0F6E4B4E63B0143", xmpMediaManagementSchema.getDocumentID());
        TiffSchema tiffSchema = (TiffSchema) xmp2.getSchema(TiffSchema.class);
        assertEquals("[Orientation=IntegerType:1]", tiffSchema.getProperty(TiffSchema.ORIENTATION).toString());
        PhotoshopSchema photoshopSchema = xmp2.getPhotoshopSchema();
        assertEquals((Integer) 4, photoshopSchema.getColorMode());
        ExifSchema exifSchema = (ExifSchema) xmp2.getSchema(ExifSchema.class);
        assertEquals("[PixelXDimension=IntegerType:1525]", exifSchema.getProperty(ExifSchema.PIXEL_X_DIMENSION).toString());
        XMPBasicSchema xmpBasicSchema = xmp2.getXMPBasicSchema();
        assertEquals("Adobe Photoshop CS2 Windows", xmpBasicSchema.getCreatorTool());
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp2, baos, true);
        // check that there are no isolated properties
        // (Happened before the change at the bottom of loadAttributes())
        String s2 = baos.toString("utf-8");
        assertFalse(s2.contains(" ColorMode="));
        assertFalse(s2.contains(" CreateDate="));
        assertFalse(s2.contains(" CreatorTool="));
        assertFalse(s2.contains(" DocumentID="));
        // now make sure that parsing again still brings the same data
        DomXmpParser xmpParser3 = new DomXmpParser();
        xmpParser3.setStrictParsing(false);
        XMPMetadata xmp3 = xmpParser3.parse(baos.toByteArray());
        DublinCoreSchema dublinCoreSchema3 = xmp3.getDublinCoreSchema();
        assertEquals("image/tiff", dublinCoreSchema3.getFormat());
        XMPMediaManagementSchema xmpMediaManagementSchema3 = xmp3.getXMPMediaManagementSchema();
        assertEquals("uuid:F1FEDA1D7D03DA11B0F6E4B4E63B0143", xmpMediaManagementSchema3.getDocumentID());
        TiffSchema tiffSchema3 = (TiffSchema) xmp3.getSchema(TiffSchema.class);
        assertEquals("[Orientation=IntegerType:1]", tiffSchema3.getProperty(TiffSchema.ORIENTATION).toString());
        PhotoshopSchema photoshopSchema3 = xmp3.getPhotoshopSchema();
        assertEquals((Integer) 4, photoshopSchema3.getColorMode());
        ExifSchema exifSchema3 = (ExifSchema) xmp3.getSchema(ExifSchema.class);
        assertEquals("[PixelXDimension=IntegerType:1525]", exifSchema3.getProperty(ExifSchema.PIXEL_X_DIMENSION).toString());
        XMPBasicSchema xmpBasicSchema3 = xmp3.getXMPBasicSchema();
        assertEquals("Adobe Photoshop CS2 Windows", xmpBasicSchema3.getCreatorTool());
    }

    @Test
    void testNoSchema() throws XmpParsingException
    {
        // From file 0075304.pdf, Centers for Medicare Medicaid Services
        // file uses "xml:ModifyDate" instead of "xmp:ModifyDate"
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.6-c016 91.163616, 2018/10/29-16:58:49        \">\n" +
                    "    <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "        <rdf:Description xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\" rdf:about=\"\">\n" +
                    "            <xml:ModifyDate>2019-07-26'T'19:28:53.000'-04:00'</xml:ModifyDate>\n" +
                    "            <xmp:ModifyDate>2019-07-29T15:12:07-04:00</xmp:ModifyDate>\n" +
                    "            <pdf:Producer>iTextSharp 4.0.3 (based on iText 2.0.2)</pdf:Producer>\n" +
                    "        </rdf:Description>\n" +
                    "    </rdf:RDF>\n" +
                    "</x:xmpmeta><?xpacket end=\"w\"?>";
        final DomXmpParser xmpParser1 = new DomXmpParser();
        XmpParsingException ex = assertThrows(XmpParsingException.class,
                () -> xmpParser1.parse(s.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Schema is not set in this document : http://www.w3.org/XML/1998/namespace, property: xml:ModifyDate", ex.getMessage());
    }
}
