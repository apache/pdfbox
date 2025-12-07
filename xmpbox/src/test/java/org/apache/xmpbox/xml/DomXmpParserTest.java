/** ***************************************************************************
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
 *************************************************************************** */
package org.apache.xmpbox.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XMPageTextSchema;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.DefinedStructuredType;
import org.apache.xmpbox.type.DimensionsType;
import org.apache.xmpbox.type.PDFASchemaType;
import org.apache.xmpbox.type.ResourceEventType;
import org.apache.xmpbox.type.ResourceRefType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class DomXmpParserTest
{

    @Test
    public void testPDFBox5835() throws IOException, XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-5835.xml");
        DomXmpParser dxp = new DomXmpParser();
        dxp.setStrictParsing(false);
        XMPMetadata xmp = dxp.parse(fis);
        assertEquals("A", xmp.getPDFIdentificationSchema().getConformance());
        assertEquals((Integer) 3, xmp.getPDFIdentificationSchema().getPart());
        fis.close();
    }

    @Test
    public void testPDFBox5976() throws XmpParsingException, UnsupportedEncodingException
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
        XMPMetadata xmp = xmpParser.parse(s.getBytes("utf-8"));
        assertEquals("B", xmp.getPDFIdentificationSchema().getConformance());
        assertEquals((Integer) 3, xmp.getPDFIdentificationSchema().getPart());
    }

    /**
     * PDFBOX-6106: Check that "pdf:CreationDate='2004-01-30T17:21:50Z'" is detected as incorrect.
     * (Only Keywords, PDFVersion, and Producer are allowed in strict mode)
     *
     * @throws XmpParsingException
     */
    @Test
    public void testPDFBox6106() throws XmpParsingException, UnsupportedEncodingException
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
        try
        {
            xmpParser.parse(s.getBytes("utf-8"));
            fail("XmpParsingException expected");
        }
        catch (XmpParsingException ex)
        {
            assertEquals("No type defined for {http://ns.adobe.com/pdf/1.3/}CreationDate", ex.getMessage());
        }        
    }

    /**
     * PDFBOX-5288: check that namespace declaration within an "rdf:li" element is found.
     *
     * @throws XmpParsingException
     */
    @Test
    public void testPDFBox5288() throws XmpParsingException, UnsupportedEncodingException
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
        XMPMetadata xmp = xmpParser.parse(s.getBytes("utf-8"));
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
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testPageTextSchema() throws XmpParsingException, UnsupportedEncodingException
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
        XMPMetadata xmp = xmpParser.parse(s.getBytes("utf-8"));
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
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testPageTextSchema2() throws XmpParsingException, UnsupportedEncodingException
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
        XMPMetadata xmp = xmpParser.parse(s.getBytes("utf-8"));
        XMPageTextSchema pageTextSchema = xmp.getPageTextSchema();
        DimensionsType dim = (DimensionsType) pageTextSchema.getProperty(XMPageTextSchema.MAX_PAGE_SIZE);
        assertEquals("DimensionsType{4.0 x 3.0 inch}", dim.toString());
        assertEquals("[NPages=IntegerType:7]", pageTextSchema.getProperty(XMPageTextSchema.N_PAGES).toString());
    }

    /**
     * PDFBOX-3882: Test PageTextSchema with dimensions as attributes only.
     *
     * @throws XmpParsingException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testPageTextSchema3() throws XmpParsingException, UnsupportedEncodingException
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
        XMPMetadata xmp = xmpParser.parse(s.getBytes("utf-8"));
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
    public void testPDFBox3882() throws IOException, XmpParsingException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/xml/PDFBOX-3882-dematbox.xml");
        DomXmpParser dxp = new DomXmpParser();
        dxp.setStrictParsing(false);
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
        is.close();
    }
}
